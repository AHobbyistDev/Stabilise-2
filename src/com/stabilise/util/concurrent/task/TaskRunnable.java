package com.stabilise.util.concurrent.task;

/**
 * The common interface with which to implement tasks to run under the Task
 * framework. You can think of a {@code TaskRunnable} as equivalent to a {@code
 * Runnable} which accepts a {@code TaskHandle} through which to communicate to
 * the Task API.
 */
@FunctionalInterface
public interface TaskRunnable extends TaskExecutable {
    
    /**
     * Runs the task.
     * 
     * @param handle The handle to the task API. This should not escape the
     * scope of this method.
     * 
     * @throws Exception if an unrecoverable error occurred while running the
     * task.
     */
    void run(TaskHandle handle) throws Exception;
    
}
