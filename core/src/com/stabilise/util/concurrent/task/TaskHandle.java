package com.stabilise.util.concurrent.task;

public interface TaskHandle {
    
    /**
     * Sets the status of this task.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    void setStatus(String status);
    
    /**
     * Marks a single part as completed. Equivalent to {@link #increment(int)
     * increment}{@code (1)}.
     * 
     * <p>Invoking this method publishes this task.
     */
    default void increment() {
        increment(1);
    }
    
    /**
     * Marks a specified number of parts as completed.
     * 
     * <p>Invoking this method publishes this task.
     * 
     * @throws IllegalArgumentException if {@code parts < 1}.
     */
    void increment(long parts);
    
    /**
     * Marks the specified number of parts as completed. Values are clamped to
     * the total parts. This method is provided for convenience; using {@link
     * #increment(long)} is generally preferable.
     * 
     * <p>Invoking this method publishes this task.
     * 
     * @throws IllegalArgumentException if {@code parts < 0}.
     */
    void set(long parts);
    
    /**
     * Sets the total number of parts for this subtask.
     * 
     * @throws IllegalArgumentException if {@code totalParts < 0 || totalParts
     * == Long.MAX_VALUE}.
     * @throws IllegalStateException if this task has been published.
     */
    void setTotal(long totalParts);
    
    /**
     * Updates the status and marks a single part as completed. Equivalent to
     * {@link #next(int, String) next}{@code (1, status)}.
     * 
     * <p>Invoking this method publishes this task.
     * 
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    default void next(String status) {
        next(1, status);
    }
    
    /**
     * Updates the status and marks a specified number of parts as completed.
     * Equivalent to invoking {@link #increment(int) increment}{@code (parts)}
     * and then {@link #setStatus(String) setStatus}{@code (status)}.
     * 
     * <p>Invoking this method publishes this task.
     * 
     * @throws IllegalArgumentException if {@code parts < 1}.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    default void next(long parts, String status) {
        increment(parts);
        setStatus(status);
    }
    
    /**
     * Throws an {@code InterruptedException} if the task has been cancelled.
     */
    default void checkCancel() throws InterruptedException {
        if(pollCancel())
            throw new InterruptedException("Cancelled");
    }
    
    /**
     * Checks for whether or not the task has been cancelled.
     * 
     * @return {@code true} if the task has been cancelled; {@code false}
     * otherwise.
     */
    boolean pollCancel();
    
    /**
     * Spawns a subtask of this task unit. The subtask will not be executed
     * until the current unit has completed, and any units following the
     * current one will not be executed until the subtask has completed.
     * 
     * <p>All non-parallel subtasks will be executed sequentially in order
     * of submission.
     * 
     * @param parallel Whether or not to execute the subtask in parallel with
     * other subtasks.
     * @param r The subtask.
     * 
     * @throws NullPointerException if {@code r} is {@code null}.
     * @throws IllegalArgumentException if {@code parallel} is {@code true},
     * and subtasks are being {@link #beginFlatten() flattened}.
     * @throws UnsupportedOperationException if this is a lightweight task that
     * cannot spawn subtasks.
     */
    void spawn(boolean parallel, TaskRunnable r);
    
    /**
     * Begins subtask flattening. All subtasks {@link
     * #spawn(boolean, TaskRunnable) spawned} between this and the next
     * invocation of {@link #endFlatten()} will be merged into a single task
     * unit, which may produce a nicer and less disjoint aesthetic from the
     * perspective of {@link Task#getStack()}.
     * 
     * @throws IllegalStateException if flattening is already in progress.
     * @throws UnsupportedOperationException if this is a lightweight task that
     * cannot spawn subtasks.
     */
    void beginFlatten();
    
    /**
     * Ends subtask flattening.
     * 
     * @throws IllegalStateException if flattening is not currently in
     * progress.
     * @throws UnsupportedOperationException if this is a lightweight task that
     * cannot spawn subtasks.
     */
    void endFlatten();
    
}
