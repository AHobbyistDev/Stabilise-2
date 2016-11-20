package com.stabilise.util.concurrent.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.stabilise.util.Log;
import com.stabilise.util.concurrent.FakeLock;
import com.stabilise.util.concurrent.Striper;

/**
 * An EventDispatcher allows users to register event listeners and dispatch
 * events to these listeners. To register an event listener via {@link
 * #addListener(Executor, Event, EventHandler)}, users specify the event to
 * listen for, the <i>callback function</i> - or <i>handler</i> - which is
 * invoked when the event is posted, and the executor with which to execute
 * the handler. It is worth nothing that even if given executors provide
 * execution-order guarantees, this class does not.
 * 
 * <p>An EventDispatcher offers two modes: <i>normal</i> and <i>retained</i>.
 * In retained mode, a posted event is considered to be perpetually posted, and
 * as such subsequent listeners registered on already-posted events are
 * triggered immediately. Note that whether or not a listener is single-use is
 * irrelevant in retained mode since any particular event may only be posted
 * once.
 * 
 * <p>Event equality is determined using the {@link Event#equals(Object)}
 * method - that is, a listener will be triggered if a posted event {@code
 * equals} its registered event.
 */
public class EventDispatcher {
    
    protected final boolean retained;
    protected final Map<Event, ListenerBucket<?>> handlers;
    protected final Supplier<ListenerBucket<?>> bucketSupplier;
    protected final Striper<Lock> locks;
    
    
    /**
     * @param retained true if this is a retained dispatcher; false if this is
     * a normal dispatcher.
     * @param concurrencyLevel The number of internal locks to use for this
     * dispatcher. 1 is usually a good value, but it may be suitable to use
     * more if this dispatcher will be frequently used by multiple threads. If
     * this value is 0, this dispatcher will not use any locks and will thus
     * not be thread-safe.
     * 
     * @throws IllegalArgumentException if {@code concurrencyLevel < 0}.
     */
    public EventDispatcher(boolean retained, int concurrencyLevel) {
        if(concurrencyLevel < 0)
            throw new IllegalArgumentException("concurrencyLevel < 0");
        
        this.retained = retained;
        
        handlers = concurrencyLevel == 0
                ? new HashMap<>()
                : new ConcurrentHashMap<>();
        
        bucketSupplier = retained
                ? RetainedListenerBucket::new
                : StandardListenerBucket::new;
        
        locks = concurrencyLevel == 0
                ? new Striper<>(1, () -> FakeLock.INSTANCE)
                : new Striper<>(concurrencyLevel, ReentrantLock::new);
    }
    
    /**
     * Adds a multi-use event listener.
     * 
     * @param exec The executor with which to execute the handler.
     * @param event The event to listen for.
     * @param handler The handler to invoke when the specified event is posted.
     * 
     * @throws NullPointerException if any argument is null.
     */
    public <E extends Event> void addListener(Executor exec, E event,
            EventHandler<? super E> handler) {
        addListener(exec, event, handler, false);
    }
    
    /**
     * Adds an event listener.
     * 
     * @param exec The executor with which to execute the handler.
     * @param event The event to listen for.
     * @param handler The handler to invoke when the specified event is posted.
     * @param singleUse If {@code true}, the listener is automatically removed
     * after an event is received.
     * 
     * @throws NullPointerException if any argument is null.
     */
    public <E extends Event> void addListener(Executor exec, E event,
            EventHandler<? super E> handler, boolean singleUse) {
        doAddListener(event, new Listener<>(exec, handler, singleUse ? 1 : -1));
    }
    
    /**
     * Registers the specified event listener.
     * 
     * @param e The event to listen for.
     * @param l The event listener.
     * 
     * @throws NullPointerException if either argument is null.
     */
    protected final <E extends Event> void doAddListener(E e, Listener<? super E> li) {
        Objects.requireNonNull(li);
        Lock l = locks.get(e);
        l.lock();
        try {
            if(handlers.computeIfAbsent(e, k -> bucketSupplier.get()).addListener(li))
                return;
        } finally {
            l.unlock();
        }
        
        // Method hasn't returned = listener rejected = we should execute it now
        li.execute(e);
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
    public <E extends Event> void removeListener(E e, EventHandler<? super E> handler) {
        Objects.requireNonNull(handler);
        
        Lock l = locks.get(e);
        l.lock();
        try {
            ListenerBucket<?> b = handlers.get(e);
            if(b != null) {
                b.removeListener(li -> li.handler.equals(handler));
                if(b.isEmpty())
                    handlers.remove(e);
            }
        } finally {
            l.unlock();
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
    
    @SuppressWarnings("unchecked")
    private <E extends Event> void doPost(E e) {
        ListenerBucket<E> b;
        Listener<? super E>[] ls = null;
        
        Lock l = locks.get(e);
        l.lock();
        try {
            if(retained) {
                if((b = (ListenerBucket<E>) handlers.get(e)) == null)
                    handlers.put(e, b = (ListenerBucket<E>) bucketSupplier.get());
                ls = b.post(e);
            } else {
                if((b = (ListenerBucket<E>) handlers.get(e)) != null) {
                    ls = b.post(e);
                    if(b.isEmpty())
                        handlers.remove(e);
                }
            }
        } finally {
            l.unlock();
        }
        
        if(ls != null) {
            for(Listener<? super E> li : ls) {
                try {
                    li.execute(e);
                } catch(RejectedExecutionException ex) {
                    Log.get().postSevere("Event listener rejected! Did the executor shut down?", ex);
                }
            }
        }
    }
    
    /**
     * Clears all event listeners.
     */
    public void clearListeners() {
        handlers.clear();
    }
    
    // No point exposing this method since Listener is a package-private class.
    /**
     * Clears any listeners satisfying the specified predicate. This is
     * exposed for subclasses to utilise if they wish; however it is not a
     * part of the public API for obvious reasons.
     * 
     * @throws NullPointerException if {@code pred} is {@code null}.
     */
    /*
    public final void clearListeners(Predicate<Listener<?>> pred) {
        Objects.requireNonNull(pred);
        handlers.entrySet().removeIf(e -> {
            Lock l = locks.get(e.getKey());
            l.lock();
            try {
                ListenerBucket<?> b = e.getValue();
                b.removeListeners(pred);
                return b.isEmpty();
            } finally {
                l.unlock();
            }
        });
    }
    */
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Returns a new <i>normal</i> EventDispatcher which is not thread-safe.
     */
    public static EventDispatcher normal() {
        return new EventDispatcher(false, 0);
    }
    
    /**
     * Returns a new <i>retained</i> EventDispatcher which is not thread-safe.
     */
    public static EventDispatcher retained() {
        return new EventDispatcher(true, 0);
    }
    
    /**
     * Returns a new <i>normal</i> EventDispatcher which is thread-safe (and
     * has a concurrency level of 1).
     */
    public static EventDispatcher concurrentNormal() {
        return new EventDispatcher(false, 1);
    }
    
    /**
     * Returns a new <i>retained</i> EventDispatcher which is thread-safe (and
     * has a concurrency level of 1).
     */
    public static EventDispatcher concurrentRetained() {
        return new EventDispatcher(true, 1);
    }
    
}