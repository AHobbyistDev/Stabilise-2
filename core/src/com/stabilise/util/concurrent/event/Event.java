package com.stabilise.util.concurrent.event;

import java.util.Objects;

/**
 * An Event represents a notification which may be distributed to listeners via
 * an {@link EventDispatcher}.
 * 
 * <p>An Event is identified by the two things: its class and the name assigned
 * to it in its constructor, so {@code
 * new Event("Hello, world!").equals(new Event("Hello, world!"))} will return
 * {@code true}, as they are indistinguishable. This is acceptable for many
 * cases, but poses a security threat for others. To make an event
 * unduplicatable, simply subclass Event and instantiate your event as a
 * member of that class. e.g.,
 * 
 * <pre>
 * // In practice you'd probably want a private constructor to ensure you have
 * // a true singleton.  
 * public class MyEvent extends Event {
 *     public MyEvent(String name) { super(name); }
 * }
 * 
 * assert(new Event("hi").equals(new   Event("hi"))); // works - they are equal
 * assert(new Event("hi").equals(new MyEvent("hi"))); // doesn't - not equal
 * </pre>
 */
public class Event {
    
    private final String name;
    
    
    /**
     * Creates an Event.
     * 
     * @param name The identifier for this event. Events are distinguished by
     * name, so be sure to make this reasonably unique!
     * 
     * @throws NullPointerException if {@code name} is {@code null}.
     */
    public Event(String name) {
        this.name = Objects.requireNonNull(name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public final boolean equals(Object o) {
        if(o == this) return true;
        // Events with identical names but different classes will be unequal,
        // which is what we want.
        if(o == null || getClass() != o.getClass()) return false;
        Event e = (Event)o;
        return name.equals(e.name);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(\"" + name + "\")";
    }
    
    /**
     * Identical to {@code new Event(name)}.
     */
    public static Event of(String name) {
        return new Event(name);
    }
    
}
