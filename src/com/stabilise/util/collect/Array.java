package com.stabilise.util.collect;

import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * This class provides a thin wrapper for an object array with some added
 * convenience methods.
 */
public class Array<E> implements Iterable<E> {
	
	protected E[] data;
	
	
	/**
	 * Creates a new Array with a default length of 16.
	 * 
	 * <p>Use of this constructor should be avoided if possible, as the runtime
	 * type of the backing array will be {@code Object[]}, which creates the
	 * possibility of typing errors.
	 */
	public Array() {
		this(16);
	}
	
	/**
	 * Creates a new Array.
	 * 
	 * <p>Use of this constructor should be avoided if possible, as the runtime
	 * type of the backing array will be {@code Object[]}, which creates the
	 * possibility of typing errors.
	 * 
	 * @param capacity The initial array length.
	 * 
	 * @throws NegativeArraySizeException if {@code capacity} is negative.
	 */
	public Array(int capacity) {
		@SuppressWarnings("unchecked")
		final E[] arr = (E[])new Object[capacity];
		data = arr;
	}
	
	/**
	 * Creates a new Array with a default length of 16.
	 * 
	 * @param elementClass The {@code Class} object of the element type {@code
	 * E}.
	 * 
	 * @throws NullPointerException if {@code elementClass} is {@code null}.
	 */
	public Array(Class<E> elementClass) {
		this(elementClass, 16);
	}
	
	/**
	 * Creates a new Array.
	 * 
	 * @param elementClass The {@code Class} object of the element type {@code
	 * E}.
	 * @param capacity The initial array length.
	 * 
	 * @throws NullPointerException if {@code elementClass} is {@code null}.
	 * @throws NegativeArraySizeException if {@code capacity < 0}.
	 */
	public Array(Class<E> elementClass, int capacity) {
		data = createArr(elementClass, capacity);
	}
	
	/**
	 * Creates a new Array with the specified array as its backing array.
	 * 
	 * @param data The backing array.
	 * 
	 * @throws NullPointerException if {@code data} is {@code null}.
	 */
	public Array(E[] data) {
		this.data = Preconditions.checkNotNull(data);
	}
	
	/**
	 * Returns this Array's backing array. Use this for iteration.
	 */
	public E[] get() {
		return data;
	}
	
	/**
	 * Gets the element at the specified index.
	 * 
	 * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or
	 * greater than {@link #length()}.
	 */
	public E get(int index) {
		return data[index];
	}
	
	/**
	 * Sets the element at the specified index.
	 * 
	 * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or
	 * greater than {@link #length()}.
	 */
	public void set(int index, E value) {
		data[index] = value;
	}
	
	/**
	 * Sets the element at the specified index, resizing the array if
	 * necessary as per {@link #ensureLength(int) ensureLength(index+1)}.
	 * 
	 * @throws ArrayIndexOutOfBoundsException if {@code index} is negative.
	 */
	public void setWithExpand(int index, E value) {
		ensureLength(index + 1);
		set(index, value);
	}
	
	/**
	 * Adds a new element to the array, increasing its length by 1. Note that
	 * this is an expensive operation as it requires a memory copy.
	 */
	public void add(E value) {
		resize(data.length + 1);
		data[data.length - 1] = value;
	}
	
	/**
	 * Returns the length of the backing array.
	 */
	public int length() {
		return data.length;
	}
	
	/**
	 * Ensures that the length of the backing array is at least that of {@code
	 * length}.
	 */
	public void ensureLength(int length) {
		if(data.length < length)
			resize(length);
	}
	
	/**
	 * Resizes the backing array.
	 * 
	 * @throws NegativeArraySizeException if {@code length} is negative.
	 */
	public void resize(int length) {
		E[] newArr = newArr(length);
		System.arraycopy(data, 0, newArr, 0, Math.min(data.length, length));
		data = newArr;
	}
	
	/**
	 * Returns a copy of this Array's backing array. The elements themselves
	 * are not copied.
	 */
	public E[] copy() {
		E[] copy = newArr(data.length);
		System.arraycopy(data, 0, copy, 0, data.length);
		return copy;
	}
	
	/**
	 * Returns a newly-constructed array with the specified length.
	 */
	private E[] newArr(int length) {
		@SuppressWarnings("unchecked")
		final E[] copy = (E[])createArr(data.getClass().getComponentType(), length);
		return copy;
	}
	
	@Override
	public Iterator<E> iterator() {
		return new ArrayIterator();
	}
	
	/**
	 * Returns an {@code Iterator} which filters out any {@code null} array
	 * elements. The returned iterator does not support {@code remove()}.
	 */
	public Iterator<E> iteratorNullsFiltered() {
		return Iterators.filter(iterator(), Predicates.notNull());
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}
	
	@Override
	public String toString() {
		return "Array[" + data.getClass().getComponentType().getSimpleName() + "," + data.length + "]";
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Creates an Array of the specified component type with the specified
	 * size.
	 * 
	 * @see java.lang.reflect.Array#newInstance(Class, int)
	 */
	@SuppressWarnings("unchecked")
	protected static <T> T[] createArr(Class<T> c, int size) {
		return (T[])java.lang.reflect.Array.newInstance(c, size);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	private class ArrayIterator implements Iterator<E> {
		
		int cursor = 0;
		
		@Override
		public boolean hasNext() {
			return cursor < data.length;
		}
		
		@Override
		public E next() {
			return data[cursor++];
		}
		
		@Override
		public void remove() {
			data[cursor - 1] = null;
		}
		
	}
	
}
