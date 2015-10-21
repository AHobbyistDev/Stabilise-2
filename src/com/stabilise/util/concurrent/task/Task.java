package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.concurrent.Waiter;

/**
 * A Task is essentially a {@code Runnable} with other additional facilities.
 * 
 * <p>The main entry point for creating a Task is {@link #builder()}.
 */
@ThreadSafe
public class Task {
    
    static enum State {
        // There's no point in an UNSTARTED state for a task since a TaskUnit's
        // state variable doesn't come into scope until the task has begun.
        RUNNING, CANCELLED, COMPLETED, FAILED;
    }
    
    private final Executor exec;
    
    private final TaskTracker tracker;
    private TaskUnit curUnit;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    private final Lock      lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    
    private final AtomicReference<Throwable> failCause = new AtomicReference<>(null);
    
    
    /**
     * Instantiated only by TaskBuilder.
     */
    Task(Executor exec, TaskTracker tracker, TaskUnit firstUnit) {
        this.exec = exec;
        this.tracker = tracker;
        this.curUnit = firstUnit;
    }
    
    /**
     * Starts this task, unless it has been preemptively cancelled.
     * 
     * @return This task.
     * @throws IllegalStateException if this task has already been started.
     */
    public Task start() {
        if(getState() != State.CANCELLED) {
            if(started.compareAndSet(false, true))
                exec.execute(curUnit.setTask(this));
            else
                throw new IllegalStateException("Already started");
        }
        return this;
    }
    
    /**
     * Notifies this task that the specified unit is now the current top-level
     * one.
     * 
     * @return true if the new unit may run; false if this task has been
     * cancelled.
     */
    boolean setCurrent(TaskUnit unit) {
        synchronized(this) {
            if(cancelled())
                return false;
            this.curUnit = unit;
        }
        return true;
    }
    
    void setState(State state) {
        tracker.setState(state);
        signalAll();
    }
    
    State getState() {
        return tracker.getState();
    }
    
    private void signalAll() {
        lock.lock();
        try {
            cond.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    void fail(TaskUnit failurePoint) {
        failCause.compareAndSet(null, failurePoint.getFailCause());
        setState(State.FAILED);
    }
    
    /**
     * Cancels this task. The speed at which a Task actually stops following a
     * cancellation request depends on the responsiveness of an implementation,
     * but it is guaranteed that a new task unit will not begin following an
     * invocation of this method. A Task which stops due to cancellation is
     * considered to have {@link #failed() failed}.
     */
    public void cancel() {
        if(tracker.setState(State.RUNNING, State.CANCELLED)) {
            signalAll();
            synchronized(this) {
                curUnit.cancel();
            }
        }
    }
    
    /** Polls cancellation status. */
    boolean cancelled() {
        return tracker.getState() == State.CANCELLED;
    }
    
    /**
     * Returns {@code true} if this task has stopped, either due to completion
     * or failure; {@code false} otherwise.
     * 
     * <p>An invocation of this is the atomic equivalent to {@code completed()
     * || failed()}.
     */
    public boolean stopped() {
        return tracker.getState() != State.RUNNING;
    }
    
    /**
     * Returns {@code true} if this task has been successfully completed;
     * {@code false} otherwise.
     * 
     * <p>An invocation of this is the atomic equivalent to {@code stopped()
     * && !failed()}.
     */
    public boolean completed() {
        return tracker.getState() == State.COMPLETED;
    }
    
    /**
     * Returns {@code true} if this task has failed or was cancelled; {@code
     * false} otherwise.
     * 
     * <p>An invocation of this is the atomic equivalent to {@code stopped()
     * && !completed()}.
     */
    public boolean failed() {
        State s = tracker.getState();
        return s == State.CANCELLED || s == State.FAILED;
    }
    
    /**
     * Blocks the current thread until either the task has finished executing,
     * or the current thread is interrupted.
     * 
     * @return {@code true} if the task successfully completed; {@code false}
     * if it either failed or was cancelled.
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     */
    public boolean await() throws InterruptedException {
        lock.lock();
        try {
            while(!stopped())
                cond.await();
        } finally {
            lock.unlock();
        }
        return completed();
    }
    
    /**
     * Blocks the current thread until the task has finished executing. If the
     * current thread was interrupted while waiting, the interrupt flag will be
     * set when this method returns.
     * 
     * @return {@code true} if the task successfully completed; {@code false}
     * if it either failed or was cancelled.
     */
    public boolean awaitUninterruptibly() {
        lock.lock();
        try {
            while(!stopped())
                cond.awaitUninterruptibly();
        } finally {
            lock.unlock();
        }
        return completed();
    }
    
    /**
     * Blocks the current thread until either the task has finished executing,
     * the current thread is interrupted, or the specified waiting time
     * elapses.
     * 
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @return {@code true} if the task {@link #stopped() stopped}; {@code
     * false} if the specified waiting time elapsed.
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     */
    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            return cond.await(time, unit);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Creates a Waiter for this task.
     * 
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws NullPointerException if {@code unit} is {@code null}.
     */
    public Waiter waiter(long time, TimeUnit unit) {
        return new Waiter(() -> stopped(), time, unit);
    }
    
    /**
     * Gets the status of this task.
     */
    public String getStatus() {
        return tracker.getStatus();
    }
    
    /**
     * Returns the fraction of this task which has been completed, from 0 to 1
     * (inclusive).
     */
    public double fractionCompleted() {
        return tracker.fractionCompleted();
    }
    
    /**
     * Returns the percentage of this task which has been completed, from 0 to
     * 100 (inclusive). Equivalent to {@code (int)(100 * fractionCompleted())}.
     */
    public int percentCompleted() {
        return (int)(100 * fractionCompleted());
    }
    
    /**
     * Returns the number of parts of this task which have been marked as
     * completed. This is always less than or equal to the value returned by
     * {@link #getTotalParts()}.
     */
    public long getPartsCompleted() {
        return tracker.getPartsCompleted();
    }
    
    /**
     * Returns the total number of parts in this task. This value never
     * changes.
     */
    public long getTotalParts() {
        return tracker.getTotalParts();
    }
    
    /**
     * Returns a string representation of this task.
     * 
     * <p>This implementation behaves as if by:
     * 
     * <pre>
     * return getStatus() + "... " + percentCompleted() + "% ("
     *         + getPartsCompleted() + "/" + getTotalParts() + ")";
     * </pre>
     * 
     * which returns a string of the form:
     *
     * <pre>"Working... 50% (256/512)"</pre>
     */
    @Override
    public String toString() {
        return getStatus() + "... " + percentCompleted() + "% ("
                + getPartsCompleted() + "/" + getTotalParts() + ")";
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates a new TaskBuilderBuilder. This function is the main entry-point
     * for creating a Task.
     */
    public static TaskBuilderBuilder builder() {
        return new TaskBuilderBuilder();
    }
    
    /**
     * Creates a new TaskBuilderBuilder.
     * 
     * @param executor The executor with which to run the task.
     * 
     * @throws NullPointerException if {@code executor} is {@code null}.
     */
    public static TaskBuilderBuilder builder(Executor executor) {
        return builder().executor(executor);
    }
    
}
