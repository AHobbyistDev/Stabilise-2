package com.stabilise.util.box;

import java.util.function.UnaryOperator;

/**
 * A Box is an object which contains - or "Boxes" - another object. A Box may
 * be useful when multiple sources need to share multiple objects.
 */
public interface Box<T> {
    
    /**
     * Gets the boxed object, or {@code null} if no element is boxed.
     */
    T get();
    
    /**
     * Sets the boxed object.
     * 
     * @throws UnsupportedOperationException if this box does not permit
     * setting.
     * @throws NullPointerException if {@code t} is {@code null} and this box
     * does not permit null values.
     */
    void set(T t);
    
    /**
     * Updates the boxed value using the specified updater function.
     * 
     * @throws UnsupportedOperationException if this box does not permit
     * setting.
     * @throws NullPointerException if the updater is {@code null}, or it
     * supplies a {@code null} object and this box does not permit null values.
     */
    default void update(UnaryOperator<T> updater) {
        set(updater.apply(get()));
    }
    
}
