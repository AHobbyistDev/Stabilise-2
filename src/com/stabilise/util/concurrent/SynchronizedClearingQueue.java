package com.stabilise.util.concurrent;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.collect.ClearingQueue;


@ThreadSafe
public class SynchronizedClearingQueue<E> implements ClearingQueue<E> {
	
	// Node Class -------------------------------------------------------------
	
	/**
	 * Node class. Each node stores its item and links to the next node.
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
		
	}
	
	/** Synchronize on this to change things. */
	private final Node head = new Node();
	private Node tail = head;
	
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
		return head.next == null;
	}
	
	@Override
	public void add(E e) {
		Node n = new Node(e);
		synchronized(head) {
			tail.next = n;
			tail = n;
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		Node n;
		synchronized(head) {
			if(head.next == null)
				return Collections.emptyIterator();
			n = head.next;
			head.next = null;
			tail = head;
		}
		return new Itr(n);
	}
	
	@Override
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
			return next != null;
		}
		
		@Override
		public E next() {
			if(next == null)
				throw new NoSuchElementException();
			final E e = next.item;
			next = next.next;
			return e;
		}
		
	}
	
}
