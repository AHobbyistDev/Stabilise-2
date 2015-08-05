package com.stabilise.util.collect;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * An UnrolledLinkedList is a linked list whose nodes contain arrays of
 * elements rather than a single element. This provides a sort of middle-ground
 * between an ArrayList and a LinkedList, sharing many of the benefits and
 * drawbacks of both, in addition to potentially offering improved cache
 * performance.
 * 
 * <p>This class offers a minimal implementation of an unrolled linked list,
 * providing mainly {@link #add(Object) add}, {@link #iterator() iterate}, and
 * {@link #clear() clear} operations.
 */
@NotThreadSafe
public class UnrolledLinkedList<E> extends AbstractCollection<E> implements List<E> {
	
	/** Array size for each node. */
	private final int nodeSize;
	/** Number of nodes. */
	private int numNodes = 0;
	/** Head node. Never null. It's {@code elems} is always a 0-length array.
	 * If {@code next} is null then the list is empty. */
	private Node head;
	/** Tail node. Never null. Equal to {@code head} if the list is empty. This
	 * field is just here for convenience; it is always reachable through
	 * {@code head} by traversing the list. */
	private Node tail;
	
	
	/**
	 * Creates a new UnrolledLinkedList.
	 * @param nodeSize
	 */
	public UnrolledLinkedList(int nodeSize) {
		if(nodeSize < 1)
			throw new IllegalArgumentException("nodeSize < 1");
		this.nodeSize = nodeSize;
		
		head = tail = new Node(0);
	}
	
	@Override
	public boolean add(E e) {
		if(tail.isFull())
			addNewNode();
		tail.add(e);
		return true;
	}
	
	private void addNewNode() {
		tail.next = new Node(nodeSize);
		tail = tail.next;
		numNodes++;
	}
	
	@Override
	public E get(int index) {
		Node n = head;
		while((n = n.next) != null) {
			if(index < n.size)
				return n.elems[index];
			else
				index -= n.size;
		}
		return null;
	}
	
	/**
	 * Returns the number of elements in this list. This is not a constant-time
	 * operation.
	 */
	@Override
	public int size() {
		int size = 0;
		Node n = head;
		while((n = n.next) != null)
			size += n.size;
		return size;
	}
	
	/**
	 * Returns the current capacity of this list, equivalent to the number of
	 * nodes times the node size/
	 */
	public int capacity() {
		return nodeSize * numNodes;
	}
	
	/**
	 * Returns the number of nodes in this list.
	 */
	public int nodes() {
		return numNodes;
	}
	
	@Override
	public void clear() {
		head.next = null;
		tail = head;
		numNodes = 0;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E set(int index, E element) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void add(int index, E element) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public E remove(int index) throws
			UnsupportedOperationException {
		return null;
	}
	
	@Override
	public int indexOf(Object o) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int lastIndexOf(Object o) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ListIterator<E> listIterator() throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ListIterator<E> listIterator(int index) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex) throws
			UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Returns an iterator over the elements in this list. The iterator does
	 * not support {@code remove()}.
	 */
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}
	
	/**
	 * Node class.
	 */
	private class Node {
		
		/** Next node. {@code null} if this is the tail node. */
		private Node next = null;
		/** Element data in this node. Never {@code null}. */
		private final E[] elems;
		/** The number of elements in this node. */
		private int size = 0;
		
		
		@SuppressWarnings("unchecked")
		public Node(int size) {
			elems = (E[])new Object[size];
		}
		
		public void add(E e) {
			elems[size++] = e;
		}
		
		public boolean isFull() {
			return size == elems.length;
		}
		
	}
	
	private class Itr implements Iterator<E> {
		
		private Node node = head;
		private int i = 0; // index in current node
		
		@Override
		public boolean hasNext() {
			return i < node.size ||    // sneaky way of resetting i
					((node = node.next) != null && node.size > (i = 0));
		}
		
		@Override
		public E next() {
			try {
				return node.elems[i++];
			} catch(Exception e) {
				throw new NoSuchElementException();
			}
		}
		
	}
	
}
