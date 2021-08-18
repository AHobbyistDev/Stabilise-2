package com.stabilise.util.concurrent.task;


/**
 * Possible states for a task and its units to occupy.
 */
enum State {
    
    /**
     * Indicates a task or unit has not started yet.
     * 
     * <p>Allowed transitions:
     * <ul>
     * <li>UNSTARTED -> RUNNING
     * </ul>
     */
    UNSTARTED,
    
    /**
     * Indicates a task or unit is currently running.
     * 
     * <p>Allowed transitions:
     * <ul>
     * <li>RUNNING -> COMPLETION_PENDING (TaskUnit only)
     * <li>RUNNING -> FAILED
     * </ul>
     */
    RUNNING,
    
    /**
     * Indicates a unit (<i>not the main task</i>) has finished main
     * execution, and is waiting for some final operations (e.g. triggering
     * subtasks) to complete before transitioning to COMPLETED;
     * 
     * <p>Allowed transitions:
     * <ul>
     * <li>COMPLETION_PENDING -> COMPLETED
     * <li>COMPLETION_PENDING -> FAILED
     * </ul>
     */
    COMPLETION_PENDING,
    
    /**
     * Indicates a task or unit has successfully completed.
     */
    COMPLETED,
    
    /**
     * Indicates a task or unit has failed, possibly due to cancellation.
     */
    FAILED
    
}
