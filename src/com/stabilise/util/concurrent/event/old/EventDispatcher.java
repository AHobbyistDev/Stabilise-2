package com.stabilise.util.concurrent.event.old;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.stabilise.core.app.Application;
import com.stabilise.util.annotation.GuardedBy;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.util.concurrent.Striper;
import com.stabilise.util.concurrent.event.Event;

// This class is an older version of EventDispatcher that doesn't involve
// generics, just in case something went really really wrong with the generic
// version.

/**
 * An EventDispatcher allows users to register event listeners and dispatch
 * events to these listeners. To register an event listener via {@link
 * #addListener(Event, EventHandler)}, users specify the event to listen for
 * and the <i>callback function</i> - or <i>handler</i> - which is invoked when
 * the event is posted.
 * 
 * <p>Handlers are posted to an {@code Executor} specified in an {@code
 * EventDispatcher's} constructor (or the {@link Application#executor()
 * application executor} by default). As such, no guarantees are made as to the
 * order in which listeners for a particular event are run, and given they may
 * be run on different threads entirely, handler methods should be thread-safe.
 * 
 * <p>By default, listeners are single-use - that is, they are automatically
 * removed when they receive an event. To register a persistent listener, use
 * {@link #addListener(Event, EventHandler, boolean)}.
 * 
 * <p>Event equality is determined using the {@link Event#equals(Object)}
 * method such that a listener will be invoked when an event which
 * <tt>equals</tt> its registered event is posted.
 * 
 * <p>This class may be subclassed if desired.
 */
@ThreadSafe
public class EventDispatcher {
    
    // Package-private for RetainedEventDispatcher
    final ConcurrentHashMap<Event, ListenerBucket> handlers = new ConcurrentHashMap<>();
    private final Striper<Object> locks;
    private final Executor executor;
    
    
    /**
     * Creates a new EventDispatcher powered by the {@link
     * Application#executor() application executor} and with a concurrency
     * level of 1.
     */
    public EventDispatcher() {
        this(Application.executor());
    }
    
    /**
     * Creates a new EventDispatcher with a concurrency level of 1.
     * 
     * @param executor The executor with which to run event handlers.
     * 
     * @throws NullPointerException if {@code executor} is {@code null}.
     */
    public EventDispatcher(Executor executor) {
        this(executor, 1);
    }
    
    /**
     * Creates a new EventDispatcher.
     * 
     * @param executor The executor with which to run event handlers.
     * @param concurrencyLevel The number of internal locks to use for this
     * dispatcher. 1 is usually a good value, but it may be suitable to use
     * more if this dispatcher will be frequently used by multiple threads.
     * 
     * @throws NullPointerException if {@code executor} is {@code null}.
     * @throws IllegalArgumentException if {@code concurrencyLevel < 1}.
     */
    public EventDispatcher(Executor executor, int concurrencyLevel) {
        this.executor = Objects.requireNonNull(executor);
        this.locks = Striper.generic(concurrencyLevel);
    }
    
    /**
     * Adds a single-use event listener.
     * 
     * @param event The event to listen for.
     * @param handler The handler to invoke when the specified event is posted.
     * 
     * @throws NullPointerException if either argument is null.
     */
    public void addListener(Event event, EventHandler handler) {
        addListener(event, handler, true);
    }
    
    /**
     * Adds an event listener.
     * 
     * @param event The event to listen for.
     * @param handler The handler to invoke when the specified event is posted.
     * @param singleUse If {@code true}, the listener is automatically removed
     * after an event is received.
     * 
     * @throws NullPointerException if any argument is null.
     */
    public void addListener(Event event, EventHandler handler,
            boolean singleUse) {
        addListener(event, new Listener(handler, singleUse ? 1 : -1));
    }
    
    /**
     * Registers the specified event listener.
     * 
     * @param e The event to listen for.
     * @param l The event listener.
     * 
     * @throws NullPointerException if either argument is null.
     */
    protected final void addListener(Event e, Listener l) {
        Objects.requireNonNull(l);
        synchronized(lockFor(e)) {
            if(handlers.computeIfAbsent(e, k -> newBucket()).addListener(l))
                return;
        }
        // No return = listener rejected = we should execute it now
        execute(e, l);
    }
    
    final void execute(Event e, Listener l) {
        executor.execute(() -> l.accept(e));
    }
    
    /**
     * Creates a new listener bucket. Overridden in {@link
     * RetainedEventDispatcher} to return a customised bucket.
     */
    ListenerBucket newBucket() {
        return new StandardListenerBucket();
    }
    
    /**
     * Removes an event listener, if it exists.
     * 
     * <p>Note that this method generally does not interact nicely with
     * lambdas as they aren't designed for equality testing. As such, when
     * using this method, ensure you're properly unregistering a listener!
     * 
     * @param e The event being listened for.
     * @param handler The handler to remove.
     * 
     * @throws NullPointerException if either argument is null.
     */
    public final void removeListener(Event e, EventHandler handler) {
        Objects.requireNonNull(handler);
        
        synchronized(lockFor(e)) {
            ListenerBucket b = handlers.get(e);
            if(b != null) {
                b.removeListener(l -> l.handler.equals(handler));
                if(b.isEmpty())
                    handlers.remove(e);
            }
        }
    }
    
    /**
     * Posts an event.
     * 
     * @throws NullPointerException if e is null.
     */
    public final void post(Event e) {
        doPost(Objects.requireNonNull(e));
    }
    
    /**
     * Actually posts an event. Package-private impl. method as {@link
     * #post(Event)} must be public final. {@code e} is never null.
     */
    void doPost(Event e) {
        ListenerBucket b; Listener[] ls = null;
        synchronized(lockFor(e)) {
            if((b = handlers.get(e)) != null) {
                ls = b.post(e);
                if(b.isEmpty())
                    handlers.remove(e);
            }
        }
        if(ls != null) {
            for(Listener l : ls) {
                execute(e, l);
            }
        }
    }
    
    /**
     * Clears all event listeners.
     */
    public final void clear() {
        handlers.clear();
    }
    
    /**
     * Clears any listeners satisfying the specified predicate. This is
     * exposed for subclasses to utilise if they wish; however it is not a
     * part of the public API for obvious reasons.
     * 
     * @throws NullPointerException if {@code pred} is {@code null}.
     */
    protected final void clear(Predicate<Listener> pred) {
        Objects.requireNonNull(pred);
        handlers.entrySet().removeIf(e -> {
            synchronized(lockFor(e.getKey())) {
                e.getValue().removeListeners(pred);
                return e.getValue().isEmpty();
            }
        });
    }
    
    /**
     * Returns a lock for the specified event. Throws an NPE if e is null.
     */
    Object lockFor(Event e) {
        return locks.get(e.hashCode());
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Implementation interface for an event handler.
     */
    public static interface EventHandler extends Consumer<Event> {}
    
    /**
     * Holds all data about an event listener. Implements EventHandler for 
     * convenience.
     */
    protected static class Listener implements EventHandler {
        
        private final EventHandler handler;
        /** Set to 1 for single-use and -1 otherwise. This is merely a
         * framework in the case that I wish to implement listeners which may
         * activate only a set number of times. */
        private long uses;
        
        /**
         * @throws NullPointerException if handler is null.
         */
        public Listener(EventHandler handler, long uses) {
            this.handler = Objects.requireNonNull(handler);
            this.uses = uses;
        }
        
        /**
         * @throws NullPointerException if handler is null.
         */
        public Listener(EventHandler handler, boolean singleUse) {
            this(handler, singleUse ? 1 : -1);
        }
        
        @Override
        public final void accept(Event e) {
            handler.accept(e);
        }
        
    }
    
    /**
     * A ListenerBucket is an entry in the {@link EventDispatcher#handlers} map
     * corresponding to a registered event. Each ListenerBucket manages the
     * listeners corresponding to its event.
     * 
     * <p>Access to each method is guarded by an exclusion lock, so
     * implementations do not need to worry about thread safety.
     */
    static interface ListenerBucket {
        
        /**
         * Adds a listener to the bucket. It is implicitly trusted that {@code
         * l} is not null and listens for the same event as this bucket.
         * 
         * @return true if the listener was successfully registered; false if
         * it should instead be executed immediately (see {@link
         * RetainedEventDispatcher}).
         */
        @GuardedBy("lockFor")
        public boolean addListener(Listener l);
        
        /**
         * Removes the first listener from this bucket satisfying the given
         * predicate.
         */
        @GuardedBy("lockFor")
        public void removeListener(Predicate<Listener> pred);
        
        /**
         * Removes any listeners from this bucket satisfying the given
         * predicate.
         */
        @GuardedBy("lockFor")
        public void removeListeners(Predicate<Listener> pred);
        
        /**
         * Posts the event. It is implicitly trusted that {@code e} is not
         * null. Returns the list of triggered listeners so they can be
         * executed while not in a synchronised block.
         */
        @GuardedBy("lockFor")
        public Listener[] post(Event e);
        
        /**
         * Returns {@code true} if this bucket is empty and may be removed.
         */
        @GuardedBy("lockFor")
        public boolean isEmpty();
        
    }
    
    /**
     * The standard listener bucket implementation. Registered listeners are
     * stored in an unordered list. When an event is posted, we traverse the
     * list and run each of the listeners, removing any single-use listeners.
     */
    private class StandardListenerBucket implements ListenerBucket {
        
        private final List<Listener> listeners = new UnorderedArrayList<>(4, 2f);
        
        @Override
        public boolean addListener(Listener l) {
            listeners.add(l);
            return true;
        }
        
        @Override
        public void removeListener(Predicate<Listener> pred) {
            IteratorUtils.removeFirst(listeners, pred);
        }
        
        @Override
        public void removeListeners(Predicate<Listener> pred) {
            listeners.removeIf(pred);
        }
        
        @Override
        public Listener[] post(Event e) {
            Listener[] arr = listeners.toArray(new Listener[listeners.size()]);
            listeners.removeIf(l -> --l.uses == 0);
            return arr;
        }
        
        @Override
        public boolean isEmpty() {
            return listeners.isEmpty();
        }
        
    }
    
}
