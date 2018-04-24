package com.stabilise.util.concurrent.event;

import java.util.List;
import java.util.function.Predicate;

import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.UnorderedArrayList;

/**
 * The standard listener bucket implementation. Registered listeners are stored
 * in an unordered list. When an event is posted, we traverse the list and run
 * each of the listeners, returning any ones whose uses have expired.
 */
class StandardListenerBucket<E extends Event> implements ListenerBucket<E> {
    
    private final List<Listener<? super E>> listeners = new UnorderedArrayList<>(4, 2f);
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean addListener(Listener<?> l) {
        listeners.add((Listener<? super E>) l);
        return true;
    }
    
    @Override
    public void removeListener(Predicate<Listener<?>> pred) {
        IteratorUtils.removeFirst(listeners, pred);
    }
    
    @Override
    public void removeListeners(Predicate<Listener<?>> pred) {
        listeners.removeIf(pred);
    }
    
    @Override
    public Listener<? super E>[] post(E e) {
        @SuppressWarnings("unchecked")
        Listener<? super E>[] arr = listeners.toArray(new Listener[listeners.size()]);
        listeners.removeIf(Listener::consumeUse);
        return arr;
    }
    
    @Override
    public boolean isEmpty() {
        return listeners.isEmpty();
    }
    
}
