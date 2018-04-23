package com.stabilise.util.concurrent.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import javaslang.Tuple2;
import javaslang.control.Either;

import com.stabilise.util.Checks;
import com.stabilise.util.Printable;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.task.TaskHandle;
import com.stabilise.util.concurrent.task.TaskView;
import com.stabilise.util.concurrent.task.TaskEvent.FailEvent;


class TaskUnit implements Runnable, TaskHandle, TaskView, Printable {
    
    private final TaskRunnable task;
    private final TaskTracker tracker;
    
    /** Tuple cases:
     * <br><tt>( (TaskRunnable queuedTask, Long parts), Boolean parallel)</tt>
     * <br><tt>(Unit task, Boolean runDirectly)</tt> */
    private final List<Tuple2<Either<Tuple2<TaskRunnable, Long>, TaskUnit>, Boolean>> children
            = new ArrayList<>();
    private FlattenedRunnable flattened = null;
    private final AtomicInteger numSubtasks = new AtomicInteger(0);
    
    private final Executor exec;
    private volatile Thread thread = null;
    
    private TaskImpl owner;
    private boolean published;
    private final boolean publishable;
    private final boolean sequential;
    private final boolean levelHead;
    
    private TaskUnit parent;
    private TaskUnit next;
    
    private final EventDispatcher events;
    
    
    /**
     * Constructor for use by Prototype.build().
     */
    public TaskUnit(Executor exec, TaskRunnable task, TaskTracker tracker,
            EventDispatcher events) {
        this(exec, task, tracker, events, true, true, false);
    }
    
    /**
     * Constructor for use by buildSubtasks().
     */
    public TaskUnit(Executor exec, TaskRunnable task, TaskTracker tracker,
            boolean publishable, boolean sequential, boolean levelHead) {
        this(exec, task, tracker, EventDispatcher.concurrentRetained(),
                publishable, sequential, levelHead);
    }
    
    private TaskUnit(Executor exec, TaskRunnable task, TaskTracker tracker,
            EventDispatcher events, boolean publishable, boolean sequential,
            boolean levelHead) {
        this.exec = exec;
        this.task = task;
        this.tracker = tracker;
        this.events = events;
        this.publishable = publishable;
        this.sequential = sequential;
        this.levelHead = levelHead;
        
        // Cheaty way of preventing parallel units from being published to the
        // stack.
        published = !sequential;
    }
    
    @Override
    public void run() {
        if(!tracker.setState(State.UNSTARTED, State.RUNNING))
            throw new IllegalStateException();
        
        thread = Thread.currentThread();
        
        prepublish();
        
        // Note that we don't publish this unit to the owner Task's stack
        // immediately; instead, we wait for something to trigger publishing,
        // or force a publish after execution if nothing triggered it.
        
        if(testCancel()) // cancel test no.1
            return;
        
        events.post(TaskEvent.START);
        
        try {
            task.run(this);
        } catch(Throwable e) {
            fail(e);
            return;
        }
        
        publish(); // force the publish here
        
        if(testCancel()) // cancel test no.2
            return;
        
        if(!tracker.setState(State.RUNNING, State.COMPLETION_PENDING))
            throw new IllegalStateException();
        
        buildSubtasks();
        runSubtasks();
        
        tryFinish();
    }
    
    private void buildSubtasks() {
        // I'd rather throw an exception here, but meh. It's not important.
        if(flattened != null)
            endFlatten();
        
        Box<TaskUnit> firstSeq = Boxes.emptyMut();
        Box<TaskUnit> lastSeq  = Boxes.emptyMut();
        
        children.replaceAll(e -> {
            Tuple2<TaskRunnable, Long> tup = e._1.left().get();
            TaskRunnable r = tup._1;
            long parts = tup._2;
            boolean seq = !e._2; // !parallel
            boolean runDirectly = true;
            
            // We only publish sequential units for obvious reasons, and only
            // the first sequential unit may act as the head to start a new
            // level.
            TaskUnit u = new TaskUnit(exec, r, new TaskTracker(parts),
                    publishable & seq, seq, seq && !firstSeq.isPresent());
            u.parent = this;
            u.owner = owner;
            
            if(seq) {
                lastSeq.ifPresent(u2 -> u2.next = u);
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
        return tracker.getState() == State.COMPLETION_PENDING && subtasksFinished();
    }
    
    private boolean subtasksFinished() {
        return children.stream().allMatch(t -> t._1.right().get().isFinished());
    }
    
    private void tryFinish() {
        if(!canFinish())
            return;
        
        if(testCancel()) // cancel test no.3
            return;
        
        //System.out.println("Completed \"" + status() + "\": " + tracker.getState());
        
        if(!tracker.setState(State.COMPLETION_PENDING, State.COMPLETED))
            throw new IllegalStateException();
        
        events.post(TaskEvent.STOP);
        events.post(TaskEvent.COMPLETE);
        
        if(next != null) {
            next.owner = owner;
            // Submit to executor rather than reuse current thread as per
            // next.run() to avoid unbounded recursive stack growth.
            exec.execute(next);
        } else
            unpublish();
    }
    
    /**
     * Checks for whether or not this unit is considered finished.
     */
    private boolean isFinished() {
        State s = tracker.getState();
        return s == State.COMPLETED || s == State.FAILED;
    }
    
    /**
     * Fails this unit.
     */
    private void fail(Throwable t) {
        //System.out.println("Failed \"" + status() + "\": " + tracker.getState());
        
        if(!tracker.setState(State.RUNNING, State.FAILED) &&
                !tracker.setState(State.COMPLETION_PENDING, State.FAILED))
            throw new IllegalStateException();
        
        events.post(TaskEvent.STOP);
        events.post(new FailEvent(t));
        
        owner.fail(t); // bring the entire Task down with us
        
        // Clear the interrupt flag to prevent it from leaking if this thread
        // is reused as part of a pool.
        // We conservatively synchronise to avoid the unlikely race condition
        // with cancel() which can cause us to miss the interrupt.
        // Race start in this method is at the very start when we CAS the
        // tracker state to FAILED, and race end is where we clear the
        // interrupt.
        synchronized(tracker) {
            Thread.interrupted();
        }
        
        // Fail all the units that come sequentially after us so that
        // parent.subtasksFinished() returns true as appropriate
        for(TaskUnit u = next; u != null; u = u.next)
            u.tracker.setState(State.FAILED);
        
        unpublish();
    }
    
    /**
     * "Prepublishes"/"minipublishes" this unit by invoking {@link
     * Task#onUnitStart()} if this unit is the first in a recognisably distinct
     * stream of tasks (i.e. a standalone parallel unit or the first in a
     * sequential list).
     * 
     * <p>It is absolutely critical for proper task execution that every
     * invocation of onUnitStart() has an associated invocation of {@link
     * Task#onUnitStop()} (see {@link #unpublish()}).
     */
    private void prepublish() {
        // Only invoke onUnitStart for parallel units and for the first unit in
        // a sequential list.
        if(!sequential || levelHead)
            owner.onUnitStart();
    }
    
    /**
     * Publishes this unit to the public task stack, if it hasn't been
     * published already.
     */
    private void publish() {
        if(publishable && !published) {
            published = true;
            if(levelHead)
                owner.beginSubtask(this);
            else
                owner.nextSequential(this);
        }
    }
    
    /**
     * Unpublishes this unit from the public task, if applicable.
     */
    private void unpublish() {
        owner.onUnitStop();
        
        if(parent != null) {
            // This was the last subtask in its group
            if(sequential && publishable && published)
                owner.endSubtask(this);
            parent.onSubtaskComplete(this);
        } else if(sequential && publishable && published) {
            // This was the last unit
            owner.endSubtask(this);
            owner.setState(tracker.getState()); // i.e. COMPLETED or FAILED
        }
    }
    
    /**
     * Cancels this unit by posting an interrupt to its execution thread. If it
     * has subtasks, also cancels the subtasks.
     */
    public void cancel() {
        // Synchronised to avoid a race condition with fail() to prevent
        // the interrupt from leaking.
        synchronized(this) {
            Thread t = thread;
            State s = tracker.getState();
            if(t != null && s == State.RUNNING) // s == State.RUNNING is race start
                t.interrupt();                  // t.interrupt() is race end
        }
        
        // Cancel subtasks
        children.forEach(e -> {
            if(e._1.isRight())
                e._1.right().get().cancel();
        });
    }
    
    /**
     * Polls for task cancellation, and invokes {@link #fail(Throwable)} if
     * the task has been cancelled.
     * 
     * @return {@code true} if the task has been cancelled; {@code false} if
     * not.
     */
    private boolean testCancel() {
        if(owner.cancelled()) {
            fail(owner.failurePoint());
            return true;
        }
        return false;
    }
    
    /**
     * Invoked by a subtask when a subtask stream is completed.
     */
    private void onSubtaskComplete(TaskUnit sub) {
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
    public void setTotal(long totalParts) {
        if(published)
            throw new IllegalStateException();
        tracker.setTotal(totalParts);
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
        spawn(parallel, 0, r);
    }
    
    /**
     * General version of {@link #spawn(boolean, TaskRunnable)}.
     * 
     * <p>TODO: Make spawned tasks report parts to the parent if the parent has
     * incomplete parts left over.
     */
    private void spawn(boolean parallel, long parts, TaskRunnable r) {
        if(flattened != null) {
            if(parallel)
                throw new IllegalArgumentException("Parallel not allowed while flattening");
            flattened.add(r);
        } else
            children.add(new Tuple2<>(Either.left(new Tuple2<>(
                    Objects.requireNonNull(r),
                    Checks.test(parts, TaskTracker.MIN_PARTS, TaskTracker.MAX_PARTS))),
                    parallel));
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
            children.add(new Tuple2<>(Either.left(
                    new Tuple2<>(flattened, (long)flattened.size())),
                    false)); // false = not parallel
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
    
    TaskUnit setOwner(TaskImpl owner) {
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
        @Override public void set(long parts) {}
        @Override public void setTotal(long totalParts) {}
        
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
