package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;
import com.stabilise.util.concurrent.event.EventDispatcher.EventHandler;


class TaskUnit implements Runnable, TaskHandle {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    protected final TaskTracker tracker;
    private boolean partsSpecified;
    
    protected Task owner = null;
    protected TaskGroup group = null;
    protected TaskUnit next = null;
    
    protected TaskRunnable task = null;
    
    protected final Executor executor;
    private Thread thread;
    
    protected final Lock doneLock = new ReentrantLock();
    protected final Condition doneCondition = doneLock.newCondition();
    
    private final EventDispatcher events;
    
    
    /**
     * Creates a new task.
     * 
     * @param exec The executor with which to execute the task. This is never
     * null thanks to {@link TaskBuilder}.
     * @param status The initial task status.
     * @param parts Initial task parts.
     * 
     * @throws NullPointerException if either {@code exec} or {@code status}
     * are {@code null}.
     * @throws IllegalArgumentException if {@code parts <= 0}.
     */
    public TaskUnit(Executor exec, TaskRunnable task, String status, int parts,
            boolean partsSpecified) {
        this.executor = exec;
        this.task = task;
        this.tracker = new TaskTracker(parts, status);
        this.events = new EventDispatcher(exec);
    }
    
    @Override
    public void run() {
        thread = Thread.currentThread();
        
        boolean success = false;
        events.post(TaskEvent.START);
        try {
            success = execute();
        } catch(Exception e) {
            // press f to pay respects
            events.post(TaskEvent.STOP);
            events.post(TaskEvent.FAIL);
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
        if(next != null) {
            // Reuse current thread rather than submit to executor
            next.run();
        } else {
            // f
        }
    }
    
    @Override
    public void setStatus(String status) {
        
    }
    
    @Override
    public void increment(int parts) {
        if(partsSpecified) {
            tracker.increment(parts);
        }
    }
    
    @Override
    public void next(int parts, String status) {
        if(partsSpecified) {
            tracker.next(parts, status);
        }
    }
    
    @Override
    public void post(Event e) {
        events.post(e);
    }
    
    public <E extends Event> void addListener(E e, EventHandler<? super E> h) {
        events.addListener(e, h);
    }
    
}
