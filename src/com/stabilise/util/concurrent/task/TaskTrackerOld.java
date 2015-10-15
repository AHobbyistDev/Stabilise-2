package com.stabilise.util.concurrent.task;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.stabilise.util.Checks;
import com.stabilise.util.ObjectIntFunction;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.concurrent.Tasks;

/**
 * A TaskTracker is designed to provide a means of communication between
 * threads as to the progress towards completion of a task. In a typical
 * implementation, worker threads simply invoke {@link #increment()} for each
 * 'part' of the task they complete.
 * 
 * <p>As is implied, this class is thread-safe.
 */
@ThreadSafe
@Deprecated
class TaskTrackerOld {
    
    private static final long MASK_DONE            = 0x8000000000000000L;
    private static final long MASK_COMPLETED       = 0x4000000000000000L;
    private static final long MASK_PARTS           = 0x3FFFFFFF80000000L;
    private static final long MASK_PARTS_COMPLETED = 0x000000007FFFFFFFL;
    private static final long MASK_UNSTARTED       = MASK_DONE | MASK_COMPLETED;
    private static final long VAL_UNSTARTED        = MASK_COMPLETED;
  //private static final int SHIFT_DONE            = 63;
  //private static final int SHIFT_COMPLETED       = 62;
    private static final int SHIFT_PARTS           = 31;
  //private static final int SHIFT_PARTS_COMPLETED = 0;
    
    /** Task status. Never null. */
    private volatile String status;
    
    /** The state of the task, encoded as follows, from the highest bit to the
     * lowest bit:
     * 
     * <ul>
     * <li><b>1</b> (1 bit) - if the task is done, 0 if not. A task which is
     *     done may not necessarily have been completed; for example, it may
     *     have been cancelled.
     * <li><b>2</b> (1 bit) - if the task is completed, 0 if not.
     * <li><b>3-33</b> (31 bits) - the total number of parts in the task.
     * <li><b>34-64</b> (31 bits) - the number of parts which have been
     *     completed. Always less than the total number of parts.
     * </ul>
     * 
     * <p><b>Note</b>: There is a <u>special case</u> for the first two bits -
     * {@code 01} - which indicates a task has not yet started. This value is
     * taken as it would otherwise be left as an ordinarily-invalid state
     * (completed but not done).
     * 
     * <p>Since the number of completed parts and total number of parts
     * cannot be negative, we make space for the two indicator bits by shaving
     * off their negation (most significant) bits without sacrificing
     * information.
     * 
     * <p>We also synchronise on this when updating both the state and status,
     * such that these changes are atomic from the perspective of {@link
     * #toString()}.
     */
    private final AtomicLong state;
    
    /** This tracker's parent. When this tracker increments the parts
     * completed, the parent has that many incremented too. May be null.*/
    private final TaskTrackerOld parent;
    
    
    /**
     * Creates a new TaskTracker.
     * 
     * @param parts The number of parts to complete. This value is
     * unrestricted, so checking must be done by TaskBuilder.
     * @param status The initial status of the task.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public TaskTrackerOld(int parts, String status) {
        this(status, parts, null);
    }
    
    private TaskTrackerOld(String status, int parts, TaskTrackerOld parent) {
        this.status = Objects.requireNonNull(status);
        state = new AtomicLong(setParts(VAL_UNSTARTED, parts));
        this.parent = parent;
    }
    
    /**
     * Creates a TaskTracker which is a child of this one. The number of parts
     * specified will be added to this tracker's total parts. Whenever the
     * child has its completed parts incremented, this tracker will be
     * incremented too.
     * 
     * <p>Due to the possibility of a race condition, adding a child tracker
     * once this one has been started is not permitted, and an {@code
     * IllegalStateException} will be thrown.
     * 
     * @param status The initial status of the task.
     * @param parts The number of parts to complete.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     * @throws IllegalArgumentException if {@code parts <= 0}.
     * @throws IllegalStateException if this task has already been started.
     */
    TaskTrackerOld child(String status, int parts) {
        TaskTrackerOld child = new TaskTrackerOld(status, parts, this);
        addParts(parts);
        return child;
    }
    
    /**
     * Adds to the total number of parts to this task and its parent, if
     * applicable.
     * 
     * @throws IllegalStateException if the task or its parent has been
     * started.
     */
    private void addParts(int parts) {
        Checks.testMin(parts, 1);
        // We don't need to do a CAS loop since TaskBuilder ensures this method
        // is only ever invoked when a Task is being built by a single thread.
        /*
        long s, n;
        int p;
        do {
            s = state.get();
            if(extractStarted(state.get()))
                throw new IllegalStateException("Task already started!");
            p = extractParts(s) + parts;
            if(p < 0) // overflow, so we cap at MAX_VALUE
                p = Integer.MAX_VALUE;
            n = setParts(s, p);
        } while(!state.compareAndSet(s, n));
        */
        long s = state.get();
        int p = extractParts(s) + parts;
        if(p < 0) // overflow, so we cap at MAX_VALUE
            p = Integer.MAX_VALUE;
        state.set(setParts(s, p));
        if(parent != null)
            parent.addParts(parts);
    }
    
    /**
     * Begins tracking the task.
     * 
     * @throws IllegalStateExeption if the task has already been started.
     */
    public void start() {
        long s = state.get();
        if(extractStarted(s) || !state.compareAndSet(s, setStarted(s)))
            throw new IllegalStateException("Already started!");
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
     * @throws IllegalStateException if the task hasn't started or has already
     * been stopped.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public void setStatus(String status) {
        checkRunning(state.get());
        this.status = Objects.requireNonNull(status);
    }
    
    /**
     * Returns the status of the task.
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Registers a part of the task as completed, as if by {@link
     * #increment(int) increment(1)}.
     * 
     * @throws IllegalStateException if the task hasn't started or has already
     * been stopped.
     */
    public void increment() {
        increment(1);
    }
    
    /**
     * Registers a specified number of parts of the task as completed.
     * 
     * @throws IllegalArgumentException if {@code parts <= 0}.
     * @throws IllegalStateException if the task hasn't started or has already
     * been stopped.
     */
    public void increment(int parts) {
        updateParent(doIncrement(parts, true));
    }
    
    /**
     * Increments the number of parts completed.
     * 
     * @param parts Number by which to increment partsCompleted
     * @param check if true, this method will throw an IAE if parts < 1. Also,
     * {@link #checkRunning(long)} will be checked.
     * 
     * @return The number of parts which were successfully incremented, in the
     * range [0,parts].
     * @throws IllegalArgumentException if {@code parts <= 0} (if check is
     * true).
     * @throws IllegalStateException if the task hasn't started or has already
     * been stopped (if check is true).
     */
    private int doIncrement(int parts, boolean check) {
        if(check)
            Checks.testMin(parts, 1);
        long s;
        int p, c, u; // parts, completed, updated
        do {
            s = state.get();
            if(check)
                checkRunning(s);
            p = extractParts(s);
            c = u = extractPartsCompleted(s);
            c += parts;
            if(c < 0 || c > p)
                c = p;
            u = c - u;
        } while(!state.compareAndSet(s, setPartsCompleted(s, c)));
        return u;
    }
    
    /**
     * Invokes {@link #increment()} and then {@link #setStatus(String)
     * setStatus(status)}.
     * 
     * @throws IllegalStateException if the task hasn't started or has already
     * been stopped.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public void next(String status) {
        next(1, status);
    }
    
    /**
     * Invokes {@link #increment(int) increment(parts)} and then {@link
     * #setStatus(String) setStatus(status)}.
     * 
     * @throws IllegalArgumentException if {@code parts <= 0}.
     * @throws IllegalStateException if the task hasn't started or has already
     * been stopped.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public void next(int parts, String status) {
        int u;
        synchronized(state) {
            u = doIncrement(parts, true);
            setStatus(status);
        }
        updateParent(u, status);
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
    /*
    public void reset(int parts) {
        checkDone(state.get());
        state.set(setState(Checks.testMin(parts, 1), 0));
    }
    */
    
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
    /*
    public void reset(int parts, String status) {
        synchronized(state) {
            reset(parts);
            setStatus(status);
        }
    }
    */
    
    /**
     * Returns the total number of parts in the task, which is always greater
     * than 0.
     */
    public int parts() {
        return extractParts(state.get());
    }
    
    /**
     * Returns the number of parts of the task which have been completed. This
     * is always greater than 0, and less than or equal to {@link #parts()}.
     */
    public int partsCompleted() {
        return extractPartsCompleted(state.get());
    }
    
    /**
     * Returns the completion percentage of the task, from 0 to 100.
     * 
     * <p>This method is the atomic equivalent of {@code 100 * partsCompleted()
     * / parts()}.
     */
    public int percent() {
        long s = state.get();
        return 100 * extractPartsCompleted(s) / extractParts(s);
    }
    
    /**
     * Returns the floating-point completion percentage of the task, from 0
     * to 1.
     * 
     * <p>This method is the atomic equivalent of {@code (float)
     * partsCompleted() / parts()}.
     */
    public float percentFloat() {
        long s = state.get();
        return (float)extractPartsCompleted(s) / extractParts(s);
    }
    
    /**
     * @throws IllegalStateException if unstarted or done
     */
    private void checkRunning(long state) {
        if(extractUnstarted(state))
            throw new IllegalStateException("Task hasn't started!");
        checkDone(state);
    }
    
    /**
     * @throws IllegalStateException if done
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
        Tasks.doThenNotify(this, () -> setCompletionStatus(true));
    }
    
    /**
     * Sets the task as failed.
     */
    public void setFailed() {
        Tasks.doThenNotify(this, () -> setCompletionStatus(false));
    }
    
    private void setCompletionStatus(boolean success) {
        long s;
        do {
            s = state.get();
            checkRunning(s);
        } while(!state.compareAndSet(s, setCompleted(s, success)));
        updateParent(doIncrement(Integer.MAX_VALUE, false));
    }
    
    /**
     * Checks for whether or not the task has been started.
     */
    public boolean started() {
        return extractStarted(state.get());
    }
    
    /**
     * Checks for whether or not the task is currently running.
     */
    boolean running() {
        long s = state.get();
        return extractStarted(s) && !extractDone(s);
    }
    
    /**
     * Checks for whether or not the task has stopped. This result encapsulates
     * the two possible end states: {@link #completed() completion} and {@link
     * #failed() failure}.
     * 
     * @return {@code true} if the task is done; {@code false} otherwise.
     */
    public boolean stopped() {
        return extractDone(state.get());
    }
    
    /**
     * Checks for whether or not the task has been completed. If this returns
     * {@code true}, then {@link #stopped()} will return true too. Note that
     * the returned value is <i>not</i> strictly equivalent to the expression
     * {@code partsCompleted == parts()}.
     * 
     * @return {@code true} if the task has been completed; {@code false} if it
     * has not.
     */
    public boolean completed() {
        return extractCompleted(state.get());
    }
    
    /**
     * Checks for whether or not the task has failed. If this returns {@code
     * true}, then {@link #stopped()} will return true too.
     * 
     * @return {@code true} if the task has failed; {@code false} otherwise.
     */
    public boolean failed() {
        return extractFailed(state.get());
    }
    
    /**
     * Blocks the current thread until the task is done, either due to
     * completion or failure.
     * 
     * @throws InterruptedException if the current thread is interrupted while
     * waiting.
     */
    public void waitUntilStopped() throws InterruptedException {
        Tasks.waitInterruptibly(this, () -> stopped());
    }
    
    /**
     * Blocks the current thread until the task is done, either due to
     * completion or failure.
     * 
     * <p>If the current thread received an interrupt while waiting, the
     * interrupt flag will be set when this method returns.
     */
    public void waitUninterruptibly() {
        Tasks.waitUntil(this, () -> stopped());
    }
    
    protected void updateParent(int parts) {
        if(parent != null)
            parent.increment(parts);
    }
    
    protected void updateParent(int parts, String status) {
        if(parent != null)
            parent.next(parts, status);
    }
    
    /**
     * Gets a string representation of this task tracker. The returned String
     * is given as if by:
     * 
     * <pre>return getStatus() + "... " + percent() + "%";</pre>
     */
    @Override
    public String toString() {
        return toString((s,p) -> s + "... " + p + "%");
    }
    
    /**
     * Produces a string representation of the task using the supplied
     * function in a thread-safe manner. This method is provided as to allow
     * users to create custom string representations using {@link #getStatus()}
     * and {@link #percent()} without needing to worry about the inherent race
     * in invoking them separately.
     * 
     * @param func The function with which to produce the string output. The
     * first argument of the function is given by {@link #getStatus()}, and the
     * second by {@link #percent()}.
     * 
     * @return The string returned by {@code func}.
     */
    public String toString(ObjectIntFunction<String, String> func) {
        synchronized(state) {
            return func.apply(status, percent());
        }
    }
    
    // Static functions -------------------------------------------------------
    
    private static boolean extractUnstarted(long state) {
        return (state & MASK_UNSTARTED) == VAL_UNSTARTED;
    }
    
    private static boolean extractStarted(long state) {
        return !extractUnstarted(state);
    }
    
    private static boolean extractDone(long state) {
        return (state & MASK_DONE) == MASK_DONE;
    }
    
    private static boolean extractCompleted(long state) {
        return (state & MASK_COMPLETED) == MASK_COMPLETED
                && extractStarted(state);
    }
    
    private static boolean extractFailed(long state) {
        return extractDone(state) && !extractCompleted(state);
    }
    
    private static int extractParts(long state) {
        return (int)((state & MASK_PARTS) >>> SHIFT_PARTS);
    }
    
    private static int extractPartsCompleted(long state) {
        return (int)(state & MASK_PARTS_COMPLETED);
    }
    
    private static long setStarted(long state) {
        // We simply switch from the special unstarted state 01 to the ordinary
        // running state 00.
        return state & ~MASK_UNSTARTED;
    }
    
    private static long setParts(long state, int parts) {
        return (state & ~MASK_PARTS) | (((long)parts) << SHIFT_PARTS);
    }
    
    private static long setPartsCompleted(long state, int partsCompleted) {
        return (state & ~MASK_PARTS_COMPLETED) | partsCompleted;
    }
    
    /** Sets the done bit, and optionally sets the completed bit. */
    private static long setCompleted(long state, boolean completed) {
        return completed
                ? state | MASK_DONE | MASK_COMPLETED
                : state | MASK_DONE;
    }
    
    /*
    private static long setState(int parts, int partsCompleted) {
        return ((long)parts << SHIFT_PARTS) |
                ((long)partsCompleted << SHIFT_PARTS_COMPLETED);
    }
    */
    
}
