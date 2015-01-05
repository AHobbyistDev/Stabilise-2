package com.stabilise.util.collect;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A lightweight linked list implementation which is designed solely for
 * easy ordered iteration of its elements without the bulk of features which
 * won't be used.
 * 
 * <p>This class supports the following operations; all others will throw an
 * {@code UnsupportedOperationException}.
 * 
 * <ul>
 * <li>{@link #size()}
 * <li>{@link #isEmpty()}
 * <li>{@link #contains(Object)}
 * <li>{@link #toArray()}
 * <li>{@link #toArray(Object[])}
 * <li>{@link #add(Object)} - main usage method
 * <li>{@link #get(int)}
 * <li>{@link #remove(int)}
 * <li>{@link #clear()}
 * <li>{@link #iterator()} - main usage method
 * </ul>
 * 
 * <p>An instance of this class may be preferable over a {@code LinkedList} if
 * the only operations which will be performed with it are {@link #add(Object)
 * appending} and {@link #iterator() iterating}.
 */
public class LightLinkedList<E> implements List<E> {
	
	protected int size = 0;
	protected Node<E> head = null;
	protected Node<E> tail = null;
	/** Maintain the iterator since it's wasteful to keep recreating it. */
	protected final AbstractItr iterator;
	
	
	/**
	 * Creates a new LightLinkedList.
	 */
	public LightLinkedList() {
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
		if(head == null) {
			head = new Node<E>(e);
			tail = head;
		} else {
			tail.next = new Node<E>(e);
		}
		size++;
		return true;
	}
	
	/**
	 * {@inheritDoc}
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
			head = oldHead.next;
			if(size == 0)
				tail = null;
			return oldHead.e;
		} else {
			Node<E> node = head;
			while(--index > 0)
				node = node.next;
			Node<E> nodeToRemove = node.next; // never null
			node.next = nodeToRemove.next;
			if(node.next == null)
				tail = node;
			return nodeToRemove.e;
		}
	}
	
	/**
	 * Gets the node at the specified index.
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
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Throws UnsupportedOperationException.
	 */
	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
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
		
		/**
		 * Wipes pointers to help the GC.
		 */
		void wipe() {
			e = null;
			next = null;
		}
	}
	
	/**
	 * Your standard iterator implementation.
	 */
	protected abstract class AbstractItr implements Iterator<E> {
		
		Node<E> prev;
		Node<E> lastReturned;
		
		protected AbstractItr() {
			reset();
		}
		
		/**
		 * Called on construction and when this iterator is returned as per
		 * {@link List#iterator()}.
		 */
		protected void reset() {
			prev = lastReturned = head;
		}
		
		@Override
		public boolean hasNext() {
			return lastReturned.next != null;
		}
		
	}
	
	/**
	 * Your standard iterator implementation.
	 */
	protected class Itr extends AbstractItr {
		
		int nextIndex; // index of lastReturned + 1
		
		@Override
		protected void reset() {
			prev = lastReturned = head;
			nextIndex = 0;
		}
		
		@Override
		public boolean hasNext() {
			return nextIndex > size;
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
