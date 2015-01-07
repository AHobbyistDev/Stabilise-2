package com.stabilise.util.collect;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A lightweight linked list implementation which doesn't include the bulk of
 * features that {@link java.util.LinkedList LinkedList} possesses, since in
 * most cases they won't be needed anyway.
 * 
 * <p>An instance of this class may as such be preferable over a {@code
 * LinkedList} if the only operations which will be performed with it are
 * {@link #add(Object) appending} and {@link #iterator() iterating}, and for
 * which removal of elements only occurs while iterating.
 * 
 * <p>Though this class is not thread-safe, it supports {@link #add(Object)
 * add} operations while being iterated over.
 * 
 * <p>Refer to the documentation of a method before using it, as many methods
 * of this class will throw an {@code UnsupportedOperationException}.
 */
public class LightweightLinkedList<E> extends AbstractCollection<E> implements List<E> {
	
	protected int size = 0;
	protected Node<E> head = null;
	protected Node<E> tail = null;
	/** Maintain the iterator since it's wasteful to keep recreating it. */
	protected final AbstractItr iterator;
	
	
	/**
	 * Creates a new LightLinkedList.
	 */
	public LightweightLinkedList() {
		iterator = getIterator();
	}
	
	protected AbstractItr getIterator() {
		return new Itr();
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public boolean isEmpty() {
		return size == 0;
	}
	
	@Override
	public boolean contains(Object o) {
		for(E e : this)
			if(e == o)
				return true;
		return false;
	}
	
	@Override
	public Object[] toArray() {
		// Yoinked from LinkedList
		Object[] arr = new Object[size];
		int i = 0;
		for(Node<E> node = head; node != null; node = node.next)
			arr[i++] = node.e;
		return arr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		// Yoinked from LinkedList
		if (a.length < size)
			a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
		int i = 0;
		Object[] result = a;
		for (Node<E> x = head; x != null; x = x.next)
			result[i++] = x.e;

		if (a.length > size)
			a[size] = null;

		return a;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method has O(1) performance.
	 */
	@Override
	public boolean add(E e) {
		if(head == null)
			head = tail = new Node<E>(e);
		else
			tail.next = new Node<E>(e);
		size++;
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method has performance of O(index).
	 * 
	 * @throws IndexOutOfBoundsException if {@link index < 0 || index >=
	 * size()}.
	 */
	@Override
	public E get(int index) {
		return getNode(index).e;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method has performance of O(index).
	 * 
	 * @throws IndexOutOfBoundsException if {@link index < 0 || index >=
	 * size()}.
	 */
	@Override
	public E remove(int index) {
		if(index < 0 || index >= size)
			throw new IndexOutOfBoundsException("Invalid index " + index);
		size--;
		if(index == 0) {
			Node<E> oldHead = head;
			head = oldHead.next; // null if size == 0
			if(size == 0)
				tail = null;
			return oldHead.e;
		} else {
			Node<E> node = head;
			while(--index > 0)
				node = node.next;
			Node<E> nodeToRemove = node.next; // never null
			node.next = nodeToRemove.next; // bridge nodes
			if(node.next == null)
				tail = node;
			return nodeToRemove.e;
		}
	}
	
	/**
	 * Gets the node at the specified index.
	 * 
	 * <p>This method has performance of O(index).
	 * 
	 * @throws IndexOutOfBoundsException if {@link index < 0 || index >=
	 * size()}.
	 */
	private Node<E> getNode(int index) {
		if(index < 0 || index >= size)
			throw new IndexOutOfBoundsException("Invalid index " + index);
		Node<E> node = head;
		while(index-- > 0)
			node = node.next;
		return node;
	}
	
	/**
	 * Unlinks the specified node from the list. size is decremented. This
	 * method is inefficient as the previous node must first be found for
	 * proper unlinking.
	 */
	@SuppressWarnings("unused")
	private void unlink(Node<E> node) {
		if(node == head) {
			unlink(null, node);
		} else {
			Node<E> prev = head;
			while(prev.next != node)
				prev = prev.next;
			unlink(prev, node);
		}
	}
	
	/**
	 * Unlinks the specified node. size is decremented.
	 * 
	 * @param prev The node before {@code node}. A value of {@code null}
	 * implies {@code node == head}.
	 * @param node The node to remove.
	 */
	private void unlink(Node<E> prev, Node<E> node) {
		size--;
		if(prev == null) // i.e. node == head
			head = node.next;
		else
			prev.next = node.next;
		if(node == tail)
			tail = prev;
	}
	
	@Override
	public void clear() {
		// Since LinkedList detatches all nodes rather than simply nullifying
		// head and tail, I figure I may as well do that too.
		for(Node<E> node = head; node != null;) {
			Node<E> next = node.next;
			node.wipe();
			node = next;
		}
		head = tail = null;
		size = 0;
	}
	
	@Override
	public Iterator<E> iterator() {
		iterator.reset();
		return iterator;
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public ListIterator<E> listIterator() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public ListIterator<E> listIterator(int index) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		if(isEmpty())
			return false;
		if(o == null) {
			if(size == 1) {
				if(head.e == null) {
					unlink(null, head);
					return true;
				}
			} else {
				for(Node<E> x = head; x.next != null; x = x.next) {
					if(x.next.e == null) {
						unlink(x, x.next);
						return true;
					}
				}
			}
		} else {
			if(size == 1) {
				if(o.equals(head.e)) {
					unlink(null, head);
					return true;
				}
			} else {
				for(Node<E> x = head; x.next != null; x = x.next) {
					if(o.equals(x.next.e)) {
						unlink(x, x.next);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public E set(int index, E element) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public void add(int index, E element) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public int indexOf(Object o) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public int lastIndexOf(Object o) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * List node. Stores its element and the pointer to the enxt node.
	 */
	protected static class Node<E> {
		
		E e;
		Node<E> next;
		
		Node(E e) {
			this.e = e;
		}
		
		Node(Node<E> next) {
			this.next = next;
		}
		
		/**
		 * Nullifies pointers to help the GC.
		 */
		void wipe() {
			e = null;
			next = null;
		}
		
	}
	
	/**
	 * Your standard iterator implementation which is reused every time {@link
	 * #iterator()} is invoked.
	 */
	protected abstract class AbstractItr implements Iterator<E> {
		
		/** The element most recently returned by {@link #next()}. */
		Node<E> lastReturned;
		
		
		protected AbstractItr() {
			// nothing to see here, move along
		}
		
		/**
		 * Called when this iterator is returned as per {@link
		 * List#iterator()}.
		 */
		protected void reset() {
			lastReturned = new Node<E>(head);
		}
		
		@Override
		public boolean hasNext() {
			return lastReturned.next != null;
		}
		
	}
	
	/**
	 * Your actual standard iterator implementation.
	 */
	protected class Itr extends AbstractItr {
		
		/** The element preceding {@link #lastReturned}. */
		Node<E> prev;
		/** Equivalent to indexOf(lastReturned)+1 */
		int nextIndex;
		
		
		@Override
		protected void reset() {
			super.reset();
			nextIndex = 0;
		}
		
		@Override
		public E next() {
			if(!hasNext())
				throw new NoSuchElementException();
			prev = lastReturned;
			lastReturned = prev.next;
			nextIndex++;
			return lastReturned.e;
		}
		
		@Override
		public void remove() {
			size--;
			nextIndex--;
			if(nextIndex == 0) { // lastReturned was head
				head = lastReturned.next;
				if(size == 0)
					tail = null;
				lastReturned.wipe();
			} else {
				prev.next = lastReturned.next;
				if(prev.next == null)
					tail = prev;
			}
		}
		
	}
	
}
