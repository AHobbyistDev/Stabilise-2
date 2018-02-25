package com.stabilise.util.concurrent;

import java.util.Iterator;
import java.util.function.Consumer;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@code ClearingQueue} is a minimalist type of thread-safe queue for which
 * elements can be {@link #add(Object) added} to and {@link #iterator()
 * iterated} over.
 * 
 * <p>Iterating over a {@code ClearingQueue} clears the queue; this has the
 * same net effect as invoking {@link Iterator#remove() remove()} for every
 * element returned by {@link Iterator#next() next()}. A single element will
 * never be iterated over twice unless it is added to a queue multiple times.
 * 
 * <p>Memory consistency effects of a {@code ClearingQueue}: actions in a
 * thread prior to adding an element to a queue <i>happen-before</i> actions in
 * a thread which reads that element during iteration.
 */
@ThreadSafe
public interface ClearingQueue<E> extends Iterable<E> {
    
    /**
     * Returns the size of this list. Note that this is not a constant-time
     * operation, as it traverses all the elements of the list.
     */
    int size();
    
    /**
     * Checks for whether this list is empty. This is a constant-time
     * operation.
     * 
     * @return {@code true} if this list is empty; {@code false} otherwise.
     */
    boolean isEmpty();
    
    /**
     * Adds an element to this queue.
     * 
     * <p>Memory consistency effects: actions in a thread prior to adding an
     * element to this queue <i>happen-before</i> actions in a thread which
     * reads that element from an iterator.
     */
    void add(E e);
    
    /**
     * Performs the given action for each element of the queue, and clears the
     * queue (since this is after all a ClearingQueue).
     * 
     * <p>This method is functionally equivalent to {@link #forEach(Consumer)},
     * however, usage of this method may offer more clarity on inspection as to
     * the fact that iteration clears the queue. For example, in the following:
     * 
     * <pre>
     * for(Object o : queue)
     *     doSomething(o);</pre>
     * 
     * <p>it is not readily apparent that the action of iteration clears the
     * queue, whereas
     * 
     * <pre>queue.consume(o -> doSomething(o));</pre>
     * 
     * <p>is more suggestive of the clearing nature of the queue.
     * 
     * @param consumer The action to be performed for each element.
     * 
     * @throws NullPointerException if {@code consumer} is {@code null}.
     */
    default void consume(Consumer<? super E> consumer) {
        forEach(consumer);
    }
    
    /**
     * Returns an iterator over the elements in this queue, and clears this
     * queue. The returned iterator does not support remove(), as it is
     * meaningless.
     * 
     * <p>This method essentially ports the state of this queue over to the
     * returned iterator, and as such additions to the queue after iterator
     * creation are not seen by the iterator. Synchronisation over the iterator
     * is not required.
     * 
     * <p>Memory consistency effects: actions in a thread prior to adding an
     * element to this queue <i>happen-before</i> actions in a thread which
     * reads that element from the returned iterator.
     */
    Iterator<E> iterator();
    
    /**
     * Returns an iterator over the elements in this list. The returned
     * iterator does not support remove().
     * 
     * <p>Note that synchronisation over the iterator is not required.
     * 
     * <p>Memory consistency effects: actions in a thread prior to adding an
     * element to this queue <i>happen-before</i> actions in a thread which
     * reads that element from the returned iterator.
     */
    Iterator<E> nonClearingIterator();
    
    /**
     * Returns an iterable view of this queue whose iterator is returned as if
     * by {@link #nonClearingIterator()}. This method is provided as to allow
     * one to use the shorthand iterator notation for non-clearing iterators,
     * i.e., as if by:
     * 
     * <pre>
     * for(Object o : queue.asNonClearing()) { ... }
     * </pre>
     */
    default Iterable<E> asNonClearing() {
        return () -> nonClearingIterator();
    }
    
    /**
     * Returns an iterable view of this queue whose iterator is optionally
     * clearing.
     * 
     * @param clearing If {@code true}, returns this. If {@code false}, returns
     * {@link #asNonClearing()}.
     */
    default Iterable<E> asIterable(final boolean clearing) {
        return clearing ? this : asNonClearing();
    }
    
    /**
     * Returns a new {@code ClearingQueue}.
     * 
     * <p>This convenience method is offered to make implementation switching
     * easier in the event that a better/faster implementation than {@link
     * SynchronizedClearingQueue} is devised.
     */
    public static <E> ClearingQueue<E> create() {
        return new SynchronizedClearingQueue<>();
    }
    
}
