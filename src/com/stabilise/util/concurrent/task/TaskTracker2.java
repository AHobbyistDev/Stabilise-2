package com.stabilise.util.concurrent.task;

import java.util.concurrent.atomic.AtomicLong;

class TaskTracker2 {
    
    private static enum State {
        UNSTARTED, RUNNING, COMPLETED, FAILED;
    }
    
    private volatile String status;
    private long totalParts;
    private final AtomicLong parts = new AtomicLong();
    
    private TaskTracker2 parent;
    private double scaling;
    private long reportedParts;
    
    public TaskTracker2() {
        
    }
    
}
