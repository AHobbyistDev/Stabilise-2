package com.stabilise.core.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.concurrent.ClearingQueue;
import com.stabilise.util.concurrent.Striper;

/**
 * This class manages events, which may be posted by any thread.
 * 
 * <p>Event listeners have the following properties:
 * <ul>
 * <li><b>Persistence</b>: When the Application changes state, non-persistent
 *     listeners are automatically removed.
 * <li><b>Single-use</b>: Single-use listeners are removed automatically after
 *     being invoked once.
 * <li><b>Async</b>: Asynchronous listeners are given to {@link
 *     Application#getExecutor() the Application's executor}, while synchronous
 *     (non-async) listeners are invoked on the main application thread.
 * </ul>
 */
@ThreadSafe
public class EventBus {
	
	private static final int CONCURRENCY_LEVEL = 4;
	
	private final ConcurrentHashMap<Event, List<Listener>> handlers =
			new ConcurrentHashMap<>();
	private final ClearingQueue<Event> pendingEvents = ClearingQueue.create();
	
	private final Striper<Object> locks = Striper.generic(CONCURRENCY_LEVEL);
	
	
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
		addListener(event, handler, persistent, singleUse, false);
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
	 * @param async Whether or not the listener should be run asynchronously.
	 * 
	 * @throws NullPointerException if any argument is null.
	 */
	public void addListener(Event event, Consumer<Event> handler,
			boolean persistent, boolean singleUse, boolean async) {
		synchronized(lockFor(event)) {
			handlers.computeIfAbsent(event, k -> new ArrayList<>(4)).add(
					new Listener(handler, persistent, singleUse ? 1 : -1, async)
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
		
		synchronized(lockFor(e)) {
			List<Listener> listeners = handlers.get(e);
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
		// Post it to the queue for the non-async handlers, and then fire off
		// the async handlers immediately.
		pendingEvents.add(Objects.requireNonNull(e));
		handle(e, true);
	}
	
	/**
	 * Updates the bus.
	 */
	void update() {
		pendingEvents.consume(e -> handle(e, false));
	}
	
	private void handle(Event e, boolean async) {
		// We add triggered listeners to a list and run them outside the
		// synchronized block to minimise lock hold time.
		
		List<Listener> ls = null; // lazy-init
		
		synchronized(lockFor(e)) {
			List<Listener> listeners = handlers.get(e);
			if(listeners == null)
				return;
			
			ls = new ArrayList<>(listeners.size());
			
			for(Iterator<Listener> i = listeners.iterator(); i.hasNext();) {
				Listener l = i.next();
				if(async != l.async)
					continue;
				
				l.e = e;
				ls.add(l);
				
				if(--l.uses == 0)
					i.remove();
			}
			
			if(listeners.isEmpty())
				handlers.remove(e);
		}
		
		if(ls != null) {
			for(Listener l : ls) {
				if(async)
					Application.executor().execute(l);
				else
					l.run();
			}
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
	
	void clear(Predicate<Listener> condition) {
		IteratorUtils.forEach(handlers.entrySet(), e -> {
			synchronized(lockFor(e.getKey())) {
				IteratorUtils.forEach(e.getValue(), condition);
				return e.getValue().isEmpty();
			}
		});
	}
	
	private Object lockFor(Event e) {
		return locks.get(e);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Holds all data about an event listener. Extends Runnable for convenience
	 * when submitting to an executor.
	 */
	private static final class Listener implements Runnable {
		
		private final Consumer<Event> handler;
		private final boolean persistent;
		/** Set to 1 for single-use and -1 otherwise. This is merely a
		 * framework in the case that I wish to implement listeners which may
		 * activate only a set number of times. */
		private long uses;
		private final boolean async;
		
		/** Convenience storage. */
		public Event e = null;
		
		
		/**
		 * @throws NullPointerException if handler is null
		 */
		public Listener(Consumer<Event> handler, boolean persistent, long uses,
				boolean async) {
			this.handler = Objects.requireNonNull(handler);
			this.persistent = persistent;
			this.uses = uses;
			this.async = async;
		}
		
		@Override
		public void run() {
			handler.accept(e);
		}
		
	}
	
}
