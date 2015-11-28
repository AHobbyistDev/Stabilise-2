package com.stabilise.util.concurrent.task;

import static com.stabilise.util.concurrent.task.Task.State;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.stabilise.util.Checks;
import com.stabilise.util.Log;

/**
 * A TaskTracker maintains the state of a task - status, parts completed, and
 * number of parts reported to the parent.
 */
class TaskTracker {
    
    static final long MIN_PARTS = 0;
    static final long MAX_PARTS = Long.MAX_VALUE - 1;
    static final String DEFAULT_STATUS = "Working";
    
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
    
    private final AtomicReference<State> state = new AtomicReference<>(State.RUNNING);
    
    
    TaskTracker(PrototypeTracker prototype) {
        status = prototype.status;
        totalParts = prototype.parts;
        partsToReport = prototype.partsToReport;
    }
    
    /** "Starts" this tracker. If totalParts is zero (technically 1, if you
     * include the completion part), this method sets totalParts to what {@link
     * TaskRunnable#getParts()} returns (unless it returns -1, in which case
     * this method does nothing). Runnable may be null. */
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
                totalParts = newParts + 1; // +1 for completion part
        }
    }
    
    /**
     * Increments the specified number of parts.
     * 
     * @throws IllegalArgumentException if p < 1
     */
    void increment(long p) {
        doIncrement(p, -1);
    }
    
    /**
     * Sets the parts count to the specified amount. Values are clamped to the
     * total parts.
     * 
     * @throws IllegalArgumentException if p < 0
     */
    void set(long p) {
        doSet(p, -1);
    }
    
    /** Sets the state, incrementing parts up to full if the state is
     * COMPLETED. This is not atomic. */
    void setState(State state) {
        if(state == State.COMPLETED)
            doIncrement(Long.MAX_VALUE, 0);
        this.state.set(state);
    }
    
    boolean setState(State expect, State update) {
        return state.compareAndSet(expect, update);
    }
    
    State getState() {
        return state.get();
    }
    
    /**
     * @param p parts to increment
     * @param c completion part. 0 if complete; -1 if not
     * 
     * @throws IllegalArgumentException if p < 1
     */
    private void doIncrement(long p, long c) {
        Checks.testMin(p, 1);
        State s = state.get();
        if(s != State.RUNNING)
            throw new IllegalStateException("Task isn't running!");
        long o, n; // old, new
        do {
            o = parts.get();
            if(o >= totalParts + c)
                return;
            n = o + p;
            if(n >= totalParts || n < 0) // overstep or overflow
                n = totalParts + c;
        } while(!parts.compareAndSet(o, n));
        
        reportToParent(n, c);
    }
    
    /**
     * 
     * @param n number of parts to set
     * @param c completion part. 0 if complete; -1 if not
     * 
     * @throws IllegalArgumentException if n < 0
     */
    private void doSet(long n, long c) {
        Checks.testMin(n, 0);
        if(n >= partsToReport + c)
            n = partsToReport + c;
        State s = state.get();
        if(s != State.RUNNING)
            throw new IllegalStateException("Task isn't running!");
        long o; // old
        do {
            o = parts.get();
            if(o == totalParts)
                return;
        } while(!parts.compareAndSet(o, n));
        
        reportToParent(n, c);
    }
    
    /**
     * @param n number of parts
     * @param c completion part. 0 if complete; -1 if not
     */
    private void reportToParent(long n, long c) {
        long o; // old
        // If we have a parent, we try to forward these parts (scaled
        // appropriately as per partsToReport) to our parent.
        if(parent != null) {
            // n and o now take on new/old values for reportedParts
            n = Math.max(0, (long)(n*((double)partsToReport/totalParts)) + c);
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
