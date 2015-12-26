package com.stabilise.util.concurrent.task2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CancellationException;
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

import com.stabilise.util.annotation.ForTestingPurposes;
import com.stabilise.util.concurrent.Waiter;

/**
 * A Task is essentially a {@code Runnable} with additional useful facilities.
 * 
 * <p>The main entry point for creating a Task is {@link #builder()}.
 */
@ThreadSafe
public class Task implements TaskView {
    
    private static final TaskUnit[] EMPTY_STACK = {};
    
    private final Executor exec;
    
    @GuardedBy("this") private final Deque<TaskUnit> stack = new ArrayDeque<>();
    @GuardedBy("this") private TaskUnit[] lastReturnedStack = EMPTY_STACK;
    @GuardedBy("this") private boolean stackDirty = true;
    
    private final TaskTracker tracker;
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
        
        stack.addLast(firstUnit);
    }
    
    /**
     * Starts this task, unless it has been preemptively cancelled.
     * 
     * @return This task.
     * @throws IllegalStateException if this task has already been started.
     */
    public Task start() {
        if(!cancelled.get()) {
            if(tracker.setState(State.UNSTARTED, State.RUNNING))
                exec.execute(stack.getLast().setOwner(this)); // i.e. the only element
            else
                throw new IllegalStateException("Already started");
        } else {
            failCause.compareAndSet(null, new CancellationException("Task cancelled"));
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
    
    // Task stack operations
    
    synchronized void nextSequential(TaskUnit unit) {
        stack.removeLast();
        stack.addLast(unit);
        stackDirty = true;
    }
    
    synchronized void beginSubtask(TaskUnit unit) {
        stack.addLast(unit);
        stackDirty = true;
    }
    
    synchronized void endSubtask() {
        stack.removeLast();
        stackDirty = true;
    }
    
    public synchronized TaskView[] getStack() {
        if(stackDirty) {
            stackDirty = false;
            lastReturnedStack = stack.toArray(EMPTY_STACK);
        }
        return lastReturnedStack;
    }
    
    @ForTestingPurposes
    public void printStack() {
        TaskView[] view = getStack();
        StringBuilder sb = new StringBuilder();
        printProgressBar(sb, this);
        for(TaskView t : view)
            printProgressBar(sb, t);
        System.out.println(sb.toString());
    }
    
    private void printProgressBar(StringBuilder sb, TaskView view) {
        sb.append("<");
        String status = view.status();
        if(status.length() > 80)
            status = status.substring(0, 80);
        int perc = view.percentCompleted();
        int statLen = status.length();
        int i;
        for(i = 1; i <= Math.min(perc, 50 - statLen/2); i++)
            sb.append('#');
        for(; i <= 50-statLen/2; i++)
            sb.append(' ');
        sb.append(status);
        i += statLen;
        for(; i <= Math.min(100, perc); i++)
            sb.append('#');
        for(; i <= 100; i++)
            sb.append(' ');
        sb.append(">\n");
    }
    
    // End task stack operations
    
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
    
    void fail(Throwable failurePoint) {
        // We'll restrict ourselves to a single failure cause since one task
        // failing can set off others failing too, and their failures could
        // be redundant (but we have no way of knowing).
        failCause.compareAndSet(null, failurePoint);
        
        // If the current unit is a group and one of its subtasks failed, we'll
        // need to throw out a cancellation to notify the rest of them to stop.
        synchronized(this) {
            cancelled.set(true);
            
            stack.getFirst().cancel();
        }
        
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
        if(!stoppedExclUnstarted() && cancelled.compareAndSet(false, true)) {
            synchronized(this) {
                // Root propagates cancellation down to all subtasks, so we
                // don't need to explicitly cancel everything in the stack.
                stack.getFirst().cancel();
            }
        }
    }
    
    /** Polls cancellation status. */
    boolean cancelled() {
        return cancelled.get();
    }
    
    /**
     * Returns {@code true} if this task is stopped (i.e. it either hasn't been
     * started yet, has completed, or has failed); {@code false} otherwise.
     */
    public boolean stopped() {
        return tracker.getState() != State.RUNNING;
    }
    
    private boolean stoppedExclUnstarted() {
        State s = tracker.getState();
        return s != State.UNSTARTED && s != State.RUNNING;
    }
    
    /**
     * Returns {@code true} if this task has been successfully completed;
     * {@code false} otherwise.
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
    public long partsCompleted() {
        return tracker.getPartsCompleted();
    }
    
    @Override
    public long totalParts() {
        return tracker.getTotalParts();
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
