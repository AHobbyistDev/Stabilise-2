package com.stabilise.util.collect;

import java.util.Arrays;
import java.util.function.LongConsumer;

/**
 * This class provides a thin wrapper for a resizable long array. Iteration
 * should be performed as:
 * 
 * <pre>for(int i = 0; i < list.size(); i++) {
 *     doSomething(list.get(i));
 * }
 * </pre>
 */
public class LongList {
    
    private long[] data;
    private int size;
    
    
    /**
     * Creates a new LongList with an internal array size of 8.
     */
    public LongList() {
        this(8);
    }
    
    /**
     * Creates a new LongList.
     * 
     * @param initialLength The initial length of the internal array.
     * 
     * @throws NegativeArraySizeException if initialLength < 0.
     */
    public LongList(int initialLength) {
        data = new long[initialLength];
        size = 0;
    }
    
    /**
     * Returns the size of this list.
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns the length of the backing array.
     */
    public int capacity() {
        return data.length;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Adds a long to this list. If necessary, the backing array will be
     * resized.
     */
    public void add(long l) {
        if(size == data.length)
            data = Arrays.copyOf(data, 2*size + 1);
        data[size++] = l;
    }
    
    /**
     * Sets the element at the specified index, ignoring the size of this list.
     * 
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >=
     * capacity()}.
     */
    public void set(int index, long l) {
        data[index] = l;
    }
    
    /**
     * Gets the element at the specified index.
     * 
     * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >=
     * capacity()}.
     */
    public long get(int index) {
        return data[index];
    }
    
    /**
     * Clears this list.
     */
    public void clear() {
        size = 0;
    }
    
    /**
     * Iterates over this list as if by:
     * 
     * <pre>for(int i = 0; i < size; i++)
     *      action.accept(data[i]);
     * </pre>
     * 
     * and then clears this list.
     */
    public void clear(LongConsumer action) {
        if(size == 0) return;
        for(int i = 0; i < size; i++)
            action.accept(data[i]);
        size = 0;
    }
    
    /**
     * Iterates over this list as if by:
     * 
     * <pre>for(int i = 0; i < size; i++)
     *      action.accept(data[i]);
     * </pre>
     */
    public void interate(LongConsumer action) {
        for(int i = 0; i < size; i++)
            action.accept(data[i]);
    }
    
}
