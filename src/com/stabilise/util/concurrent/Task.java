package com.stabilise.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.function.BooleanSupplier;

import com.stabilise.util.annotation.ThreadSafe;

/**
 * A Task is a wrapper for a {@code Runnable} with some additional facilities.
 * 
 * <p>Unlike a {@code Runnable}, subclasses should override {@link #execute()}
 * instead of {@link #run()} when implementing code. Furthermore, it is advised
 * that subclasses interact with the provided {@link #tracker TaskTracker} when
 * performing the task.
 * 
 * <p>Instances of this class are thread-safe - however, normal synchronisation
 * requirements apply for implemented code, as they would for any {@code
 * Runnable}.
 */
@ThreadSafe
public abstract class Task implements Runnable {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /**
     * Different states the task may occupy.
     */
    protected static enum TaskState {
        /** Indicates that the task has yet to be performed. */
        UNSTARTED,
        /** Indicates that the task is currently being performed. */
        RUNNING,
        /** Indicates that the task has stopped. */
        STOPPED,
        /** Indicates that the task has completed successfully, and
         * has stopped running. */
        COMPLETED;
    };
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The thread on which this task is executing. */
    private volatile Thread thread;
    
    private volatile TaskState state = TaskState.UNSTARTED;
    /** The throwable thrown during execution of the task. A value of {@code
     * null} indicates the task ran without throwing anything. This variable
     * piggybacks on the volatility of {@link #state} for consistency across
     * threads. */
    private Throwable throwable = null;
    
    /** The task tracker. This is set by the constructor. */
    protected final TaskTracker tracker;
    
    
    /**
     * Creates a new Task.
     * 
     * @param tracker The task's {@code TaskTracker}.
     * 
     * @throws NullPointerException if {@code tracker} is {@code null}.
     */
    public Task(TaskTracker tracker) {
        if(tracker == null)
            throw new NullPointerException("tracker is null");
        this.tracker = tracker;
    }
    
    /**
     * Runs the task. If {@link #cancel()} was invoked before this method is
     * invoked, the task will abort immediately.
     * 
     * @throws IllegalStateException if the task is already running.
     */
    public final void run() {
        if(state.equals(TaskState.RUNNING))
            throw new IllegalStateException("Task is already running!");
        if(isCancelled()) {
            state = TaskState.STOPPED;
            return;
        }
        throwable = null;
        thread = Thread.currentThread();
        state = TaskState.RUNNING;
        try {
            execute();
        } catch(Throwable t) {
            throwable = t;
            tracker.setFailed();
            ceaseRunning(TaskState.STOPPED);
            return;
        }
        tracker.setCompleted();
        ceaseRunning(TaskState.COMPLETED);
    }
    
    /**
     * Executes the task. Implementations are encouraged to allow exceptions
     * to propagate if they are severe enough to halt the task.
     * 
     * @throws Exception as per standard exceptional conditions.
     */
    protected abstract void execute() throws Exception;
    
    /**
     * Sets the state of this task and wakes up any threads which are waiting
     * for this task to complete, as per {@link #waitUntilStopped()} or {@link
     * #waitUninterruptibly()}.
     */
    private void ceaseRunning(TaskState newState) {
        doThenNotify(this, () -> state = newState);
    }
    
    /**
     * Returns {@link Thread#interrupted()}.
     * 
     * @return {@code true} if the thread on which this task it executing has
     * been interrupted; {@code false} otherwise.
     */
    protected final boolean isCancelled() {
        return Thread.interrupted();
    }
    
    /**
     * Checks for whether or not this task has been cancelled or the thread it
     * is running on has been interrupted, and throws an {@code
     * InterruptedException} if so.
     * 
     * @throws InterruptedException if the task has been cancelled/interrupted.
     */
    protected final void checkCancel() throws InterruptedException {
        if(state.equals(TaskState.RUNNING) && isCurrentThread() && isCancelled())
            throw new InterruptedException();
    }
    
    /**
     * Attempts to cancel the task by interrupting the thread on which it is
     * running. Invoking this does nothing if the task has finished running.
     */
    public final void cancel() {
        if(state.equals(TaskState.RUNNING) && !isCurrentThread())
            thread.interrupt();
    }
    
    /**
     * Checks for whether or not the task is currently stopped. Note that this
     * will return {@code false} if the task has not been run yet.
     * 
     * <p>Memory consistency effects: actions by the thread executing this task
     * happen-before actions in the current thread if this method returns
     * {@code true}.
     * 
     * @return {@code true} if the task is stopped; {@code false} if it is
     * currently executing.
     */
    public final boolean stopped() {
        return state.equals(TaskState.COMPLETED) || state.equals(TaskState.STOPPED);
    }
    
    /**
     * Checks for whether or not the task has been completed.
     * 
     * <p>Memory consistency effects: actions by the thread executing this task
     * happen-before actions in the current thread if this method returns
     * {@code true}.
     * 
     * @return {@code true} if the task has been completed; {@code false} if it
     * has not.
     */
    public final boolean completed() {
        return state.equals(TaskState.COMPLETED);
    }
    
    /**
     * Checks for whether or not the task has been completed, and reattempts
     * the task on the current thread if it failed to complete otherwise. This
     * method executes as if by:
     * 
     * <blockquote>
     * if(stopped()) {
     *     if(!completed())
     *         run();
     *     return true;
     * }
     * return false;
     * </blockquote>
     * 
     * <p>Note that this implies that this method may stall the current thread
     * if the expression {@code stopped() && !completed()} evaluates to
     * {@code true}.
     * 
     * <p>Memory consistency effects: actions by the thread executing this task
     * happen-before actions in the current thread if this method returns
     * {@code true}.
     * 
     * @return {@code true} if the task has been completed; {@code false} if it
     * has not.
     */
    /*
    public final boolean completedWithReattempt() {
        if(stopped()) {
            if(!completed())
                run();
            return true;
        }
        return false;
    }
    */
    
    /**
     * Gets the percentage of the task which has been completed thus far.
     * 
     * @return The percentage, from 0.0 to 1.0.
     * @see TaskTracker#percentComplete()
     */
    public final float percentComplete() {
        return tracker.percentComplete();
    }
    
    /**
     * Waits for the task to either complete or abort, if it is currently being
     * executed asynchronously. The current thread will block until the task
     * has stopped, or this thread is interrupted. Note that when this method
     * returns the task may not necessarily have been completed successfully.
     * 
     * <p>Memory consistency effects: actions by the thread executing this task
     * happen-before actions in the current thread if this method returns
     * without throwing an {@code InterruptedException}.
     * 
     * @throws InterruptedException if the current thread was interrupted while
     * waiting for the task to stop.
     * @throws ExecutionException if the task threw an exception or error while
     * executing.
     */
    public final void waitUntilStopped() throws InterruptedException, ExecutionException {
        if(canWait()) {
            waitInterruptibly(this, () -> !state.equals(TaskState.RUNNING));
            throwExcecutionException();
        }
    }
    
    /**
     * Waits for the task to either complete or abort, if it is currently being
     * executed. The current thread will block until the task has stopped
     * (beware of potential deadlocks). Note that when this method returns the
     * task may not necessarily have been completed successfully.
     * 
     * <p>If the current thread received an interrupt while waiting, the
     * interrupt flag will be set when this method returns.
     * 
     * <p>Memory consistency effects: actions by the thread executing this task
     * happen-before actions in the current thread when this method returns.
     * 
     * @throws ExecutionException if the task threw an exception or error while
     * executing.
     */
    public final void waitUninterruptibly() throws ExecutionException {
        if(canWait()) {
            waitUntil(this, () -> state.equals(TaskState.RUNNING));
            throwExcecutionException();
        }
    }
    
    /**
     * Gets the Throwable thrown by this task. A return value of {@code null}
     * indicates that the task has run (or is still running) without throwing
     * an exception or error.
     * 
     * @return The Throwable, or {@code null} if a Throwable has not been
     * thrown.
     */
    public final Throwable getThrowable() {
        return throwable;
    }
    
    /**
     * Checks for whether or not the task may be waited for by the current
     * thread.
     * 
     * @return {@code true} if task may be waited for; {@code false} otherwise.
     */
    private boolean canWait() {
        return state.equals(TaskState.RUNNING) && !isCurrentThread();
    }
    
    /**
     * @return {@code true} if the task is executing on the current thread;
     * {@code false} otherwise.
     */
    private boolean isCurrentThread() {
        return Thread.currentThread().equals(thread);
    }
    
    /**
     * Throws an {@code ExecutionException} wrapping the Throwable which halted
     * this task, if one was thrown at all. This clears {@code throwable}.
     * 
     * @throws ExecutionException if the task threw a Throwable while
     * executing.
     */
    private void throwExcecutionException() throws ExecutionException {
        // There is no need to make this atomic as this will only be invoked
        // once the task has completed
        if(throwable != null) {
            Throwable t = throwable;
            throwable = null;
            throw new ExecutionException(t);
        }
    }
    
    /**
     * @return This task's state.
     */
    protected final TaskState getState() {
        return state;
    }
    
    /**
     * Sets the status of the task being executed, as returned by
     * {@link #getStatus()}.
     * 
     * @param status The status of the current task being executed.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     * @see TaskTracker#setStatus(String)
     */
    protected final void setStatus(String status) {
        tracker.setStatus(status);
    }
    
    /**
     * Gets this task's {@code Tracker}.
     */
    public Tracker tracker() {
        return tracker;
    }
    
    /**
     * Gets a String representation of the Task, equivalent to {@link
     * TaskTracker#toString()}.
     * 
     * @see TaskTracker#toString()
     */
    @Override
    public String toString() {
        return tracker.toString();
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Waits on the specified object's monitor lock until the specified
     * condition returns {@code true}. If the current thread was interrupted
     * while waiting, the interrupt flag will be set when this method returns.
     * 
     * @param o The object to wait on.
     * @param endCondition The condition on which to wait.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static void waitUntil(Object o, BooleanSupplier endCondition) {
        boolean interrupted = false;
        synchronized(o) {
            while(!endCondition.getAsBoolean()) {
                try {
                    o.wait();
                } catch(InterruptedException retry) {
                    interrupted = true;
                }
            }
        }
        if(interrupted)
            Thread.currentThread().interrupt();
    }
    
    /**
     * Waits on the specified object's monitor lock until the specified
     * condition returns {@code true}.
     * 
     * @param o The object to wait on.
     * @param endCondition The condition on which to wait.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws InterruptedException if the current thread received an
     * interrupt while waiting.
     */
    public static void waitInterruptibly(Object o, BooleanSupplier endCondition)
            throws InterruptedException {
        synchronized(o) {
            while(!endCondition.getAsBoolean())
                o.wait();
        }
    }
    
    /**
     * Synchronises on {@code o}, then runs {@code task}, and then invokes
     * {@code notifyAll()} on {@code o} as such:
     * 
     * <pre>
     * synchronized(o) {
     *     if(task != null) task.run();
     *     o.notifyAll();
     * }</pre>
     * 
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public static void doThenNotify(Object o, Runnable task) {
        synchronized(o) {
            if(task != null) task.run();
            o.notifyAll();
        }
    }
    
    /**
     * If the specified condition returns {@code true}, invokes {@code
     * notifyAll()} on {@code o}.
     * 
     * @param syncCondition If true, the condition will be invoked while
     * synchronized on {@code o}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static void notifyIf(Object o, BooleanSupplier condition,
            boolean syncCondition) {
        if(syncCondition) {
            synchronized(o) {
                if(condition.getAsBoolean())
                    o.notifyAll();
            }
        } else {
            if(condition.getAsBoolean()) {
                synchronized(o) { o.notifyAll(); }
            }
        }
    }
    
}
