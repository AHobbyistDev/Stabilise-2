package com.stabilise.util.concurrent.task2;

import javax.annotation.Nonnull;

/**
 * The common interface with which to implement value-returning tasks to run
 * under the Task framework. You can think of a {@code TaskCallable} as
 * equivalent to a {@code Callable} which accepts a {@code TaskHandle} through
 * which to communicate to the Task API.
 */
@FunctionalInterface
public interface TaskCallable<T> {
    
    /**
     * Runs the task and returns the resultant value.
     * 
     * @param handle The handle to the task API. This should not escape the
     * scope of this method.
     * 
     * @throws Exception if an unrecoverable error occurred while running the
     * task.
     */
    T run(@Nonnull TaskHandle handle) throws Exception;
    
}
