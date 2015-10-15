package com.stabilise.util.concurrent.task;

import static com.stabilise.util.concurrent.task.Task.State;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.stabilise.util.Checks;
import com.stabilise.util.Log;

class TaskTracker {
    
    static final long MIN_PARTS = 0;
    static final long MAX_PARTS = Long.MAX_VALUE - 1;
    static final String DEFAULT_STATUS = "Working";
    private static final long PERC_CUTOFF = Long.MAX_VALUE / 100;
    
    private volatile String status;
    /** Total number of parts. Includes the completion part. */
    private long totalParts;
    private final AtomicLong parts = new AtomicLong();
    
    /** The tracker of our task's parent. This is only ever null if we are the
     * root task. */
    private TaskTracker parent = null;
    /** Total number of parts to report. Includes the completion part. */
    private long partsToReport;
    /** Number of parts which we've reported so far. Unused if parent is null. */
    private final AtomicLong reportedParts = new AtomicLong();
    
    private volatile State state = State.RUNNING;
    
    
    TaskTracker(PrototypeTracker prototype) {
        status = prototype.status;
        totalParts = prototype.parts;
        partsToReport = prototype.partsToReport;
    }
    
    void start(TaskRunnable runnable) {
        // i.e. only accept the updated value if we lack a proper initial
        // value (that is, totalParts == 1).
        if(totalParts != 1 && runnable != null) {
            long newParts = runnable.getParts();
            if(newParts == -1)
                return;
            else if(newParts < MIN_PARTS || newParts > MAX_PARTS)
                Log.getAgent("Task").postWarning("TaskRunnable for task \""
                        + status + "\" returned illegal value from getParts() ("
                        + newParts + ")");
            else
                totalParts = newParts;
        }
    }
    
    /**
     * Increments the specified number of parts.
     * 
     * @throws IllegalArgumentException if p < 1
     */
    void increment(long p) {
        doIncrement(p, 0);
    }
    
    void setState(State state) {
        if(state == State.COMPLETED)
            doIncrement(Long.MAX_VALUE, 1);
        this.state = state;
    }
    
    State getState() {
        return state;
    }
    
    /**
     * @param p parts to increment
     * @param c completion part. 1 if complete; 0 if not
     * 
     * @throws IllegalArgumentException if p < 1
     */
    private void doIncrement(long p, long c) {
        Checks.testMin(p, 1);
        if(state != State.RUNNING)
            throw new IllegalStateException();
        long o, n; // old, new
        c--; // since otherwise we'd use c-1 everywhere
        do {
            o = parts.get();
            if(o == totalParts + c)
                return;
            n = o + p;
            if(n >= totalParts || n < 0) // overstep or overflow
                n = totalParts + c;
        } while(!parts.compareAndSet(o, n));
        
        // If we have a parent, we try to forward these parts (scaled
        // appropriately as per partsToReport) to our parent.
        if(parent != null) {
            // n and o now take on new/old values for reportedParts
            n = Math.max(0, (long)(n*((double)(partsToReport)/(totalParts))) + c);
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
        // Pretty much equivalent to doIncrement(), but without unnecessary
        // checks, as we're guaranteed parts provided by a child are safe.
        parts.getAndAdd(p);
    }
    
    void setParent(TaskTracker parent) {
        this.parent = parent;
    }
    
    void setPartsToReport(long partsToReport) {
        this.partsToReport = partsToReport;
    }
    
    /**
     * @throws NullPointerException if status is null
     */
    void setStatus(String status) {
        this.status = Objects.requireNonNull(status);
    }
    
    String getStatus() {
        return status;
    }
    
    /**
     * Returns the fraction of parts completed, from 0 to 1 (inclusive).
     */
    double fractionCompleted() {
        return (double)parts.get() / totalParts;
    }
    
    /**
     * Returns the percentage of parts completed, from 0-100 (inclusive).
     */
    int percentCompleted() {
        long p = parts.get();
        // Cutoff required since 100*Long.MAX_VALUE won't end well.
        // A flat "(int)(100 * fractionCompleted())" may work for all values,
        // but I'm too lazy to test :P
        if(p > PERC_CUTOFF)
            return (int)(100 * fractionCompleted());
        else
            return (int)(100 * p / totalParts);
    }
    
    /**
     * Returns the number of parts of this task which have been marked as
     * completed. This is always less than or equal to the value returned by
     * {@link #getTotalParts()}.
     */
    long getPartsCompleted() {
        return parts.get();
    }
    
    /**
     * Returns the total number of parts in this task.
     */
    long getTotalParts() {
        return totalParts;
    }
    
}
