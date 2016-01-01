package com.stabilise.util.collect;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * Miscellaneous iterator-based utils.
 */
public class IteratorUtils {
    
    private IteratorUtils() {}
    
    public static final Iterator<Object> EMPTY_ITERATOR = new EmptyIterator();
    
    
    /**
     * Runs the given predicate for every element in the {@code Iterable},
     * removing each element for which {@code pred} returns {@code true}.
     * 
     * @throws NullPointerException if either argument is null.
     * @see java.util.Collection#removeIf(Predicate)
     */
    public static <T> void forEach(Iterable<T> col, Predicate<? super T> pred) {
        forEach(col.iterator(), pred);
    }
    
    /**
     * Runs the given predicate for every remaining element in the {@code
     * Iterator}, removing every element for which {@code pred} returns {@code
     * true}.
     * 
     * @throws NullPointerException if either argument is null.
     * @see java.util.Collection#removeIf(Predicate)
     */
    public static <T> void forEach(Iterator<T> i, Predicate<? super T> pred) {
        Objects.requireNonNull(pred); // fail-fast
        while(i.hasNext()) {
            if(pred.test(i.next()))
                i.remove();
        }
    }
    
    /**
     * Removes the first item in {@code i} which satisfies the given predicate.
     * 
     * @throws NullPointerException if either argument is null.
     */
    public static <T> void removeFirst(Iterable<T> i, Predicate<? super T> pred) {
        removeFirst(i.iterator(), pred);
    }
    
    /**
     * Removes the first item from {@code i} which satisfies the given
     * predicate.
     * 
     * @throws NullPointerException if either argument is null.
     */
    public static <T> void removeFirst(Iterator<T> i, Predicate<? super T> pred) {
        Objects.requireNonNull(pred); // fail-fast
        while(i.hasNext()) {
            if(pred.test(i.next())) {
                i.remove();
                return;
            }
        }
    }
    
    /**
     * Returns an iterator over {@code col} which filters out any null
     * elements. The returned iterator does not support remove.
     * 
     * @throws NullPointerException if col is null.
     */
    public static <T> Iterator<T> iteratorNullsFiltered(Iterable<T> col) {
        return iteratorNullsFiltered(col.iterator());
    }
    
    /**
     * Returns an iterator wrapping {@code itr} which filters out any null
     * elements. The returned iterator does not support remove.
     * 
     * @throws NullPointerException if itr is null.
     */
    public static <T> Iterator<T> iteratorNullsFiltered(Iterator<T> itr) {
        return Iterators.filter(itr, Predicates.notNull());
    }
    
    /**
     * Wraps the specified Enumeration in an Iterable.
     * 
     * @throws NullPointerException if e is null.
     */
    public static <T> Iterable<T> toIterable(Enumeration<T> e) {
        Objects.requireNonNull(e); // fail-fast
        return () -> toIterator(e);
    }
    
    /**
     * Converts the specified Enumeration to an Iterator.
     * 
     * @throws NullPointerException if e is null.
     */
    public static <T> Iterator<T> toIterator(Enumeration<T> e) {
        Objects.requireNonNull(e);
        
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return e.hasMoreElements();
            }
            @Override
            public T next() {
                return e.nextElement();
            }
        };
    }
    
    /**
     * Builds an iterator from a specified initial element and a function which
     * provides the next element. The input for {@code nextSupplier} is always
     * the element that was last returned by {@link Iterator#next()}, and its
     * return value is used as the next element. The iterator terminates when
     * {@code nextSupplier} returns {@code null} (i.e. {@code hasNext() returns
     * false}).
     * 
     * <p>Constructing an iterator as such may be preferable to invoking a
     * function recursively, as recursion can result in unbounded stack growth.
     * For example, consider the following.
     * 
     * <pre>
     * public class RecursiveClass {
     *     private RecursiveClass next;
     *     
     *     // Nice and simple, but can result in unbounded stack growth!
     *     public void work() {
     *         ... // do some work
     *         if(next != null)
     *             next.work();
     *     }
     * }
     * 
     * public class IterativeClass extends MyClass {
     *     private IterativeClass next;
     *     
     *     // Not quite as simple, but doesn't risk stack overflow!
     *     public void work() {
     *         IteratorUtils.buildIterator(this, obj -> obj.next)
     *                 .forEachRemaining(obj -> obj.doWork());
     *         
     *         // which is equivalent to:
     *         //for(IterativeClass obj = this; obj != null; obj = obj.next) {
     *         //    obj.doWork();
     *         //}
     *     }
     *     
     *     private void doWork() { ... }
     * }
     * </pre>
     * 
     * @param initial The first element to return.
     * @param nextSupplier A function to return the next element given the one
     * preceding it.
     * 
     * @return An iterator.
     */
    public static <T> Iterator<T> buildIterator(T initial, UnaryOperator<T> nextSupplier) {
        Objects.requireNonNull(initial);
        Objects.requireNonNull(nextSupplier);
        
        return new Iterator<T>() {
            T cur = initial;
            T next = initial;
            boolean polled = true;
            
            @Override
            public boolean hasNext() {
                poll();
                return next != null;
            }
            
            private void poll() {
                if(!polled) {
                    next = nextSupplier.apply(cur);
                    polled = true;
                }
            }
            
            @Override
            public T next() {
                if(!hasNext()) throw new NoSuchElementException();
                polled = false;
                cur = next;
                return cur;
            }
            
        };
    }
    
    /**
     * Wraps {@code itr} in an {@code iterable}.
     */
    public static <T> Iterable<T> wrap(Iterator<T> itr) {
        return () -> itr;
    }
    
    /**
     * Returns an iterator which contains no elements.
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EMPTY_ITERATOR;
    }
    
    
    
    private static class EmptyIterator implements Iterator<Object> {
        
        @Override
        public boolean hasNext() {
            return false;
        }
        
        @Override
        public Object next() {
            return null;
        }
        
    }
    
}
