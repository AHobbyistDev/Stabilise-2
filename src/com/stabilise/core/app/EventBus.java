package com.stabilise.core.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.util.concurrent.Striper;
import com.stabilise.util.concurrent.SynchronizedClearingQueue;

/**
 * This class manages events. Events may be posted by any thread, and their
 * corresponding listeners are invoked on the main application thread.
 * 
 * <p>Event listeners have two properties - <i>persistence</i>, and
 * <i>single-use</i>. Persistent listeners are retained when the application
 * changes states. Single-use listeners are removed automatically after being
 * invoked once.
 */
@ThreadSafe
public class EventBus {
	
	private final ConcurrentHashMap<Event, List<Listener>> handlers =
			new ConcurrentHashMap<>();
	private final ClearingQueue<Event> pendingEvents =
			new SynchronizedClearingQueue<>();
	
	private final Striper<Object> locks = Striper.generic(16);
	
	
	EventBus() {} // Package-private constructor
	
	/**
	 * Adds a non-persistent, single-use event listener.
	 * 
	 * @param event The event to listen for.
	 * @param handler The handler to invoke when the specified event is posted.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public void addListener(Event event, Consumer<Event> handler) {
		addListener(event, handler, false);
	}
	
	/**
	 * Adds a single-use event listener.
	 * 
	 * @param event The event to listen for.
	 * @param handler The handler to invoke when the specified event is posted.
	 * @param persistent Whether or not the listener is persistent.
	 * 
	 * @throws NullPointerException if any argument is null.
	 */
	public void addListener(Event event, Consumer<Event> handler,
			boolean persistent) {
		addListener(event, handler, persistent, true);
	}
	
	/**
	 * Adds an event listener.
	 * 
	 * @param event The event to listen for.
	 * @param handler The handler to invoke when the specified event is posted.
	 * @param persistent Whether or not the listener is persistent.
	 * Non-persistent events are automatically removed when the application
	 * state is changed.
	 * @param singleUse Whether or not the listener is single-use.
	 * 
	 * @throws NullPointerException if any argument is null.
	 */
	public void addListener(Event event, Consumer<Event> handler,
			boolean persistent, boolean singleUse) {
		synchronized(lockFor(event)) {
			handlers.computeIfAbsent(event, k -> new ArrayList<>(4)).add(
					new Listener(handler, persistent, singleUse ? 1 : -1)
			);
		}
	}
	
	/**
	 * Removes an event listener, if it exists.
	 * 
	 * @param e The event being listened for.
	 * @param handler The handler to remove.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public void removeListener(Event e, final Consumer<Event> handler) {
		Objects.requireNonNull(handler);
		
		List<Listener> listeners;
		synchronized(lockFor(e)) {
			listeners = handlers.get(e);
			if(listeners == null)
				return;
			IteratorUtils.forEach(listeners, l -> l.handler.equals(handler));
			if(listeners.size() == 0)
				handlers.remove(e);
		}
	}
	
	/**
	 * Posts an event.
	 * 
	 * @throws NullPointerException if e is null.
	 */
	public void post(Event e) {
		pendingEvents.add(Objects.requireNonNull(e));
	}
	
	/**
	 * Updates the bus.
	 */
	void update() {
		for(Event e : pendingEvents) // clears the list
			handle(e);
	}
	
	private void handle(Event e) {
		synchronized(lockFor(e)) {
			List<Listener> listeners = handlers.get(e);
			if(listeners == null)
				return;
			IteratorUtils.forEach(listeners, l -> {
				l.handler.accept(e);
				return --l.uses == 0;
			});
			if(listeners.isEmpty())
				handlers.remove(e);
		}
	}
	
	/**
	 * Clears all event listeners.
	 */
	public void clear() {
		clear(false);
	}
	
	/**
	 * Clears event listeners.
	 * 
	 * @param nonPersistentOnly true if only non-persistent listeners should be
	 * removed. If this is false, all listeners are cleared.
	 */
	public void clear(boolean nonPersistentOnly) {
		if(!nonPersistentOnly)
			handlers.clear();
		IteratorUtils.forEach(handlers.entrySet(), e -> {
			synchronized(lockFor(e.getKey())) {
				IteratorUtils.forEach(e.getValue(), l -> !l.persistent);
				return e.getValue().isEmpty();
			}
		});
	}
	
	private Object lockFor(Event e) {
		return locks.get(e.hashCode());
	}
	
	private static final class Listener {
		private final Consumer<Event> handler;
		private final boolean persistent;
		private long uses;
		
		/**
		 * @throws NullPointerException if handler is null
		 */
		public Listener(Consumer<Event> handler, boolean persistent, long uses) {
			this.handler = Objects.requireNonNull(handler);
			this.persistent = persistent;
			this.uses = uses;
		}
	}
	
}
