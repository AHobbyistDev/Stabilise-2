package com.stabilise.util.concurrent.task2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import javaslang.Tuple2;
import javaslang.control.Either;

import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.task2.TaskHandle;
import com.stabilise.util.concurrent.task2.TaskView;
import com.stabilise.util.concurrent.task2.TaskEvent.FailEvent;


class TaskUnit implements Runnable, TaskHandle, TaskView {
    
    private final TaskRunnable task;
    private final TaskTracker tracker;
    
    /** Tuple cases:
     * <br><tt>(TaskRunnable queuedTask, Boolean parallel)</tt>
     * <br><tt>(Unit task, Boolean runDirectly)</tt> */
    private final List<Tuple2<Either<TaskRunnable, TaskUnit>, Boolean>> children = new ArrayList<>();
    private FlattenedRunnable flattened = null;
    private final AtomicInteger numSubtasks = new AtomicInteger(0);
    
    private final Executor exec;
    private volatile Thread thread = null;
    
    private Task owner;
    private boolean published;
    private final boolean publishable;
    private final boolean levelHead;
    
    private TaskUnit parent;
    private TaskUnit next;
    
    private final EventDispatcher events = EventDispatcher.concurrentRetained();
    
    
    public TaskUnit(Executor exec, TaskRunnable task, TaskTracker tracker,
            boolean publishable, boolean levelHead) {
        this.exec = exec;
        this.task = task;
        this.tracker = tracker;
        this.publishable = publishable;
        this.levelHead = levelHead;
        
        published = !publishable;
    }
    
    @Override
    public void run() {
        if(!tracker.setState(State.UNSTARTED, State.RUNNING))
            throw new IllegalStateException();
        
        thread = Thread.currentThread();
        
        if(owner.cancelled())
            fail(new CancellationException());
        
        events.post(TaskEvent.START);
        
        try {
            task.run(this);
        } catch(Throwable e) {
            fail(e);
            return;
        }
        
        publish();
        
        buildSubtasks();
        runSubtasks();
        
        if(!tracker.setState(State.RUNNING, State.COMPLETED_PENDING))
            throw new IllegalStateException();
        
        tryFinish();
    }
    
    private void buildSubtasks() {
        // I'd rather throw an exception here, but meh. It's not important.
        if(flattened != null)
            endFlatten();
        
        Box<TaskUnit> firstSeq = Boxes.emptyMut();
        Box<TaskUnit> lastSeq  = Boxes.emptyMut();
        
        children.replaceAll(e -> {
            TaskRunnable r = e._1.left().get();
            boolean runDirectly = true;
            
            long parts = 0;
            if(r instanceof FlattenedRunnable)
                parts = ((FlattenedRunnable)r).size();
            
            // Parallel units are non-publishable since there's no "current"
            // unit in a parallel setting.
            TaskUnit u = new TaskUnit(exec, r, new TaskTracker(parts), !e._2, !firstSeq.isPresent());
            u.parent = this;
            u.owner = owner;
            
            if(!e._2) { // !parallel
                lastSeq.set(u);
                if(firstSeq.isPresent())
                    runDirectly = false;
                else
                    firstSeq.set(u);
            }
            if(runDirectly)
                numSubtasks.getAndIncrement();
            return new Tuple2<>(Either.right(u), runDirectly);
        });
    }
    
    private void runSubtasks() {
        children.forEach(e -> {
            if(e._2) // if(runDirectly)
                exec.execute(e._1.right().get());
        });
    }
    
    private boolean canFinish() {
        return tracker.getState() == State.COMPLETED_PENDING && subtasksFinished();
    }
    
    private boolean subtasksFinished() {
        return children.stream().allMatch(t -> t._1.right().get().isFinished());
    }
    
    private void tryFinish() {
        if(!canFinish())
            return;
        
        if(!tracker.setState(State.COMPLETED_PENDING, State.COMPLETED))
            throw new IllegalStateException();
        
        events.post(TaskEvent.STOP);
        events.post(TaskEvent.COMPLETE);
        
        if(next != null) {
            next.owner = owner;
            // Submit to executor rather than reuse current thread as per
            // next.run() to avoid unbounded recursive stack growth.
            exec.execute(next);
        } else if(parent != null) {
            // This was the last subtask in its group
            owner.endSubtask();
            parent.onSubtaskComplete();
        } else if(publishable) {
            // This was the last unit
            owner.endSubtask();
            owner.setState(State.COMPLETED);
        }
    }
    
    private boolean isFinished() {
        return tracker.getState() == State.COMPLETED;
    }
    
    private void fail(Throwable t) {
        if(!tracker.setState(State.RUNNING, State.FAILED))
            throw new IllegalStateException();
        
        events.post(TaskEvent.STOP);
        events.post(new FailEvent(t));
        
        owner.fail(t); // bring the entire Task down with us
        
        // Clear the interrupt flag to prevent it from leaking if this thread
        // is reused as part of a pool.
        Thread.interrupted();
        
        owner.onUnitStop(); // TODO: y
    }
    
    /** Publishes this unit to the public task stack, if it hasn't been
     * published already. */
    private void publish() {
        if(!published) {
            published = true;
            if(levelHead)
                owner.beginSubtask(this);
            else
                owner.nextSequential(this);
        }
    }
    
    public void cancel() {
        Thread t = thread;
        State s = tracker.getState();
        if(t != null && s == State.RUNNING)
            t.interrupt();
        
        // Cancel subtasks
        children.forEach(e -> {
            if(e._1.isRight())
                e._1.right().get().cancel();
        });
    }
    
    private void onSubtaskComplete() {
        if(numSubtasks.decrementAndGet() == 0)
            tryFinish();
    }
    
    // TaskHandle
    
    @Override
    public void setStatus(String status) {
        tracker.setStatus(status);
    }
    
    @Override
    public void increment(long parts) {
        tracker.increment(parts);
        publish();
    }
    
    @Override
    public void set(long parts) {
        tracker.set(parts);
        publish();
    }
    
    @Override
    public boolean pollCancel() {
        return isTaskThread() && (thread.isInterrupted() || owner.cancelled());
    }
    
    private boolean isTaskThread() {
        // Check state to avoid leaking ownership across the same thread,
        // in case the thread is a part of a pool and is reused.
        return Thread.currentThread().equals(thread)
                && tracker.getState() == State.RUNNING;
    }
    
    @Override
    public void spawn(boolean parallel, TaskRunnable r) {
        if(flattened != null) {
            if(parallel)
                throw new IllegalArgumentException("Parallel not allowed while flattening");
            flattened.add(r);
        } else
            children.add(new Tuple2<>(Either.left(Objects.requireNonNull(r)), parallel));
    }
    
    @Override
    public void beginFlatten() {
        if(flattened != null)
            throw new IllegalStateException("Already flattening");
        flattened = new FlattenedRunnable();
    }
    
    @Override
    public void endFlatten() {
        if(flattened == null)
            throw new IllegalStateException("Not currently flattening");
        if(flattened.runnables.size() != 0)
            children.add(new Tuple2<>(Either.left(flattened), false)); // false = not parallel
        flattened = null;
    }
    
    // TaskView
    
    @Override
    public String status() {
        return tracker.getStatus();
    }
    
    @Override
    public long partsCompleted() {
        return tracker.getPartsCompleted();
    }
    
    @Override
    public long totalParts() {
        return tracker.getTotalParts();
    }
    
    // For use by Prototype and Task
    
    /**
     * Sets the next task to be run after this one.
     */
    void setNext(TaskUnit unit) {
        this.next = unit;
    }
    
    TaskUnit setOwner(Task owner) {
        this.owner = owner;
        return this;
    }
    
    // Generic Object methods
    
    @Override
    public String toString() {
        return status() + "... " + percentCompleted() + "%";
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /** A TaskRunnable which is composed of a sequence of other TaskRunnables. */
    private static class FlattenedRunnable implements TaskRunnable {
        
        private final List<TaskRunnable> runnables = new ArrayList<>();
        
        public void add(TaskRunnable r) {
            runnables.add(r);
        }
        
        public int size() {
            return runnables.size();
        }
        
        @Override
        public void run(TaskHandle handle) throws Throwable {
            // We use a NonForwardingHandle as the only increments should come
            // from completions of partial tasks.
            TaskHandle h = new NonForwardingHandle(handle);
            for(TaskRunnable r : runnables) {
                r.run(h);
                handle.increment();
            }
        }
        
    }
    
    /**
     * A NonForwardingHandle wraps a TaskHandle, and blocks any attempt to
     * increment the parts count.
     */
    private static class NonForwardingHandle implements TaskHandle {
        
        private final TaskHandle handle;
        
        private NonForwardingHandle(TaskHandle handle) {
            this.handle = handle;
        }
        
        @Override
        public void setStatus(String status) {
            handle.setStatus(status);
        }
        
        @Override public void increment(long parts) {}
        @Override public void set(long parts) { }
        
        @Override
        public boolean pollCancel() {
            return handle.pollCancel();
        }
        
        @Override
        public void spawn(boolean parallel, TaskRunnable r) {
            handle.spawn(parallel, r);
        }
        
        @Override
        public void beginFlatten() {
            handle.beginFlatten();
        }
        
        @Override
        public void endFlatten() {
            handle.endFlatten();
        }
        
    }
    
}