package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.stabilise.util.concurrent.Waiter;

/**
 * A Task is essentially a {@code Runnable} with other additional facilities.
 * 
 * <p>The main entry point for creating a Task is {@link #builder()}.
 */
@ThreadSafe
public class Task implements TaskView {
    
    static enum State {
        // There's no point in an UNSTARTED state for a task since a TaskUnit's
        // state variable doesn't come into play until the task has begun.
        RUNNING, COMPLETED, FAILED;
    }
    
    private final Executor exec;
    
    @GuardedBy("this") private TaskUnit curUnit;
    
    private final TaskTracker tracker;
    private final AtomicBoolean started      = new AtomicBoolean(false);
    private final AtomicBoolean cancelled    = new AtomicBoolean(false);
    private final AtomicInteger unitsRunning = new AtomicInteger(0);
    
    private final Lock      lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    
    protected final AtomicReference<Throwable> failCause = new AtomicReference<>();
    
    
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
        if(!cancelled.get()) {
            if(started.compareAndSet(false, true))
                exec.execute(curUnit.setTask(this));
            else
                throw new IllegalStateException("Already started");
        }
        return this;
    }
    
    void onUnitStart() {
        unitsRunning.getAndIncrement();
    }
    
    void onUnitStop() {
        if(unitsRunning.decrementAndGet() == 0 && cancelled.get()) {
            setState(State.FAILED);
        }
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
            return true;
        }
    }
    
    /**
     * Sets the state and wakes all threads waiting in {@link #await()}, etc.
     */
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
        // We'll restrict ourselves to a single failure cause since one task
        // failing can set off others failing too, and their failures are
        // redundant.
        failCause.compareAndSet(null, failurePoint.getFailCause());
        
        // If the current unit is a group and one of its subtasks failed, we'll
        // need to throw out a cancellation to notify the rest of them to stop.
        cancelled.set(true);
        curUnit.cancel();
        
        // We don't invoke setState(State.FAILED) because we want to wait for
        // all currently-running tasks to finish up before notify anyone
        // waiting on await(), etc. So we use onUnitStop() for this purpose.
    }
    
    /**
     * Cancels this task. The speed at which a Task actually stops following a
     * cancellation request depends on the responsiveness of an implementation,
     * but it is guaranteed that a new task unit will not begin following an
     * invocation of this method. A Task which stops due to cancellation is
     * considered to have {@link #failed() failed}.
     */
    public void cancel() {
        if(!stopped() && cancelled.compareAndSet(false, true)) {
            synchronized(this) {
                curUnit.cancel();
            }
        }
    }
    
    /** Polls cancellation status. */
    boolean cancelled() {
        return cancelled.get();
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
        return new Waiter(this::stopped, time, unit);
    }
    
    @Override
    public String status() {
        return tracker.getStatus();
    }
    
    @Override
    public double fractionCompleted() {
        return tracker.fractionCompleted();
    }
    
    @Override
    public long partsCompleted() {
        return tracker.getPartsCompleted();
    }
    
    @Override
    public long totalParts() {
        return tracker.getTotalParts();
    }
    
    /**
     * Returns a view of the current subtask.
     */
    public synchronized TaskView curSubtask() {
        return curUnit;
    }
    
    /**
     * Returns a string representation of this task.
     * 
     * <p>This implementation behaves as if by:
     * 
     * <pre>
     * return getStatus() + "... " + percentCompleted();
     * </pre>
     * 
     * which returns a string of the form:
     *
     * <pre>"Working... 50%"</pre>
     */
    @Override
    public String toString() {
        return status() + "... " + percentCompleted() + "%";
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
