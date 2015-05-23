package com.stabilise.core.app;

import java.util.Objects;

/**
 * An Event represents an occurrence which may be distributed to listeners via
 * an {@link EventBus}.
 */
public class Event {
	
	private final String name;
	
	
	/**
	 * Creates an Event.
	 * 
	 * @param name The identifier for this event. Events are distinguished by
	 * name, so be sure to make this reasonably unique!
	 */
	public Event(String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof Event)) return false;
		if(getClass() != o.getClass()) return false;
		Event e = (Event)o;
		return name.equals(e.name);
	}
	
	/**
	 * Identical to new Event(name);
	 */
	public static Event of(String name) {
		return new Event(name);
	}
	
}
