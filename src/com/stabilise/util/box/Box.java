package com.stabilise.util.box;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A Box is an object which contains - or "Boxes" - another object. A Box may
 * be useful when multiple sources need to share multiple objects.
 * 
 * <p>A Box also includes a set of functional methods which allow it to act
 * sort-of as a mutable {@code Optional}.
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
     * Returns {@code true} if a non-null value is present in this box.
     */
    default boolean isPresent() {
        return get() != null;
    }
    
    /**
     * If a value is present, invokes the specified consumer with the boxed
     * value; otherwise does nothing.
     */
    default void ifPresent(Consumer<? super T> action) {
        T t = get();
        if(t != null) action.accept(t);
    }
    
    /**
     * If a value is present, returns a Box wrapping the given mapper's output.
     * Otherwise, returns an empty box.
     */
    default <U> Box<U> map(Function<? super T, ? extends U> mapper) {
        T t = get();
        if(t != null)
            return Boxes.box(mapper.apply(t));
        else
            return Boxes.empty();
    }
    
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
    
    /**
     * Returns the boxed value if it is non-null, else returns {@code other}.
     */
    default T orElse(T other) {
        T t = get();
        return t == null ? other : t;
    }
    
    /**
     * Returns the boxed value if it is non-null, else returns {@code
     * other.get()}.
     */
    default T orElseGet(Supplier<? extends T> other) {
        T t = get();
        return t == null ? other.get() : t;
    }
    
}
