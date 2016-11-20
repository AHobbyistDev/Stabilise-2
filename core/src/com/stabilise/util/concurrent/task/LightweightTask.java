package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;


/**
 * Lightweight Task implementation.
 */
class LightweightTask extends AbstractTask implements Runnable, TaskHandle {
    
    private final TaskRunnable task;
    
    private volatile boolean cancelled = false;
    private volatile Thread thread = null;
    
    
    public LightweightTask(Executor exec, TaskTracker tracker, TaskRunnable task) {
        super(exec, tracker);
        
        this.task = task;
    }
    
    @Override
    public Task start() {
        if(cancelled) {
            if(!tracker.setState(State.UNSTARTED, State.RUNNING))
                exec.execute(this);
            else
                throw new IllegalStateException("Already started!");
        }
        return this;
    }
    
    @Override
    public void run() {
        thread = Thread.currentThread();
        try {
            task.run(this);
            setState(State.COMPLETED);
        } catch(Throwable t) {
            setState(State.FAILED);
        }
    }
    
    private void setState(State state) {
        tracker.setState(state);
        signalAll();
    }
    
    @Override
    public TaskView[] getStack() {
        return new TaskView[] { this };
    }
    
    @Override
    public void cancel() {
        cancelled = true;
        if(tracker.getState() == State.RUNNING && thread != null)
            thread.interrupt();
    }
    
    // TaskHandle methods
    
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
    public void setTotal(long totalParts) {
        throw new IllegalStateException("Cannot reset total for lightweight task");
    }
    
    @Override
    public boolean pollCancel() {
        return cancelled;
    }
    
    @Override
    public void spawn(boolean parallel, TaskRunnable r) {
        throw new UnsupportedOperationException("Cannot spawn subtasks");
    }
    
    @Override
    public void beginFlatten() {
        throw new UnsupportedOperationException("Cannot spawn subtasks");
    }
    
    @Override
    public void endFlatten() {
        throw new UnsupportedOperationException("Cannot spawn subtasks");
    }
    
}
