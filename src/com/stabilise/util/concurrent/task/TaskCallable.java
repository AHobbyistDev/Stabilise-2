package com.stabilise.util.concurrent.task;

/**
 * The common interface with which to implement value-returning tasks to run
 * under the Task framework. You can think of a {@code TaskCallable} as
 * equivalent to a {@code Callable} which accepts a {@code TaskHandle} through
 * which to communicate to the Task API.
 */
public interface TaskCallable<T> {
    
    /**
     * Runs the task and returns the resultant value.
     * 
     * @param handle The handle to the task API. This should not escape the
     * scope of this task.
     * 
     * @throws Exception if an unrecoverable error occurred while running the
     * task.
     */
    T call(TaskHandle handle) throws Exception;
    
    /**
     * Gets the number of parts in this task unit. Ordinarily this should be
     * already set via {@code TaskBuilder} when this unit was declared, but in
     * the case it wasn't (e.g., if the total number of parts can only be
     * determined at runtime), you can override this method to calculate and
     * return the total number of parts.
     * 
     * <p>This method is invoked immediately before {@code #run(TaskHandle)}.
     * If this method returns a negative number that is not -1, or {@code
     * Long.MAX_VALUE}, the task will never execute (as they are illegal
     * values).
     * 
     * <p>A return value of -1 is used to indicate that the task shouldn't
     * update the parts value (i.e. -1 is the "ignore this!" value). This
     * method returns -1 by default.
     */
    default long getParts() {
        return -1;
    }
    
}
