package com.stabilise.util.concurrent.event.old;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import com.stabilise.core.app.Application;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.concurrent.event.Event;

/**
 * A {@code RetainedEventDispatcher} is a specialised form of event dispatcher
 * which retains posted events. That is, once an event is posted to such a
 * dispatcher, it is considered to be permanently posted, and any listeners
 * subsequently registered are immediately run.
 * 
 * <p>Whether or not a listener is single-use is irrelevant for a retained
 * dispatcher since any particular event may only be posted once, so {@link
 * #addListener(Event, EventHandler, boolean)} is redundant.
 * 
 * <p>This class may be subclassed if desired.
 */
public class RetainedEventDispatcher extends EventDispatcher {
    
    /**
     * Creates a new RetainedEventDispatcher powered by the {@link
     * Application#executor() application executor}.
     */
    public RetainedEventDispatcher() {
        super();
    }
    
    /**
     * Creates a new RetainedEventDispatcher.
     * 
     * @param executor The executor with which to run event handlers.
     * 
     * @throws NullPointerException if {@code executor} is {@code null}.
     */
    public RetainedEventDispatcher(Executor executor) {
        super(executor);
    }
    
    @Override
    ListenerBucket newBucket() {
        return new RetainedListenerBucket();
    }
    
    @Override
    void doPost(Event e) {
        Listener[] ls = null;
        synchronized(lockFor(e)) {
            ListenerBucket b = handlers.get(e);
            if(b == null)
                handlers.put(e, b = newBucket());
            ls = b.post(e);
        }
        if(ls != null) {
            for(Listener l : ls) {
                execute(e, l);
            }
        }
    }
    
    /**
     * Retainer bucket impl. We store registered listeners in a list, and run
     * all of them when an event is received. Once an event is received, a
     * bucket takes note of that and henceforth immediately runs handlers when
     * they are registered.
     */
    private class RetainedListenerBucket implements ListenerBucket {
        
        /** Lazily initialised when needed, and then reset to null when the
         * event is received. */
        private List<Listener> listeners = null;
        private Event e = null;
        
        @Override
        public boolean addListener(Listener l) {
            if(e != null) {
                return false;
            } else {
                if(listeners == null)
                    listeners = new ArrayList<>(4);
                listeners.add(l);
                return true;
            }
        }
        
        @Override
        public void removeListener(Predicate<Listener> pred) {
            if(listeners != null)
                IteratorUtils.removeFirst(listeners, pred);
        }
        
        @Override
        public void removeListeners(Predicate<Listener> pred) {
            if(listeners != null)
                listeners.removeIf(pred);
        }
        
        @Override
        public Listener[] post(Event e) {
            // We ignore duplicate events rather than complain as to adhere to
            // the contract of post() in EventDispatcher.
            if(this.e != null) {
                return null;
                //throw new IllegalStateException("Event already posted!");
            }
            
            this.e = e;
            if(listeners != null) {
                Listener[] arr = listeners.toArray(new Listener[listeners.size()]);
                listeners = null;
                return arr;
            }
            return null;
        }
        
        @Override
        public boolean isEmpty() {
            // Only ever considered empty if a listener was added and
            // subsequently removed.
            return e == null && (listeners == null || listeners.isEmpty());
        }
        
    }
    
}
