package com.stabilise.util.concurrent.task;

import java.util.concurrent.atomic.AtomicLong;

import com.stabilise.util.Checks;

class TaskTracker2 {
    
    private static enum State {
        UNSTARTED, RUNNING, COMPLETED, FAILED;
    }
    
    private volatile String status;
    private final long totalParts;
    private final AtomicLong parts = new AtomicLong();
    
    TaskTracker2 parent = null;
    long partsToReport;
    private final AtomicLong reportedParts = new AtomicLong();
    
    public TaskTracker2(PrototypeTracker prototype) {
        status = prototype.status;
        totalParts = prototype.parts;
        partsToReport = prototype.partsToReport;
    }
    
    public void increment(long p) {
        doIncrement(p, 0);
    }
    
    public void complete() {
        doIncrement(Long.MAX_VALUE, 1);
    }
    
    /**
     * 
     * @param p parts to increment
     * @param c completion part. 1 if complete; 0 if not
     */
    private void doIncrement(long p, long c) {
        Checks.testMin(p, 1);
        long o, n; // old, new
        do {
            o = parts.get();
            n = o + p;
            if(n >= totalParts || n < 0) // overstep or overflow
                n = totalParts + (c - 1);
        } while(!parts.compareAndSet(o, n));
        
        // If we have a parent and we just successfully incremented our parts
        // count, we try to forward these parts (scaled appropriately as per
        // partsToReport) to our parent.
        if(parent != null && (n - o != 0)) {
            // n and o now take on new/old values for reportedParts
            n = (long)(n*((double)(partsToReport)/(totalParts)));
            do {
                o = reportedParts.get();
                // If o >= n, then either o == n and we have no additional
                // parts to report, or o > n and someone overtook us, in which
                // case their update encapsulates ours. In both cases, we can
                // go home.
                if(o >= n)
                    return;
            } while(!reportedParts.compareAndSet(o, n));
            parent.incrementFromChild(n - o); // forward the difference
        }
    }
    
    private void incrementFromChild(long p) {
        // Pretty much equivalent to 
        parts.getAndAdd(p);
    }
    
}
