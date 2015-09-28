package com.stabilise.util.collect;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides an alternative superinterface to {@link Iterable} for simplified
 * classes.
 */
public interface SimpleIterable<E> {
    
    /**
     * Iterates over all elements, removing those for which
     * <tt>pred.{@link Predicate#test(Object) test}()</tt> returns {@code
     * true}.
     * 
     * @throws NullPointerException if {@code pred} is {@code null}.
     */
    void forEach(Predicate<? super E> pred);
    
    /**
     * Iterates over all elements, invoking <tt>
     * cons.{@link Consumer#accept(Object) accept}()</tt> for each element.
     * 
     * @throws NullPointerException if {@code cons} is {@code null}.
     */
    void iterate(Consumer<? super E> cons);
    
}
