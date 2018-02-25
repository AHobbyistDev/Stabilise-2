package com.stabilise.util.concurrent.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ReturnTask, much like a {@link Future}, encapsulates an asynchronous
 * computation. Asides from its associated return value, a ReturnTask is
 * otherwise equivalent to an ordinary Task.
 */
public interface ReturnTask<T> extends Task {
    
    /**
     * Waits if necessary for this task to complete, and then retrieves the
     * result.
     * 
     * @throws InterruptedException if the current thread was interrupted while
     * waiting.
     * @throws ExecutionException if the task failed, or didn't set the result.
     */
    T get() throws InterruptedException, ExecutionException;
    
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
    T get(long time, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException;
    
    /**
     * Tries to get the result of this task without blocking.
     * 
     * @throws IllegalStateException if this task is not yet done, or failed.
     * @throws ExecutionException if the task didn't set the result.
     */
    T tryGet() throws ExecutionException;
    
}
