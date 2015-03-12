package com.stabilise.util.concurrent;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;

import com.stabilise.util.annotation.ThreadSafe;

/**
 * A minimal implementation of a concurrent queue which supports {@link
 * #add(Object)} for queueing, and {@link #toArray()} for obtaining an iterable
 * view of this queue.
 * 
 * <p>This class does not utilise explicit locking, and instead offers high
 * concurrency through use of compare-and-swap instructions.
 */
@ThreadSafe
public class ConcurrentClearingQueue<E> {
	
	private class Node {
		/** The element held by this node. */
		final E item;
		/** The node before this one. If this is {@code null}, this is the
		 * dummy head node, and {@link #pos} is 0. */
		Node prev;
		/** The node's position in the queue, plus 1. 0 is the pos of the dummy
		 * head node, 1 is the pos of the first real node. If this is the last
		 * node, this value is the size of the list.
		 * <p>Note this value is more or less unused unless this is the tail
		 * node, but this is a tradeoff for atomicity guarantees.*/
		int pos;
		
		/** Creates a node holding the item. */
		Node(E item) {
			this.item = item;
		}
		
		/** Creates a dummy head node. */
		Node() {
			item = null;
			prev = null;
			pos = 0;
		}
		
	}
	
	/** The tail element. This is the dummy head node if the queue is empty. */
	private final AtomicReference<Node> tail = new AtomicReference<>(new Node());
	// Note we don't bother with a pointer to the head element since the
	// specification of this class is so minimal that we don't require it.
	
	
	/**
	 * Adds an element to this queue.
	 * 
	 * @param e The element to add. {@code null} is allowed.
	 */
	public void add(E e) {
		Node node = new Node(e);
		Node oldTail;
		do {
			oldTail = tail.get();
			node.prev = oldTail;
			node.pos = oldTail.pos + 1;
			// We use the volatile semantics of AtomicReference to publish the
			// fields of node, which have no inherent synchronisation.
		} while(!tail.compareAndSet(oldTail, node));
	}
	
	/**
	 * Clears this queue.
	 */
	public void clear() {
		tail.set(new Node());
	}
	
	/**
	 * Returns the size of this queue.
	 */
	public int size() {
		return tail.get().pos;
	}
	
	/**
	 * @return {@code true} if this queue is empty; {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * Creates an array with this queue's elements as its members and returns
	 * it.
	 */
	public E[] toArray() {
		return toArray0(tail.get());
	}
	
	/**
	 * Creates an array with this queue's elements as its members and returns
	 * it, and atomically clears this queue.
	 */
	public E[] toArrayWithClear() {
		return toArray0(tail.getAndSet(new Node()));
	}
	
	private E[] toArray0(Node n) {
		@SuppressWarnings("unchecked")
		E[] a = (E[])Array.newInstance(n.item.getClass(), n.pos);
		while(n.pos > 0) {
			a[n.pos - 1] = n.item;
			n = n.prev;
		}
		return a;
	}
	
}
