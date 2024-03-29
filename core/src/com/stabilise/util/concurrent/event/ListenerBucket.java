package com.stabilise.util.concurrent.event;

import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * A ListenerBucket holds all listeners for a particular event.
 * 
 * <p>Implementors do not need to worry about thread safety; concurrent
 * EventDispatchers will ensure that each method is invoked under the same
 * mutex.
 */
interface ListenerBucket<E extends Event> {
    
    /**
     * Adds a listener to the bucket. It is implicitly trusted that {@code
     * l} is not null and listens for the same event as this bucket.
     * 
     * @return true if the listener was successfully registered; false if
     * it should instead be executed immediately (as in a retained
     * EventDispatcher).
     */
    boolean addListener(Listener<?> l);
    
    /**
     * Removes the first listener from this bucket satisfying the given
     * predicate.
     */
    void removeListener(Predicate<Listener<?>> pred);
    
    /**
     * Removes any listeners from this bucket satisfying the given predicate.
     */
    void removeListeners(Predicate<Listener<?>> pred);
    
    /**
     * Posts the event. It is implicitly trusted that {@code e} is not null.
     * 
     * <p>To avoid causing a bottleneck in the mutex we return an array of
     * triggered listeners rather than executing them directly.
     */
    @Nullable Listener<? super E>[] dispatch(E e);
    
    /**
     * Returns {@code true} if this bucket is empty and may be removed.
     */
    boolean isEmpty();
    
}
