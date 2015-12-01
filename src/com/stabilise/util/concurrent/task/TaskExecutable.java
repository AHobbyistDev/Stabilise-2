package com.stabilise.util.concurrent.task;

/**
 * Superinterface for {@link TaskRunnable} and {@link TaskCallable}.
 */
public interface TaskExecutable {
    
    /**
     * Gets the number of parts in this task unit. Ordinarily this should be
     * already set via {@code TaskBuilder} when this unit was declared, but in
     * the case it wasn't (e.g., if the total number of parts can only be
     * determined at runtime), you can override this method to calculate and
     * return the total number of parts.
     * 
     * <p>This method is invoked immediately before {@code run()}. If this
     * method returns a negative number that is not -1, or {@code
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
