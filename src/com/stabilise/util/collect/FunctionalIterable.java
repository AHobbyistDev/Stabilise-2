package com.stabilise.util.collect;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Extends on {@code Iterator} for functional-ish niceness.
 * 
 * <p>Note that for a FunctionalIterable it is generally preferable to use
 * {@link #iterate(Predicate)} or {@link #forEach(Consumer)} in preference to
 * a typical iterator as implementors may be able to make optimisations which
 * would otherwise be difficult or impossible using a standard iterator
 * implementation (e.g. fast iteration of an ArrayList (see the documentation
 * for {@link java.util.RandomAccess})).
 */
public interface FunctionalIterable<E> extends Iterable<E> {
    
    /**
     * Iterates over all elements, removing those for which
     * <tt>pred.{@link Predicate#test(Object) test}()</tt> returns {@code
     * true}.
     * 
     * @throws NullPointerException if {@code pred} is {@code null}.
     */
    default void iterate(Predicate<? super E> pred) {
        Objects.requireNonNull(pred); // fail-fast
        for(Iterator<E> i = iterator(); i.hasNext();) {
            if(pred.test(i.next()))
                i.remove();
        }
    }
    
    /**
     * Iterates over this iterable's elements, stopping only once {@code pred}
     * returns {@code true} or the number of elements is exhausted.
     * 
     * @throws NullPointerException if {@code pred} is {@code null}.
     */
    default void forEachUntil(Predicate<? super E> pred) {
        Objects.requireNonNull(pred); // fail-fast
        for(Iterator<E> i = iterator(); i.hasNext();) {
            if(pred.test(i.next()))
                return;
        }
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Wraps an {@code Iterable} object in a {@code FunctionalIterable}.
     * 
     * @throws NullPointerException if {@code itr} is {@code null}.
     */
    public static <T> FunctionalIterable<T> wrap(final Iterable<T> itr) {
        Objects.requireNonNull(itr); // fail-fast
        return new FunctionalIterable<T>() {
            @Override public Iterator<T> iterator() { return itr.iterator(); }
        };
    }
    
}
