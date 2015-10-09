package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;


public class Task {
    
    final TaskTracker tracker;
    private TaskUnit unit;
    
    public Task(TaskTracker tracker, TaskUnit firstUnit) {
        this.tracker = tracker;
        this.unit = firstUnit;
    }
    
    Task start(Executor exec) {
        exec.execute(unit);
        return this;
    }
    
    public void cancel() {
        
    }
    
    public void await() throws InterruptedException {
        
    }
    
    public void awaitUninterruptibly() {
        
    }
    
}
