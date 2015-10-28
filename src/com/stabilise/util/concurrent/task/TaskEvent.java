package com.stabilise.util.concurrent.task;

import com.stabilise.util.concurrent.event.Event;

/**
 * This class encapsulates the standard set of events relating to the lifecycle
 * of a task - start, stop, cancel, fail, and complete.
 */
public class TaskEvent extends Event {
    
    public static final TaskEvent START    = new TaskEvent("start");
    public static final TaskEvent STOP     = new TaskEvent("stop");
    public static final FailEvent FAIL     = new FailEvent(null);
    public static final TaskEvent COMPLETE = new TaskEvent("complete");
    
    private TaskEvent(String name) {
        super(name);
    }
    
    /**
     * A FailEvent is a TaskEvent which indicates failure of a task. An
     * instance additionally contains a (possibly null) {@code Throwable}
     * which can be retrieved via {@link #getCause()}, which is the cause of
     * task failure.
     */
    public static class FailEvent extends TaskEvent {
        
        private final Throwable cause;
        
        FailEvent(Throwable cause) {
            super("fail");
            this.cause = cause;
        }
        
        /**
         * Returns the Throwable treated as the cause of task failure. May be
         * {@code null}.
         */
        public Throwable getCause() {
            return cause;
        }
        
    }
    
}
