package com.stabilise.util.collect;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.stabilise.util.annotation.NotThreadSafe;

/**
 * An alternative ArrayList implementation with some design adjustments.
 * 
 * <p>This class permits list modification during iteration, and as such
 * iterators will not throw {@code ConcurrentModificationExceptions}.
 * 
 * <p>An unordered list variant is available through {@link
 * #unordered(int, float)}, which avoids memory copies on element removal,
 * which can improve performance.
 */
@NotThreadSafe
public class LightArrayList<E> extends AbstractList<E> implements RandomAccess {
	
	/** The backing array.
	 * Invariant: length >= size */
	protected E[] data;
	/** The size of this list.
	 * Invariant: data.length >= size */
	protected int size = 0;
	/** The array resize scaling factor.
	 * Invariant: >= 1.0 */
	protected float scaleFactor;
	
	
	/**
	 * Creates a new LightArrayList with an initial capacity of 16 and a scaling
	 * factor of {@code 1.5}.
	 */
	public LightArrayList() {
		this(16, 1.5f);
	}
	
	/**
	 * Creates a new LightArrayList.
	 * 
	 * @param capacity The initial internal array length.
	 * @param scaleFactor The number by which the internal array length is
	 * multiplied when it needs to be expanded to accommodate new elements. If
	 * this is {@code 1.0}, the array length is increased by only {@code 1}.
	 * 
	 * @throws NegativeArraySizeException if {@code capacity} is negative.
	 * @throws IllegalArgumentException if {@code scaleFactor < 1.0}.
	 */
	public LightArrayList(int capacity, float scaleFactor) {
		@SuppressWarnings("unchecked")
		final E[] arr = (E[])new Object[capacity];
		data = arr;
		if(scaleFactor < 1.0f)
			throw new IllegalArgumentException("Scale factor must be >= 1.0!");
		this.scaleFactor = scaleFactor;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	/**
	 * Resizes the backing array to {@link #size()}. This operation is suitable
	 * for conserving memory when it is known no more elements will be added to
	 * this list.
	 */
	public void trimToSize() {
		resize(size);
	}
	
	/**
	 * Resizes the backing array to {@code length} if it is smaller than {@code
	 * length}. This method can be useful for preventing excessive array
	 * expansions, which can be wasteful.
	 */
	public void ensureInternalLength(int length) {
		if(data.length < length)
			resize(length);
	}
	
	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Itr();
	}
	
	/**
	 * Returns an {@code Iterator} which filters out any {@code null} array
	 * elements. The returned iterator does not support {@code remove()}.
	 */
	public Iterator<E> iteratorNullsFiltered() {
		return Iterators.filter(iterator(), Predicates.notNull());
	}
	
	@Override
	public E[] toArray() {
		return Arrays.copyOf(data, size);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		// Copied from ArrayList
        if(a.length < size)
            return (T[]) Arrays.copyOf(data, size, a.getClass());
        System.arraycopy(data, 0, a, 0, size);
        if(a.length > size)
            a[size] = null; // as per the contract of this method
        return a;
	}
	
	@Override
	public boolean add(E e) {
		if(data.length == size) // length is never < size
			expand();
		data[size++] = e;
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
		for(int i = 0; i < size; i++) {
			if(data[i].equals(o)) {
				remove(i);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if(c.size() == 0) return false;
		expandAndShift(index, c.size());
		for(E e : c)
			data[index++] = e;
		return true;
	}
	
	@Override
	public void clear() {
		size = 0;
		resize(0);
	}
	
	@Override
	public E get(int index) {
		rangeCheckUpper(index);
		return data[index];
	}
	
	@Override
	public E set(int index, E element) {
		rangeCheckUpper(index);
		E e = data[index];
		data[index] = element;
		return e;
	}
	
	@Override
	public void add(int index, E element) {
		rangeCheck(index);
		expandAndShift(index, 1);
		data[index] = element;
	}
	
	@Override
	public E remove(int index) {
		rangeCheckUpper(index);
		E e = data[index];
		shrinkAndShift(index, 1);
		return e;
	}
	
	/**
	 * Removes the last element of this list and returns it.
	 * 
	 * @return The last element, or {@code null} if this list is empty.
	 */
	public E removeLast() {
		if(size == 0) return null;
		E e = data[--size];
		data[size] = null;
		return e;
	}
	
	/**
	 * Removes the last element of this list if it is equal to {@code e} by the
	 * equality operator ({@code ==}).
	 * 
	 * @return {@code true} if the last element was removed; {@code false}
	 * otherwise.
	 */
	public boolean removeLast(E e) {
		if(size == 0) return false;
		if(data[size-1] != e) return false;
		data[--size] = null;
		return true;
	}
	
	/**
	 * Removes a number of elements beginning at the specified index, shifting
	 * any elements to their right left (subtracting {@code amount} from their
	 * indices).
	 * 
	 * <p>Note that even for an unordered list, this method uses an array copy.
	 * 
	 * @param index The index of the first element to remove.
	 * @param amount The number of elements to remove.
	 * 
	 * @throws IndexOutOfBoundsException if {@code index < 0 || index + amount
	 * > size}.
	 */
	public void removeAll(int index, int amount) {
		if(index < 0 || index + amount > size)
			throw new IndexOutOfBoundsException("index < 0 || index + amount > size");
		shrinkAndShift(index, amount);
	}
	
	@Override
	public int indexOf(Object o) {
		if(o == null) {
			for(int i = 0; i < size; i++)
				if(data[i] == null)
					return i;
		} else {
			for(int i = 0; i < size; i++)
				if(data[i].equals(o))
					return i;
		}
		return -1;
	}
	
	@Override
	public int lastIndexOf(Object o) {
		if(o == null) {
			for(int i = size-1; i >= 0; i--)
				if(data[i] == null)
					return i;
		} else {
			for(int i = size-1; i >= 0; i--)
				if(data[i].equals(o))
					return i;
		}
		return -1;
	}
	
	@Override
	public ListIterator<E> listIterator() {
		return new ListItr(0);
	}
	
	@Override
	public ListIterator<E> listIterator(int index) {
		if(index < 0 || index > size) // method specs mandate we allow cases where index == size
			throw new IndexOutOfBoundsException("Invalid index " + index);
		return new ListItr(index);
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	private void resize(int newLength) {
		data = Arrays.copyOf(data, newLength);
	}
	
	/**
	 * Expands the array by the scaling factor to the next {@link
	 * #scaleLength() scaled length}. The array size is guaranteed to increase
	 * by at least 1.
	 */
	private void expand() {
		resize(scaleLength());
	}
	
	/**
	 * Expands the array if necessary to hold the specified number of elements.
	 * If an ordinary {@link #expand()} is insufficient, the array is directly
	 * resized to {@code length}.
	 */
	private void expandToHold(int length) {
		if(data.length < length)
			resize(Math.max(length, scaleLength()));
	}
	
	private int scaleLength() {
		return (int)(data.length * scaleFactor) + 1;
	}
	
	/**
	 * {@link #expandToHold(int) Expands} the array if necessary to hold {@code
	 * size + amount}, shifting every element to the right of {@code index} by
	 * {@code amount} places. In other words, this creates an empty gap in the
	 * array at {@code index} with length {@code amount}.
	 * 
	 * <p>This method increments {@code size} by {@code amount}, and may change
	 * the length of the backing array.
	 */
	private void expandAndShift(int index, int amount) {
		size += amount;
		expandToHold(size);
		System.arraycopy(data, index, data, index + amount, amount);
	}
	
	/**
	 * The reverse of {@link #expandAndShift(int, int)}; this method removes
	 * {@code amount} many elements at {@code index} and moves everything to
	 * the right of it left.
	 * 
	 * <p>This method decrements {@code size} by {@code amount}, but does not
	 * change the length of the backing array.
	 * 
	 * @param index The index. It is implicitly trusted that this is valid.
	 * @param amount The number of elements to remove.
	 * 
	 * @throws IndexOutOfBoundsException if copying would cause access of data
	 * outside array bounds.
	 */
	private void shrinkAndShift(int index, int amount) {
		size -= amount;
		System.arraycopy(data, index+amount, data, index, size-index); //size-index == oldsize-index-amount
		Arrays.fill(data, size, size+amount, null);
	}
	
	protected void rangeCheck(int index) {
		if(index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException("Invalid index " + index);
	}
	
	protected void rangeCheckUpper(int index) {
		if(index >= size)
			throw new ArrayIndexOutOfBoundsException("Invalid index " + index);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Creates a new unordered LightArrayList.
	 * 
	 * <p>The returned LightArrayList is identical to an ordinary one with the
	 * exception that remove operations do not involve an array copy; the last
	 * element of the array is simply moved to the position of the removed
	 * element.
	 * 
	 * @param capacity The initial internal array length.
	 * @param scaleFactor The number by which the internal array length is
	 * multiplied when it needs to be expanded to accommodate new elements. If
	 * this is {@code 1.0}, the array length is increased by only {@code 1}.
	 * 
	 * @throws NegativeArraySizeException if {@code capacity} is negative.
	 * @throws IllegalArgumentException if {@code scaleFactor < 1.0}.
	 */
	public static <E> LightArrayList<E> unordered(int capacity, float scaleFactor) {
		return new Unordered<E>(capacity, scaleFactor);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	private class Itr implements Iterator<E> {
		
		int cursor = 0;
		
		@Override
		public boolean hasNext() {
			return cursor < size;
		}
		
		@Override
		public E next() {
			try {
				return data[cursor++];
			} catch(ArrayIndexOutOfBoundsException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}
		
		@Override
		public void remove() {
			LightArrayList.this.remove(cursor - 1);
		}
		
	}
	
	private class ListItr extends Itr implements ListIterator<E> {
		
		int cursor;
		int lastRet = -1;
		
		ListItr(int cursor) {
			this.cursor = cursor;
		}
		
		@Override
		public boolean hasPrevious() {
			return cursor != 0;
		}
		
		@Override
		public E next() {
			lastRet = cursor;
			return super.next();
		}
		
		@Override
		public E previous() {
			try {
				return data[lastRet = --cursor];
			} catch(ArrayIndexOutOfBoundsException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}
		
		@Override
		public int nextIndex() {
			return cursor;
		}
		
		@Override
		public int previousIndex() {
			return cursor - 1;
		}
		
		@Override
		public void set(E e) {
			if(lastRet == -1)
				throw new IllegalStateException();
			LightArrayList.this.data[lastRet] = e;
		}
		
		@Override
		public void add(E e) {
			if(lastRet == -1)
				throw new IllegalStateException();
			LightArrayList.this.add(lastRet, e);
			cursor++;
			lastRet = -1;
		}
		
	}
	
	private static class Unordered<E> extends LightArrayList<E> {
		
		public Unordered(int capacity, float scaleFactor) {
			super(capacity, scaleFactor);
		}
		
		@Override
		public E remove(int index) {
			rangeCheckUpper(index);
			E e = data[index];
			data[index] = data[--size];
			data[size] = null;
			return e;
		}
		
	}
	
}