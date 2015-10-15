package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;
import com.stabilise.util.concurrent.task.Task.State;


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
    
    protected final Executor executor;
    /** The thread upon which this unit is executing. This is set in run()
     * before this unit begins executing. Used as a target for interrupts. */
    private Thread thread;
    
    private final EventDispatcher events;
    
    
    /**
     * Creates a new task.
     * 
     * @param exec The executor with which to execute the task. Never null
     * thanks to TaskBuilder.
     * @param task The actual task to run. Never null thanks to TaskBuilder.
     * @param protoTracker The prototype for our tracker. Never null thanks to
     * TaskBuilder.
     */
    public TaskUnit(Executor exec, TaskRunnable task, PrototypeTracker protoTracker) {
        this.executor = exec;
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
        tracker.start(task);
        thread = Thread.currentThread();
        
        if(group == null) {
            owner.setCurrent(this);
        }
        
        boolean success = false;
        events.post(TaskEvent.START);
        try {
            success = execute();
        } catch(Exception e) {
            events.post(TaskEvent.STOP);
            events.post(TaskEvent.FAIL);
            owner.setState(State.FAILED);
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
        owner.notifyOfComplete();
        events.post(TaskEvent.STOP);
        events.post(TaskEvent.COMPLETE);
        if(next != null) {
            next.owner = owner;
            // Reuse current thread rather than submit to executor
            next.run();
        } else if(group == null) { // we're the last task
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
        thread.interrupt();
    }
    
    @Override
    public void checkCancel() throws InterruptedException {
        if(isTaskThread() && thread.isInterrupted()) {
            throw new InterruptedException();
        }
    }
    
    @Override
    public boolean pollCancel() {
        return isTaskThread() && thread.isInterrupted();
    }
    
    private boolean isTaskThread() {
        return Thread.currentThread().equals(thread);
    }
    
}
