package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class TaskUnit implements Runnable, TaskHandle {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    protected static final Executor EXEC_CURR_THREAD = r -> r.run();
    
    /**
     * Different states the task may occupy.
     */
    protected static enum TaskState {
        /** Indicates that the task has yet to be performed. */
        UNSTARTED,
        /** Indicates that the task is currently being performed. */
        RUNNING,
        /** Indicates that the task has stopped. */
        STOPPED,
        /** Indicates that the task has completed successfully, and
         * has stopped running. */
        COMPLETED;
    };
    
    public enum StopType {
        CANCEL, EXCEPTION;
    }
    
    protected final AtomicReference<TaskState> state =
            new AtomicReference<>(TaskState.UNSTARTED);
    
    protected final TaskTracker tracker;
    private boolean partsSpecified;
    
    protected TaskUnit parent = null;
    protected TaskUnit next = null;
    
    protected TaskRunnable task = null;
    
    protected final Executor executor;
    
    protected final Lock doneLock = new ReentrantLock();
    protected final Condition doneCondition = doneLock.newCondition();
    
    
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
    public TaskUnit(Executor exec, String status, int parts) {
        this.executor = exec;
        this.tracker = new TaskTracker(status, parts);
    }
    
    @Override
    public void run() {
        boolean success = false;
        try {
            success = execute();
        } catch(Exception e) {
            // press f to pay respects
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
            next.parent = this;
            next.run();
        } else {
            // f
        }
    }
    
}
