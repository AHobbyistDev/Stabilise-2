package com.stabilise.util.concurrent;

import static com.stabilise.util.TheUnsafe.unsafe;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.stabilise.util.annotation.ThreadSafe;

/**
 * A {@link ClearingQueue} which does not utilise locking, and instead offers
 * high concurrency through compare-and-swap instructions.
 * 
 * @deprecated Use {@link SynchronizedClearingQueue} instead - this class
 * currently scales very poorly for multiple threads. TODO: Get a better
 * algorithm for this class!
 */
@ThreadSafe
public class ConcurrentClearingQueue<E> implements ClearingQueue<E> {
	
	// Unsafe stuff -----------------------------------------------------------
	
	private static final long tailOffset, nextOffset;
	
	static {
		try {
			tailOffset = offsetFor(ConcurrentClearingQueue.class, "tail");
			nextOffset = offsetFor(ConcurrentClearingQueue.Node.class, "next");
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
	 * - item is null if queue is empty
	 * - head is the same as tail if queue is empty
	 * - if next is null, the queue is in its normal state and can be appended
	 *   to
	 * - if next is not null, an element is currently in the process of being
	 *   added, and tail will soon be replaced with the new tail node
	 *   - next may temporary be set to itself (i.e. tail.next == tail) to
	 *     abuse this protocol to lock this queue when extracting it to an
	 *     iterator.
	 */
	private volatile Node tail = head;
	
	
	/** Attempts to CAS the tail node and returns true if successful. */
	private boolean casTail(Node exp, Node nxt) {
		return unsafe.compareAndSwapObject(this, tailOffset, exp, nxt);
	}
	
	@Override
	public int size() {
		int size = 0;
		Node n = head;
		while((n = n.next) != null)
			size++;
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		// An alternative condition, which is however subject to lag while the
		// queue is in the intermediate state:
		// return head == tail;
		return head.next == null;
	}
	
	@Override
	public void add(E e) {
		Node t, n = new Node(e);
		// a successful CAS 'locks' the tail for this thread
		while(!(t = tail).casNext(null, n)); // retry until this succeeds
		casTail(t, n); // Allowed to fail if tail is wiped by iterator()
	}
	
	@Override
	public Iterator<E> iterator() {
		// We'll use a form of the double-check idiom to prevent unnecessary
		// CASing (if a thread is spin iterating, letting it attempt to CAS
		// tail.next carelessly may lock out other threads indefinitely).
		if(isEmpty())
			return Collections.emptyIterator();
		// We CAS-in tail recursion to 'plug' the tail up; other threads can't
		// add anything to it while tail.next != null
		while(!tail.casNext(null, tail));
		// If the queue was cleared due to an iterator in another thread, then
		// head == tail might be true, and so setting head.next to null will
		// also set tail.next to null, which WILL result in an NPE being thrown
		// in Itr.hasNext(). We'll do an extra check here to ensure this does
		// not happen.
		if(head == tail) // no race since we've got the queue 'locked'
			return Collections.emptyIterator();
		// Now, we reset the head node and extract the queue proper
		Node n = head.swapNext(null);
		tail = head; // Reset the tail node; queue works from here on out
		return new Itr(n);
	}
	
	@Override
	public Iterator<E> nonClearingIterator() {
		return new Itr(head.next);
	}
	
	// Iterator impl. ---------------------------------------------------------
	
	private class Itr implements Iterator<E> {
		
		private Node next;
		/** Testing next != next.next in hasNext() is insufficient as if next
		 * is the tail node, this will return a false negative (remember,
		 * tail.next = tail). So, we use a state value to make sure we ignore
		 * the first negative result.
		 * The first bit means we've encountered the tail.
		 * The second bit means we've got the tail from next().
		 * Using fancy bit-level operations, we basically make sure everything
		 * works out nice and well.
		 */
		private int tailState = 0;
		
		/** first should never be null */
		Itr(Node first) {
			next = first;
		}
		
		@Override
		public boolean hasNext() {
			return next != next.next || (tailState |= 1) == 1;
		}
		
		@Override
		public E next() {
			if(tailState > 0 && (tailState ^ (tailState ^= 2)) == 0)
				throw new NoSuchElementException();
			final E e = next.item;
			next = next.next;
			return e;
		}
		
	}
	
}
