package com.stabilise.util.concurrent.task;


public enum SubtaskReportMode {
    
    /** Bubble up all parts from all subtasks. */
    ALL,
    /** Only bubble up a single part per subtask when they're completed. */
    COMPLETION,
    /** Weight out all parts from subtasks so they contribute evenly. */
    EVEN;
    
}
