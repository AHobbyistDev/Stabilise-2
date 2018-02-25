package com.stabilise.util.concurrent;

import java.util.Objects;
import java.util.function.Consumer;

// Why did I even make this class if I don't use it?

/**
 * A GuardedObject guards a value behind some object's intrinsic lock.
 */
public class GuardedObject<T> {
    
    private final T value;
    private final Object guard;
    
    
    private GuardedObject(T value, Object guard) {
        this.value = value;
        this.guard = guard == null ? this : guard;
    }
    
    /**
     * Performs an action on the guarded object.
     * 
     * @throws NullPointerException if {@code action} is {@code null}.
     */
    public void with(Consumer<? super T> action) {
        synchronized(guard) {
            action.accept(value);
        }
    }
    
    /**
     * Guards an object using the returned GuardedObject as the mutex.
     */
    public static <T> GuardedObject<T> guarded(T value) {
        return new GuardedObject<>(value, null);
    }
    
    /**
     * Guards an object using the given guard as the mutex.
     * 
     * @throws NullPointerException if {@code guard} is {@code null}.
     */
    public static <T> GuardedObject<T> guarded(T value, Object guard) {
        return new GuardedObject<>(value, Objects.requireNonNull(guard));
    }
    
}
