package com.stabilise.util.collect;

import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import com.stabilise.util.annotation.NotThreadSafe;

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
 * <p>Iterators over elements of a {@code LightLinkedList} will never throw
 * {@code ConcurrentModificationExceptions}, and as such it is safe to {@code
 * add} elements while iterating.
 * 
 * <p>Refer to the documentation of a method before using it, as many methods
 * of this class will throw an {@code UnsupportedOperationException}.
 */
@NotThreadSafe
public class LightLinkedList<E> extends AbstractCollection<E> implements List<E> {
	
	protected int size = 0;
	/** The head node (i.e. the one returned by get(0)). This is null iff size
	 * is 0. */
	protected Node<E> head = null;
	/** The head node (i.e. the one returned by get(size - 1)). This is null
	 * iff size is 0. */
	protected Node<E> tail = null;
	/** Maintain the iterator since it's wasteful to keep recreating it. */
	protected final AbstractItr iterator = getIterator();
	
	
	protected AbstractItr getIterator() {
		return new Itr();
	}
	
	@Override
	public int size() {
		return size;
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
	 * Appends the contents of this list (order is preserved) into the
	 * specified list, and clears this list. This is a highly efficient bulk
	 * transfer operation with constant-time performance.
	 * 
	 * @throws NullPointerException if {@code list} is {@code null}.
	 */
	public void dumpInto(LightLinkedList<E> list) {
		if(size == 0)
			return;
		
		if(list.head == null)
			list.head = head;
		else
			list.tail.next = head;
		list.tail = tail;
		list.size += size;
		
		// clear this list
		head = tail = null;
		size = 0;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method has constant-time performance.
	 */
	@Override
	public boolean add(E e) {
		if(head == null)
			head = tail = new Node<E>(e);
		else {
			tail.next = new Node<E>(e);
			tail = tail.next;
		}
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
			return unlink(node, node.next);
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
	 * 
	 * @return The element of the removed node.
	 */
	@SuppressWarnings("unused")
	private E unlink(Node<E> node) {
		if(node == head) {
			return unlink(null, node);
		} else {
			Node<E> prev = head;
			while(prev.next != node)
				prev = prev.next;
			return unlink(prev, node);
		}
	}
	
	/**
	 * Unlinks the specified node. size is decremented.
	 * 
	 * @param prev The node before {@code node}. A value of {@code null}
	 * implies {@code node == head}.
	 * @param node The node to remove.
	 * 
	 * @return The element of the removed node.
	 */
	private E unlink(Node<E> prev, Node<E> node) {
		size--;
		if(node == head) // i.e. prev == null
			head = node.next;
		else
			prev.next = node.next;
		if(node == tail)
			tail = prev;
		return node.e;
	}
	
	private E deepUnlink(Node<E> prev, Node<E> node) {
		size--;
		if(node == head) // i.e. prev == null
			head = node.next;
		prev.next = node.next; // needed for proper iterator removal
		if(node == tail)
			tail = prev;
		return node.e;
	}
	
	@Override
	public void clear() {
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
	 * List node. Stores its element and the pointer to the next node.
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
		
	}
	
	/**
	 * Your standard iterator implementation which is reused every time {@link
	 * #iterator()} is invoked.
	 */
	protected abstract class AbstractItr implements Iterator<E> {
		
		/** Points to head node. We maintain a reference so we don't need to
		 * keep reconstructing a node every time the iterator is reset. */
		private Node<E> headWrapper;
		/** The element most recently returned by {@link #next()}. */
		Node<E> lastReturned;
		
		
		protected AbstractItr() {
			headWrapper = new Node<E>(head);
		}
		
		/**
		 * Called when this iterator is returned as per {@link
		 * List#iterator()}.
		 */
		protected void reset() {
			headWrapper.next = head;
			lastReturned = headWrapper;
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
		boolean removed;
		
		
		@Override
		protected void reset() {
			super.reset();
			removed = true;
		}
		
		@Override
		public E next() {
			if(!hasNext())
				throw new NoSuchElementException();
			prev = lastReturned;
			lastReturned = prev.next;
			removed = false;
			return lastReturned.e;
		}
		
		@Override
		public void remove() {
			if(removed)
				throw new IllegalStateException();
			removed = true;
			deepUnlink(prev, lastReturned);
			lastReturned = prev;
		}
		
	}
	
}
