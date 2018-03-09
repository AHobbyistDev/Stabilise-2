package com.stabilise.util.collect;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.math.MathUtils;

/**
 * A RingBuffer is an array-based bounded queue implementation with
 * constant-time queue operations. A RingBuffer does not permit null elements.
 */
@NotThreadSafe
public class RingBuffer<E> extends AbstractQueue<E> {
    
    /*
     * Our indexing strategy is as follows:
     * 
     * start points to the head of the queue (i.e. the first element). If the
     * queue is empty this points to where the first element will be placed.
     * 
     * end points to where the next element will be inserted. This is the same
     * as start in two cases: the queue is completely empty or completely full.
     * As a result, the distinction between a full buffer and an empty one is
     * whether or not the head element is null.
     */
    
    /** The index of the element at the head of the queue. */
    private int start = 0;
    /** index(tail element) + 1 % size. New elements are inserted into
     * buf[end], and then end is incremented. */
    private int end = 0;
    /** Bitmask for start and end to ensure wrapping. */
    private final int mask;
    private final E[] buf;
    
    
    /**
     * Creates a new RingBuffer with size equal to {@link
     * MathUtils#nextPowerOfTwo(int) nextPowerOfTwo(size)}.
     */
    public RingBuffer(int size) {
        this.mask = MathUtils.nextPowerOfTwo(size) - 1;
        @SuppressWarnings("unchecked")
        E[] e = (E[])new Object[mask + 1];
        this.buf = e;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException if {@code e} is {@code null}.
     */
    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e);
        if(isFull())
            return false;
        buf[end] = e;
        end = (end + 1) & mask;
        return true;
    }
    
    /**
     * Adds an element to the tail of this queue. If this queue is full, the
     * element at the head of the queue (i.e. the oldest element) is ejected
     * and returned.
     * 
     * @param e The element to add.
     * 
     * @return The old head of the queue, if this queue was full. Otherwise,
     * {@code null}.
     * @throws NullPointerException if {@code e} is {@code null}.
     */
    public E push(E e) {
        Objects.requireNonNull(e);
        // If full, we overwrite the head element and shift head.
        if(isFull())
            start = (start + 1) & mask;
        E old = buf[end];
        buf[end] = e;
        end = (end + 1) & mask;
        return old;
    }
    
    @Override
    public E poll() {
        E e = buf[start];
        if(e == null) // queue empty
            return null;
        buf[start] = null;
        start = (start + 1) & mask;
        return e;
    }
    
    @Override
    public E peek() {
        return buf[start];
    }
    
    /**
     * Retrieves, but does not remove, the tail of this ring buffer, or returns
     * null if this ring buffer is empty.
     */
    public E peekTail() {
        return buf[(end - 1 + buf.length) & mask];
    }
    
    /**
     * Returns the capacity of this queue.
     */
    public int capacity() {
        return buf.length;
    }
    
    /**
     * Returns the number of elements in this queue.
     */
    @Override
    public int size() {
        return isFull() ? capacity() : (end - start) & mask;
    }
    
    /**
     * Returns {@code true} if this queue is full.
     */
    public boolean isFull() {
        return end == start && !isEmpty();
    }
    
    @Override
    public boolean isEmpty() {
        return buf[start] == null;
    }
    
    /**
     * Returns an iterator over the elements in this buffer, from head to tail.
     * The returned iterator does not support {@link Iterator#remove()
     * remove()}.
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    private class Itr implements Iterator<E> {
        
        private int ptr;
        
        public Itr() {
            ptr = start;
        }
        
        @Override
        public boolean hasNext() {
            return ptr != end || (ptr == start && buf[start] != null);
        }
        
        @Override
        public E next() {
            if(!hasNext())
                throw new NoSuchElementException();
            E e = buf[ptr];
            ptr = (ptr + 1) & mask;
            return e;
        }
        
    }
    
}
