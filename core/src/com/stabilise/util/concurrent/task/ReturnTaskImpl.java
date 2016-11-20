package com.stabilise.util.concurrent.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ReturnTask, much like a {@link Future}, encapsulates an asynchronous
 * computation. Asides from its associated return value, a ReturnTask is
 * otherwise equivalent to an ordinary Task.
 */
public class ReturnTaskImpl<T> extends TaskImpl implements ReturnTask<T> {
    
    private final ReturnBox<T> retVal;
    
    
    /**
     * Instantiated only by TaskBuilder.
     */
    ReturnTaskImpl(Executor exec, TaskTracker tracker, TaskUnit firstUnit,
            ReturnBox<T> retVal) {
        super(exec, tracker, firstUnit);
        this.retVal = retVal;
    }
    
    // Overridden as to return ReturnTask<T> instead of Task
    @Override
    public ReturnTaskImpl<T> start() {
        super.start();
        return this;
    }
    
    /**
     * Waits if necessary for this task to complete, and then retrieves the
     * result.
     * 
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     * @throws ExecutionException if the task failed, or didn't set the result.
     */
    public T get() throws InterruptedException, ExecutionException {
        if(!await()) {
            Throwable t = failCause.get();
            throw new ExecutionException("Task did not complete successfully ("
                        + (t == null ? "no reason given" : t.getMessage()) + ")",
                        failCause.get());
        }
        return retVal.get(failCause);
    }
    
    /**
     * Waits if necessary for this task to complete, and then retrieves the
     * result. Throws a TimeoutException if the specified time elapsed before
     * the task completed.
     * 
     * @param time The maximum time to wait.
     * @param unit The unit of the {@code time} argument.
     * 
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     * @throws ExecutionException if the task failed, or didn't set the result.
     * @throws TimeoutException if the specified time elapsed before this task
     * could finish.
     */
    public T get(long time, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if(!await(time, unit))
            throw new TimeoutException();
        if(failed())
            throw new ExecutionException("Task did not complete successfully", failCause.get());
        return retVal.get(failCause);
    }
    
    /**
     * Tries to get the result of this task without blocking.
     * 
     * @throws IllegalStateException if this task is not yet done, or failed.
     * @throws ExecutionException if the task didn't set the result.
     */
    public T tryGet() throws ExecutionException {
        if(!completed())
            throw new IllegalStateException("Task not completed");
        return retVal.get(failCause);
    }
    
}
