package com.stabilise.util.concurrent.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.stabilise.util.ArrayUtil;
import com.stabilise.util.collect.IteratorUtils;

/**
 * Retained listener bucket implementation.
 */
class RetainedListenerBucket<E extends Event> implements ListenerBucket<E> {
    
    /** Lazily initialised when needed, and then reset to null when the
     * event is received. */
    private List<Listener<? super E>> listeners = null;
    private Event e = null;
    
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean addListener(Listener<?> l) {
        if(e != null) {
            return false;
        } else {
            if(listeners == null)
                listeners = new ArrayList<>(4);
            listeners.add((Listener<? super E>) l);
            return true;
        }
    }
    
    @Override
    public void removeListener(Predicate<Listener<?>> pred) {
        if(listeners != null)
            IteratorUtils.removeFirst(listeners, pred);
    }
    
    @Override
    public void removeListeners(Predicate<Listener<?>> pred) {
        if(listeners != null)
            listeners.removeIf(pred);
    }
    
    @Override
    public Listener<? super E>[] post(Event e) {
        // We ignore duplicate events rather than complain as to adhere to
        // the contract of post() in EventDispatcher.
        if(this.e != null) {
            return ArrayUtil.emptyArr();
            //throw new IllegalStateException("Event already posted!");
        }
        
        this.e = e;
        if(listeners != null) {
            @SuppressWarnings("unchecked")
            Listener<? super E>[] arr = listeners.toArray(new Listener[listeners.size()]);
            listeners = null;
            return arr;
        }
        return ArrayUtil.emptyArr();
    }
    
    @Override
    public boolean isEmpty() {
        // Only ever considered empty if a listener was added and
        // subsequently removed.
        return e == null && (listeners == null || listeners.isEmpty());
    }
    
}
