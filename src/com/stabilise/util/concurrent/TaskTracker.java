package com.stabilise.util.concurrent;

import java.util.concurrent.atomic.AtomicLong;

import com.stabilise.util.annotation.ThreadSafe;

/**
 * A TaskTracker is designed to provide a means of communication between
 * threads as to the progress towards completion of a task. In a typical
 * implementation, worker threads simply invoke {@link #increment()} for each
 * 'part' of the task they complete.
 * 
 * <p>As is implied, this class is thread-safe.
 */
@ThreadSafe
public class TaskTracker implements Tracker {
    
    /** Task status. Never null. */
    private volatile String status;
    
    /** The state of the task, encoded as follows, from the highest bit to the
     * lowest bit:
     * 
     * <ul>
     * <li><b>1</b> - 1 if the task is done, 0 if not. A task which is done may
     *     not necessarily have been completed; for example, it may have been
     *     cancelled.
     * <li><b>2</b> - 1 if the task is completed, 0 if not.
     * <li><b>3-33</b> (31 bits) - the total number of parts in the task.
     * <li><b>34-64</b> (31 bits) - the number of parts which have been
     *     completed. Always less than the total number of parts.
     * </ul>
     * 
     * <p>Since the number of completed parts, and total number of parts,
     * cannot be negative, we make space for the two indicator bits by shaving
     * off their negation bits without sacrificing information.
     * 
     * <p>We also synchronise on this when updating both the state and status,
     * such that these changes are atomic from the perspective of {@link
     * #toString()}.
     */
    private final AtomicLong state;
    
    
    /**
     * Creates a new TaskTracker with 1 part to complete.
     * 
     * @param status The initial status of the task.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public TaskTracker(String status) {
        this(status, 1);
    }
    
    /**
     * Creates a new TaskTracker.
     * 
     * @param status The initial status of the task.
     * @param parts The number of parts to complete.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     * @throws IllegalArgumentException if {@code parts <= 0}.
     */
    public TaskTracker(String status, int parts) {
        if(status == null)
            throw new NullPointerException("status is null");
        if(parts <= 0)
            throw new IllegalArgumentException("parts <= 0");
        this.status = status;
        state = new AtomicLong(setParts(0L, parts));
    }
    
    /**
     * Sets the status of the task.
     * 
     * <p>Memory consistency effects: actions by a thread which sets the
     * status happen-before subsequent {@link #getStatus() reads} of the
     * status.
     * 
     * @param status The name.
     * 
     * @throws IllegalStateException if the task has already been completed.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public void setStatus(String status) {
        checkDone(state.get());
        if(status == null)
            throw new IllegalArgumentException("status is null!");
        this.status = status;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Memory consistency effects: actions by the thread which set the
     * status happen-before actions in the current thread.
     */
    @Override
    public String getStatus() {
        return status;
    }
    
    /**
     * Registers a part of the task as completed, as if by {@link
     * #increment(int) increment(1)}.
     * 
     * @throws IllegalStateException if the task has already been completed.
     */
    public void increment() {
        increment(1);
    }
    
    /**
     * Registers a specified number of parts of the task as completed. Negative
     * values are permitted, but the stored value is clamped to 0.
     * 
     * @throws IllegalStateException if the task is already done.
     */
    public void increment(int parts) {
        if(parts == 0)
            return;
        long s;
        int p, c;
        do {
            s = state.get();
            checkDone(s);
            p = extractParts(s);
            c = extractPartsCompleted(s) + parts;
            if(c < 0)
                c = parts < 0 ? 0 : p; // if parts > 0, there was an overflow
            else if(c > p)
                c = p;
        } while(!state.compareAndSet(s, setState(p, c)));
    }
    
    /**
     * Invokes {@link #increment()} and then {@link #setStatus(String)
     * setStatus(status)}.
     * 
     * @throws IllegalStateException if the task is already done.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public void next(String status) {
        next(1, status);
    }
    
    /**
     * Invokes {@link #increment(int) increment(parts)} and then {@link
     * #setStatus(String) setStatus(status)}.
     * 
     * @throws IllegalStateException if the task is already done.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public void next(int parts, String status) {
        synchronized(state) {
            increment(parts);
            setStatus(status);
        }
    }
    
    /**
     * Resets this tracker by setting the number of {@link #parts() parts} to
     * the specified value and the number of {@link #partsCompleted()
     * completed parts} to {@code 0}.
     * 
     * @param parts The number of parts to reset to.
     * 
     * @throws IllegalStateException if the task is already done.
     * @throws IllegalArgumentException if {@code parts <= 0}.
     */
    public void reset(int parts) {
        checkDone(state.get());
        if(parts <= 0)
            throw new IllegalArgumentException("parts <= 0");
        state.set(setState(parts, 0));
    }
    
    /**
     * Resets this tracker by setting the number of {@link #parts() parts} to
     * the specified value and the number of {@link #partsCompleted()
     * completed parts} to {@code 0}. This method also sets the status.
     * 
     * @param parts The number of parts to reset to.
     * @param status The new status.
     * 
     * @throws IllegalStateException if the task is already done.
     * @throws IllegalArgumentException if {@code parts <= 0}.
     */
    public void reset(int parts, String status) {
        synchronized(state) {
            reset(parts);
            setStatus(status);
        }
    }
    
    @Override
    public int parts() {
        return extractParts(state.get());
    }
    
    @Override
    public int partsCompleted() {
        return extractPartsCompleted(state.get());
    }
    
    @Override
    public float percentComplete() {
        long s = state.get();
        return (float)extractPartsCompleted(s) / extractParts(s);
    }
    
    /*
    private int percent() {
        long s = state.get();
        return 100 * extractPartsCompleted(s) / extractParts(s);
    }
    */
    
    private void checkDone(long state) {
        if(extractDone(state))
            throw new IllegalStateException("Task already done!");
    }
    
    /**
     * Sets the task as completed.
     * 
     * @throws IllegalStateException if the task is already done.
     */
    public void setCompleted() {
        Task.doThenNotify(this, () -> setCompletionStatus(true));
    }
    
    /**
     * Sets the task as failed.
     */
    public void setFailed() {
        Task.doThenNotify(this, () -> setCompletionStatus(false));
    }
    
    private void setCompletionStatus(boolean success) {
        long s;
        do {
            s = state.get();
            if(extractDone(s))
                throw new IllegalStateException("Task already done!");
        } while(!state.compareAndSet(s, setCompleted(s, success)));
    }
    
    @Override
    public boolean stopped() {
        return extractDone(state.get());
    }
    
    @Override
    public boolean completed() {
        return extractCompleted(state.get());
    }
    
    @Override
    public boolean failed() {
        return extractFailed(state.get());
    }
    
    @Override
    public void waitUntilDone() throws InterruptedException {
        Task.waitInterruptibly(this, () -> stopped());
    }
    
    @Override
    public void waitUninterruptibly() {
        Task.waitUntil(this, () -> stopped());
    }
    
    /**
     * Gets a string representation of this task tracker. The returned String
     * takes the form:
     * 
     * <blockquote>
     * {@code STATUS... X%}
     * </blockquote>
     * 
     * where STATUS is the status of the task, set either in the constructor or
     * by {@link #setStatus(String)}, and X is the percentage towards
     * completion of the task, equivalent to:
     * 
     * <blockquote>
     * {@code (int)(100*percentComplete())}
     * </blockquote>
     */
    @Override
    public String toString() {
        synchronized(state) {
            return status + "... " + ((int)(100*percentComplete())) + "%";
        }
    }
    
    private static boolean extractDone(long state) {
        return state < 0;
    }
    
    private static boolean extractCompleted(long state) {
        return (state & 0x40000000) == 0x40000000;
    }
    
    private static boolean extractFailed(long state) {
        return extractDone(state) && !extractCompleted(state);
    }
    
    private static int extractParts(long state) {
        return (int)(state >>> 31) & 0x7FFF;
    }
    
    private static int extractPartsCompleted(long state) {
        return (int)state & 0x7FFF;
    }
    
    private static long setParts(long state, int parts) {
        return (state & 0xC0007FFF) | (((long)parts) << 31);
    }
    
    private static long setPartsCompleted(long state, int partsCompleted) {
        return (state & 0xFFFF8000) | partsCompleted;
    }
    
    //private static long setDone(long state, boolean done) {
    //    return done ? state | 0x80000000 : state & 0x3FFFFFFF;
    //}
    
    /** Sets the done bit, and optionally sets the completed bit. If completed,
     * this also sets partsCompleted = parts. */
    private static long setCompleted(long state, boolean completed) {
        // Invoking this method implies done, so we set done in either case
        return completed
                ? setPartsCompleted(state, extractParts(state)) | 0xC0000000
                : state | 0x80000000;
    }
    
    private static long setState(int parts, int partsCompleted) {
        return (((long)parts) << 31) | partsCompleted;
    }
    
}
