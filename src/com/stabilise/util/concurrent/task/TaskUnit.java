package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;
import com.stabilise.util.concurrent.task.Task.State;
import com.stabilise.util.concurrent.task.TaskEvent.FailEvent;


class TaskUnit implements Runnable, TaskHandle {
    
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
     * @param exec The executor with which to execute event listeners. Never
     * null thanks to TaskBuilder.
     * @param task The actual task to run. Null if this is a group.
     * @param protoTracker The prototype for our tracker. Never null thanks to
     * TaskBuilder.
     */
    public TaskUnit(Executor exec, TaskRunnable task, PrototypeTracker protoTracker) {
        this.task = task;
        this.protoTracker = protoTracker;
        this.events = new EventDispatcher(exec);
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
        thread = Thread.currentThread();
        
        tracker.start(task);
        
        if(group == null && !owner.setCurrent(this)) {
            // Cancelled = we can't do anything now
            return;
        }
        
        boolean success = false;
        events.post(TaskEvent.START);
        try {
            success = execute();
        } catch(Exception e) {
            throwable = e;
            events.post(TaskEvent.STOP);
            events.post(new FailEvent(e));
            owner.fail(this);
        }
        if(success)
            finish();
    }
    
    protected boolean execute() throws Exception {
        if(task != null)
            task.run(this);
        return true;
    }
    
    protected void finish() {
        tracker.setState(State.COMPLETED);
        events.post(TaskEvent.STOP);
        events.post(TaskEvent.COMPLETE);
        if(next != null) {
            next.owner = owner;
            // Reuse current thread rather than submit to executor
            next.run();
        } else if(group != null) {
            group.onSubtaskFinish();
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
    public void increment(int parts) {
        tracker.increment(parts);
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
    <E extends Event> void addListener(E e, EventHandler<? super E> h) {
        events.addListener(e, h);
    }
    
    void cancel() {
        if(thread != null && tracker.getState() == State.RUNNING)
            thread.interrupt();
    }
    
    @Override
    public void checkCancel() throws InterruptedException {
        if(pollCancel())
            throw new InterruptedException("Cancelled");
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
    
}
