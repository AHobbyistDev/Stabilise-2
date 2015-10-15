package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.stabilise.util.concurrent.Waiter;

/**
 * A Task is essentially a {@code Runnable} with many other additional
 * facilities.
 * 
 * <p>To construct a task, use {@link TaskBuilder}.
 */
public class Task {
    
    static enum State {
        // There's no point in an UNSTARTED state for a task since a TaskUnit's
        // state variable doesn't come into scope until the task has begun.
        RUNNING, COMPLETED, FAILED;
    }
    
    private final TaskTracker tracker;
    private volatile TaskUnit curUnit;
    
    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    
    public Task(TaskTracker tracker, TaskUnit firstUnit) {
        this.tracker = tracker;
        this.curUnit = firstUnit;
    }
    
    Task start(Executor exec) {
        exec.execute(curUnit.setTask(this));
        return this;
    }
    
    void setCurrent(TaskUnit unit) {
        this.curUnit = unit;
    }
    
    void notifyOfComplete() {
        tracker.increment(1);
    }
    
    void setState(State state) {
        tracker.setState(state);
        signalAll();
    }
    
    private void signalAll() {
        lock.lock();
        try {
            cond.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
    public void cancel() {
        
    }
    
    public boolean stopped() {
        return tracker.getState() != State.RUNNING;
    }
    
    public boolean completed() {
        return tracker.getState() == State.COMPLETED;
    }
    
    public boolean failed() {
        return tracker.getState() == State.FAILED;
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
     * @return {@code true} if the task successfully completed; {@code false}
     * if it either failed or was cancelled.
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
     * 100 (inclusive).
     */
    public int percentCompleted() {
        return tracker.percentCompleted();
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
     * Returns the total number of parts in this task.
     * @return
     */
    public long getTotalParts() {
        return tracker.getTotalParts();
    }
    
    @Override
    public String toString() {
        return getStatus() + "... " + percentCompleted() + " ("
                + getPartsCompleted() + "/" + getTotalParts() + ")";
    }
    
}
