package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventHandler;
import com.stabilise.util.concurrent.task.Task.State;
import com.stabilise.util.concurrent.task.TaskEvent.FailEvent;


class TaskUnit implements Runnable, TaskHandle, TaskView {
    
    /** Prototype tracker. Only ever used during construction; during operation
     * this will always be null. */
    PrototypeTracker protoTracker;
    /** Tracker. Initially null, but set by {@link #build()} when the task
     * hierarchy is built. */
    protected TaskTracker tracker;
    
    /** The almighty owner task. This is set before run() is invoked by
     * whatever gets this unit running. */
    protected Task owner = null;
    /** The group we belong to. null if we don't belong to a group. */
    protected TaskGroup group = null;
    /** The task queued to run when this one finishes. If null (and group is
     * null), we are the last unit and need to clean up the main task. */
    protected TaskUnit next = null;
    
    /** The actual task! Only null for dummy units (e.g. TaskGroup instances). */
    protected TaskRunnable task = null;
    
    /** The thread upon which this unit is executing. This is set in run()
     * before this unit begins executing. Used as a target for interrupts. */
    private Thread thread;
    /** References the throwable which caused this unit to fail, if it failed.
     * null otherwise. */
    private Throwable throwable = null;
    
    private final EventDispatcher events;
    
    
    /**
     * Creates a new task.
     * 
     * @param task The actual task to run. Null if this is a group.
     * @param protoTracker The prototype for our tracker. Never null thanks to
     * TaskBuilder.
     */
    public TaskUnit(TaskRunnable task, PrototypeTracker protoTracker) {
        this.task = task;
        this.protoTracker = protoTracker;
        this.events = EventDispatcher.normal();
    }
    
    /**
     * Builds the hierarchy of units underneath and including this one by
     * getting our trackers from the prototype trackers.
     */
    void buildHierarchy() {
        // Use iteration instead of recursion to avoid unbounded stack growth
        for(TaskUnit u = this; u != null; u = u.next) {
            u.build();
        }
    }
    
    protected void build() {
        tracker = protoTracker.get();
        protoTracker = null; // gc pls
    }
    
    @Override
    public void run() {
        owner.onUnitStart();
        
        setThread();
        
        tracker.start(task);
        
        // Check for cancellation. We check in two ways:
        // 1. via owner.setCurrent(), which does the check for us in a
        //    synchronous manner. It is important that we do this after setting
        //    thread = Thread.currentThread() (as we have above) to avoid a
        //    race condition with Task.cancel().
        // 2. polling owner.cancelled() otherwise
        if((group == null && !owner.setCurrent(this)) || owner.cancelled()) {
            if(group != null)
                group.onSubtaskFinish(false);
            return;
        }
        
        events.post(TaskEvent.START);
        
        try {
            execute();
        } catch(Exception e) {
            fail(e);
            return;
        }
        
        finish();
    }
    
    /** Designed to be overridden by TaskGroup since a group doesn't really run
     * on its own thread. */
    protected void setThread() {
        thread = Thread.currentThread();
    }
    
    protected void fail(Exception e) {
        throwable = e;
        
        tracker.setState(State.FAILED); // TODO: is this necessary?
        events.post(TaskEvent.STOP);
        events.post(new FailEvent(e));
        
        if(group != null)
            group.onSubtaskFinish(false);
        owner.fail(this); // bring the entire Task down with us
        
        clearInterrupt();
        owner.onUnitStop();
    }
    
    protected boolean execute() throws Exception {
        if(task != null)
            task.run(this);
        return true;
    }
    
    /**
     * Invoked when this task successfully finishes. For a TaskUnit, this is
     * invoked immediately after its TaskRunnable finishes, and for a
     * TaskGroup, this is invoked once all subtasks have completed.
     */
    protected void finish() {
        tracker.setState(State.COMPLETED); // TODO: is this necessary?
        events.post(TaskEvent.STOP);
        events.post(TaskEvent.COMPLETE);
        clearInterrupt();
        owner.onUnitStop();
        if(next != null) {
            next.owner = owner;
            // Reuse current thread rather than submit to executor
            next.run(); // TODO this is recursive
        } else if(group != null) {
            group.onSubtaskFinish(true);
        } else { // we're the last task
            owner.setState(State.COMPLETED);
        }
    }
    
    /**
     * Sets the owner Task. This should be invoked by whatever runs or
     * schedules this unit for runnning.
     */
    TaskUnit setTask(Task task) {
        this.owner = task;
        return this;
    }
    
    @Override
    public void setStatus(String status) {
        tracker.setStatus(status);
    }
    
    @Override
    public void increment(long parts) {
        tracker.increment(parts);
    }
    
    @Override
    public void set(long parts) {
        tracker.set(parts);
    }
    
    @Override
    public void post(Event e) {
        events.post(e);
    }
    
    /**
     * Adds a multi-use event listener. For builder use only.
     * 
     * @see EventDispatcher#post(Event)
     */
    <E extends Event> void addListener(Executor exec, E e, EventHandler<? super E> h) {
        events.addListener(exec, e, h);
    }
    
    void cancel() {
        if(thread != null && tracker.getState() == State.RUNNING)
            thread.interrupt();
    }
    
    @Override
    public boolean pollCancel() {
        return isTaskThread() && (thread.isInterrupted() || owner.cancelled());
    }
    
    private boolean isTaskThread() {
        // Check state too to avoid leaking ownership across the same thread,
        // in case the thread is a part of a pool and is reused.
        return Thread.currentThread().equals(thread)
                && tracker.getState() == State.RUNNING;
    }
    
    /**
     * Gets the failure cause of this task. Only guaranteed to be non-null when
     * this unit is guaranteed to have failed.
     */
    Throwable getFailCause() {
        return throwable;
    }
    
    /**
     * Clears the current thread's interrupt status as to block an interrupt
     * from propagating from this task to whatever is next executed on this
     * thread.
     */
    private void clearInterrupt() {
        Thread.interrupted();
    }
    
    @Override
    public String status() {
        return tracker.getStatus();
    }
    
    @Override
    public double fractionCompleted() {
        return tracker.fractionCompleted();
    }
    
    @Override
    public long partsCompleted() {
        return tracker.getPartsCompleted();
    }
    
    @Override
    public long totalParts() {
        return tracker.getTotalParts();
    }
    
    @Override
    public String toString() {
        return status() + "... " + percentCompleted() + "%";
    }
    
}
