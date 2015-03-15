package com.stabilise.util.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import sun.misc.Unsafe;

import com.stabilise.util.TheUnsafe;
import com.stabilise.util.annotation.ThreadSafe;

/**
 * A minimal implementation of a concurrent list which supports {@link
 * #add(Object) adding} and {@link #iterator() iteration}.
 * 
 * <p>Note that iterating over a ConcurrentClearingList clears it.
 * 
 * @see ConcurrentClearingQueue
 */
@ThreadSafe
public class ConcurrentClearingList<E> implements Iterable<E> {
	
	// Unsafe stuff -----------------------------------------------------------
	
	private static final Unsafe unsafe = TheUnsafe.get();
	private static final long tailOffset, nextOffset;
	
	static {
		try {
			tailOffset = offsetFor(ConcurrentClearingList.class, "tail");
			nextOffset = offsetFor(ConcurrentClearingList.Node.class, "next");
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
		
		/** The item. Never null unless dummy head/tail node. */
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
	private volatile Node head = new Node();
	
	/** Tail node.
	 * Invariants:
	 * - Never null
	 * - next is null
	 * Non-invariants:
	 * - item is null if list is empty
	 * - head is the same as tail if list is empty 
	 */
	private volatile Node tail = head;
	
	
	/** Attempts to CAS the tail node and silently ignores failure. */
	private void casTail(Node exp, Node nxt) {
		unsafe.compareAndSwapObject(this, tailOffset, exp, nxt);
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
	 * 
	 * @throws NullPointerException if {@code e} is {@code null}.
	 */
	public void add(E e) {
		if(e == null)
			throw new NullPointerException();
		Node n = new Node(e);
		Node t;
		// a successful CAS 'locks' the tail for this thread
		while(!(t = tail).casNext(null, n)); // retry if this fails
		// This is allowed to fail if tail is wiped via iterator()
		casTail(t, n);
	}
	
	/**
	 * Returns an iterator over the elements in this list, and clears this
	 * list. The returned iterator does not support remove, as it is
	 * meaningless.
	 */
	@Override
	public Iterator<E> iterator() {
		Node h = head.swapNext(null);
		// It's alright if tail lags behind head; this simply means an element
		// added concurrently will be tacked onto the chain to be iterated over
		// rather than the one being reset to.
		tail = head;
		return new Itr(h);
	}
	
	// Iterator impl. ---------------------------------------------------------
	
	private class Itr implements Iterator<E> {
		
		private Node next;
		
		Itr(Node n) {
			next = n;
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}
		
		@Override
		public E next() {
			try {
				E e = next.item;
				next = next.next;
				return e;
			} catch(NullPointerException e) { throw new NoSuchElementException(); }
		}
		
	}
	
}
