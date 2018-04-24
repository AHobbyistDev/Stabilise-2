package com.stabilise.util.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * This class provides a thin wrapper for an object array with some added
 * convenience methods such as resizability.
 */
@NotThreadSafe
public class Array<E> implements Iterable<E> {
    
    E[] data;
    
    
    /**
     * Creates a new Array with a default length of 16.
     * 
     * <p>Use of this constructor should be avoided if possible, as the runtime
     * type of the backing array will be {@code Object[]}, which creates the
     * possibility of typing errors unless this Array's generic type is {@code
     * Object}.
     */
    public Array() {
        this(16);
    }
    
    /**
     * Creates a new Array.
     * 
     * <p>Use of this constructor should be avoided if possible, as the runtime
     * type of the backing array will be {@code Object[]}, which creates the
     * possibility of typing errors unless this Array's generic type is {@code
     * Object}.
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
        this.data = Objects.requireNonNull(data);
    }
    
    /**
     * Gets the element at the specified index.
     * 
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or
     * greater than or equal to {@link #length()}.
     */
    public E get(int index) {
        return data[index];
    }
    
    /**
     * Safely gets the element at the specified index without throwing an
     * {@code ArrayIndexOutOfBoundsException} if {@code index} is invalid.
     * 
     * @return The element at {@code index} (which may be {@code null}), or
     * {@code null} if {@code index} is negative or greater than or equal to
     * {@link #length()}.
     */
    public E getSafe(int index) {
        return index >= 0 && index < data.length
                ? data[index]
                : null;
    }
    
    /**
     * Semi-safely gets the element at the specified index (that is, without
     * throwing an {@code ArrayIndexOutOfBoundsException} if {@code index >=
     * length()}).
     * 
     * @return The element at {@code index} (which may be {@code null}), or
     * {@code null} if {@code index} is greater than or equal to {@link
     * #length()}.
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0}.
     */
    public E getSemiSafe(int index) {
        return index < data.length ? data[index] : null;
    }
    
    /**
     * Sets the element at the specified index.
     * 
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative or
     * greater than or equal to {@link #length()}.
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
     * Sets the element at the specified index, resizing the array if necessary
     * by multiplying the length by the specified scaling factor as per:
     * 
     * <pre>resize((int)(length() * scalingFactor) + 1);</pre>
     * 
     * <p>If, however, such a resize would still leave this Array at an
     * insufficient length, it is simply resized as per:
     * 
     * <pre>resize(index + 1);</pre>
     * 
     * <p>Note that a scaling factor of {@code 1.0} is permitted; in this case
     * this method behaves the same as {@link #setWithExpand(int, Object)}.
     * 
     * @throws IllegalArgumentException if {@code scalingFactor < 1.0f}.
     * @throws ArrayIndexOutOfBoundsException if {@code index} is negative.
     */
    public void setWithExpand(int index, E value, float scalingFactor) {
        if(data.length < index + 1) {
            if(scalingFactor < 1.0f)
                throw new IllegalArgumentException("Scaling factor must be >= 1.0!");
            resize(Math.max(index, (int)(data.length * scalingFactor)) + 1);
        }
        set(index, value);
    }
    
    /**
     * Sets every array value between {@code fromIndex} (inclusive) and {@code
     * toIndex} (exclusive) to {@code value}. If {@code toIndex <= fromIndex},
     * this method does nothing.
     * 
     * @throws ArrayIndexOutOfBoundsException if {@code fromIndex < 0 ||
     * toIndex >= length()}.
     */
    public void setBetween(E value, int fromIndex, int toIndex) {
        for(int i = fromIndex; i < toIndex; i++)
            data[i] = value;
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
     * Adds a new element to the array, increasing its length by the specified
     * scaling factor as if by:
     * 
     * <pre>resize((int)(length() * scalingFactor) + 1);</pre>
     * 
     * <p>Invoking this method <i>always</i> causes a resize, and as such this
     * should not be used in the same way that {@link ArrayList#add(Object)
     * ArrayList.add()} is used, as this class does not keep track of
     * "effective array size", only the backing array's length.
     * 
     * <p>Note that a scaling factor of {@code 1.0} is permitted; in this case
     * this method behaves the same as {@link #add(Object)}.
     * 
     * @throws IllegalArgumentException if {@code scalingFactor < 1.0f}.
     */
    public void add(E value, float scalingFactor) {
        if(scalingFactor < 1.0f)
            throw new IllegalArgumentException("Scaling factor must be >= 1.0!");
        int oldLength = data.length;
        resize((int)(data.length * scalingFactor) + 1);
        set(oldLength, value);
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
     * Resizes the backing array using {@link Arrays#copyOf(Object[], int)}.
     * 
     * @throws NegativeArraySizeException if {@code length} is negative.
     * @see Arrays#copyOf(Object[], int)
     */
    public void resize(int length) {
        data = Arrays.copyOf(data, length);
    }
    
    /**
     * Returns a copy of this Array's backing array. The elements themselves
     * are not copied or cloned in any way.
     */
    public E[] copy() {
        return data.clone();
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
    public boolean equals(Object o) {
        if(!(o instanceof Array)) return false;
        return ((Array<?>)o).data == data;
    }
    
    @Override
    public String toString() {
        return "Array: " + data.getClass().getComponentType().getSimpleName()
                + "[" + data.length + "]";
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Creates an Array of the specified component type with the specified
     * length.
     * 
     * @see java.lang.reflect.Array#newInstance(Class, int)
     */
    @SuppressWarnings("unchecked")
    protected static <T> T[] createArr(Class<T> c, int length) {
        return (T[])java.lang.reflect.Array.newInstance(c, length);
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
            if(hasNext())
                return data[cursor++];
            else
                throw new NoSuchElementException();
        }
        
        @Override
        public void remove() {
            data[cursor - 1] = null;
        }
        
    }
    
}
