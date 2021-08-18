package com.stabilise.util.concurrent.task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A Task is essentially a {@code Runnable} with additional useful facilities.
 * 
 * <p>The main entry point for creating a Task is {@link #builder()}.
 */
@ThreadSafe
public class TaskImpl extends AbstractTask {
    
    private static final TaskView[] EMPTY_STACK = {};
    
    @GuardedBy("this") private final Deque<TaskUnit> stack = new ArrayDeque<>();
    @GuardedBy("this") private TaskView[] lastStack = EMPTY_STACK;
    @GuardedBy("this") private boolean stackDirty = true;
    
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicInteger unitsRunning = new AtomicInteger(0);
    
    protected final AtomicReference<Throwable> failCause = new AtomicReference<>();
    
    
    /**
     * Instantiated only by TaskBuilder. It is trusted that none of the
     * arguments are null.
     */
    TaskImpl(Executor exec, TaskTracker tracker, TaskUnit firstUnit) {
        super(exec, tracker);
        
        stack.addLast(firstUnit);
    }
    
    /**
     * Starts this task, unless it has been preemptively cancelled.
     * 
     * @return This task.
     * @throws IllegalStateException if this task has already been started.
     */
    public TaskImpl start() {
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
        //System.out.println("Adding " + unit.status() + " to stack: "
        //        + stack.size() + " -> " + (stack.size() + 1));
        stack.addLast(unit);
        stackDirty = true;
    }
    
    synchronized void endSubtask(TaskUnit unit) {
        //System.out.println("Removing " + unit.status() + " from stack: "
        //            + stack.size() + " -> " + (stack.size() - 1));
        stack.removeLast();
        stackDirty = true;
    }
    
    @Override
    public synchronized TaskView[] getStack() {
        if(stackDirty) {
            stackDirty = false;
            if(lastStack.length != 1 + stack.size()) {
                lastStack = new TaskView[1 + stack.size()];
                lastStack[0] = this;
            }
            int i = 1;
            for(TaskUnit u : stack)
                lastStack[i++] = u;
        }
        return lastStack.clone();
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
    
    /**
     * Fails the task and brings down all currently-running units, if the task
     * hasn't failed or been cancelled already.
     * 
     * @param failurePoint The throwable to treat as the cause of the failure.
     * May be null.
     */
    void fail(Throwable failurePoint) {
        // We'll restrict ourselves to a single failure cause since one task
        // failing can set off others failing too, and their failures could
        // be redundant (but we have no way of knowing).
        failCause.compareAndSet(null, failurePoint);
        
        // If the current unit is a group and one of its subtasks failed, we'll
        // need to throw out a cancellation to notify the rest of them to stop.
        if(cancelled.compareAndSet(false, true))
            stack.getFirst().cancel();
        
        // We don't invoke setState(State.FAILED) because we want to wait for
        // all currently-running tasks to finish up before notify anyone
        // waiting on await(), etc. So we use onUnitStop() for this purpose.
    }
    
    /**
     * Gets the throwable which is being treated as the cause of this task
     * failing.
     */
    Throwable failurePoint() {
        return failCause.get();
    }
    
    @Override
    public void cancel() {
        if(canCancel() && cancelled.compareAndSet(false, true)) {
            failCause.compareAndSet(null, new CancellationException("Cancelled"));
            // Root propagates cancellation down to all subtasks, so we
            // don't need to explicitly cancel everything in the stack.
            stack.getFirst().cancel();
        }
    }
    
    private boolean canCancel() {
        State s = tracker.getState();
        return s == State.UNSTARTED || s == State.RUNNING;
    }
    
    /** Polls cancellation status. */
    boolean cancelled() {
        return cancelled.get();
    }
    
}
