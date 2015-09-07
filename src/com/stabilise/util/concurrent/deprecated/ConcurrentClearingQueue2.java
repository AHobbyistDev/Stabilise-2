package com.stabilise.util.concurrent.deprecated;

import static com.stabilise.util.TheUnsafe.unsafe;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.stabilise.util.annotation.Incomplete;

/**
 * Testing alternative algorithms.
 */
@Incomplete
class ConcurrentClearingQueue2<E> implements Iterable<E> {
    
    // Unsafe stuff -----------------------------------------------------------
    
    private static final long tailOffset, nextOffset;
    
    static {
        try {
            tailOffset = offsetFor(ConcurrentClearingQueue2.class, "tail");
            nextOffset = offsetFor(ConcurrentClearingQueue2.Node.class, "next");
        } catch(Exception e) { throw new Error(e); }
    }
    
    private static long offsetFor(Class<?> c, String f) throws Exception {
        return unsafe.objectFieldOffset(c.getDeclaredField(f));
    }
    
    // Node Class -------------------------------------------------------------
    
    /**
     * Node class.
     * 
     * <p>Each node stores its item and links to the next node.
     */
    private class Node {
        
        /** The item. Always null for dummy head node. */
        final E item;
        /** Link to the next node.
         * This is null if this is the tail node, or this is the head node when
         * the list is empty. */
        volatile Node next;
        
        /** Constructs the default dummy node. */
        Node() {
            item = null;
        }
        
        /** Constructs a node holding the specified item. */
        Node(E e) {
            this.item = e;
        }
        
        E getItem() {
            return item;
        }
        
        /** Attempts to CAS the next node. */
        boolean casNext(Node exp, Node nxt) {
            return unsafe.compareAndSwapObject(Node.this, nextOffset, exp, nxt);
        }
        
        /** Swaps the next node and returns the old next. */
        @SuppressWarnings("unchecked")
        Node swapNext(Node nxt) {
            return (Node)unsafe.getAndSetObject(Node.this, nextOffset, nxt);
        }
        
    }
    
    /** Head node.
     * Invariants:
     * - Never null
     * - item is null
     * Non-invariants:
     * - next is null if list is empty
     * - next points to first node if not empty
     * - head is the same as tail if list is empty
     */
    private final Node head = new Node();
    
    /** Tail node.
     * Invariants:
     * - Never null
     * Non-invariants:
     * - item is null if list is empty
     * - head is the same as tail if list is empty
     * - if next is null, the queue is considered 'idle' and can be appended to
     * - if next is not null, an element is currently in the process of being
     *   added, and tail will soon be replaced with the new tail node
     *   - next may temporary be set to {@link #dummy} to abuse this protocol
     *     to lock this queue when extracting it to an iterator.
     */
    private volatile Node tail = head;
    
    
    /** Attempts to CAS the tail node and returns true if successful. */
    private boolean casTail(Node exp, Node nxt) {
        return unsafe.compareAndSwapObject(this, tailOffset, exp, nxt);
    }
    
    /**
     * Returns the size of this list. Note that this is not a constant-time
     * operation, as it traverses all the elements of the list.
     */
    public int size() {
        int size = 0;
        Node n = head;
        while((n = n.next) != null)
            size++;
        return size;
    }
    
    /**
     * Checks for whether this list is empty. This is a constant-time
     * operation.
     * 
     * @return {@code true} if this list is empty; {@code false} otherwise.
     */
    public boolean isEmpty() {
        return head.next == null;
    }
    
    /**
     * Adds an element to this list.
     */
    public void add(E e) {
        /*
        Node t, n = new Node(e);
        // a successful CAS 'locks' the tail for this thread
        while(!(t = tail).casNext(null, n)); // retry until this succeeds
        casTail(t, n); // Allowed to fail if tail is wiped by iterator()
        */
        
        // From java.util.concurrent.ConcurrentLinkedQueue
        final Node newNode = new Node(e);
        for(Node t = tail, p = t, q;;) {
            q = p.next;
            if(q == null) {
                // p is last node
                if(p.casNext(null, newNode)) {
                    // Successful CAS is the linearization point for e to
                    // become an element of this queue, and for newNode to
                    // become "live".
                    if(p != t) // hop two nodes at a time
                        casTail(t, newNode); // Failure is OK.
                    return;
                }
                // Lost CAS race to another thread; re-read next
            } else if(p == q)
                // We have fallen off list. If tail is unchanged, it will also
                // be off-list, in which case we need to jump to head, from
                // which all live nodes are always reachable. Else the new tail
                // is a better bet.
                p = (t != (t = tail)) ? t : head;
            else
                // Check for tail updates after two hops.
                p = (p != t && t != (t = tail)) ? t : q;
        }
    }
    
    /**
     * Returns an iterator over the elements in this list, and clears this
     * list. The returned iterator does not support remove(), as it is
     * meaningless. This method essentially ports the state of this list over
     * to the returned iterator, and as such additions to the list after
     * iterator creation are not seen by the iterator.
     */
    @Override
    public Iterator<E> iterator() {
        Node h = head.swapNext(null);
        for(Node t, n;;) {
            t = tail;
            n = t.next;
            if(n == null) {
                // The queue is in the quiescent state, and tail is the real
                // tail. We invalidate tail by pointing tail.next to itself.
                if(t.casNext(null, t))
                    break;
                // We lost the CAS race; retry.
            } else if(n.casNext(null, n))
                break;
        }
        tail = head;
        return new Itr(h);
        
        /*
        // We'll use a form of the double-check idiom to prevent unnecessary
        // CASing (if a thread is spin iterating, letting it CAS carelessly
        // may lock out other threads indefinitely).
        if(isEmpty())
            return new Itr(null);
        // We CAS in the dummy node on to the tail to 'plug it up'; other
        // threads can't add anything to it while tail.next != null
        while(!tail.casNext(null, dummy));
        // Now, we reset the head node and extract the queue proper
        Node n = head.swapNext(null);
        tail = head; // Reset the tail node; queue works from here on out
        return new Itr(n);
        */
    }
    
    /**
     * Returns an iterator over the elements in this list. The returned
     * iterator does not support remove().
     */
    public Iterator<E> nonClearingIterator() {
        return new Itr(head.next);
    }
    
    // Iterator impl. ---------------------------------------------------------
    
    private class Itr implements Iterator<E> {
        
        private Node next;
        
        /** first may be null */
        Itr(Node first) {
            next = first;
        }
        
        @Override
        public boolean hasNext() {
            return next != null && next != next.next;
        }
        
        @Override
        public E next() {
            try {
                final E e = next.getItem();
                next = next.next;
                return e;
            } catch(NullPointerException e) { throw new NoSuchElementException(); }
        }
        
    }
    
}
