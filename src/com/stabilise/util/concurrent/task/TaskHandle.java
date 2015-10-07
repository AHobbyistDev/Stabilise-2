package com.stabilise.util.concurrent.task;

import com.stabilise.util.concurrent.event.Event;
import com.stabilise.util.concurrent.event.EventDispatcher;


public interface TaskHandle {
    
    void setStatus(String status);
    default void increment() { increment(1); }
    void increment(int parts);
    default void next(String status) { next(1, status); }
    void next(int parts, String status);
    
    /** @see EventDispatcher#post(Event) */ 
    void post(Event e);
    
}
