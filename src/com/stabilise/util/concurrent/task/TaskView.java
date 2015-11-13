package com.stabilise.util.concurrent.task;


public interface TaskView {
    
    /**
     * Gets the status of this task.
     */
    String status();
    
    /**
     * Returns the fraction of this task which has been completed, from 0 to 1
     * (inclusive).
     */
    double fractionCompleted();
    
    /**
     * Returns the percentage of this task which has been completed, from 0 to
     * 100 (inclusive). Equivalent to {@code (int)(100 * fractionCompleted())}.
     */
    default int percentCompleted() {
        return (int)(100 * fractionCompleted());
    }
    
    /**
     * Returns the number of parts of this task which have been marked as
     * completed. This is always less than or equal to the value returned by
     * {@link #totalParts()}.
     */
    long partsCompleted();
    
    /**
     * Returns the total number of parts in this task. This value never
     * changes.
     */
    long totalParts();
    
}
