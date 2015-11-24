package com.stabilise.util.collect;

import com.stabilise.util.annotation.NotThreadSafe;

import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * BiObjectIntMap provides a bi-directional integer to object mapping
 * implementation. This class internally uses an array for constant-time
 * key-value retrieval, and an {@link IdentityHashMap} for value-key
 * retrieval.
 * 
 * <p>As a consequence of the integer-object mapping method used, client code
 * should refrain from using arbitrarily large integer keys, as doing so will
 * similarly create an arbitrarily large array filled with {@code null} values.
 * This also means negative keys are not permitted.
 * 
 * <p>It should also be noted that mappings are permanent and can not be
 * removed.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 * 
 * @param V The type of object to store.
 */
@NotThreadSafe
public class BiObjectIntMap<V> implements Iterable<V> {
    
    /** Maps Values -> Keys */
    private final IdentityHashMap<V, Integer> map;
    /** Maps Keys -> Values */
    private final Array<V> list;
    
    
    /**
     * Creates a new BiObjectIntMap.
     * 
     * @param capacity The initial entry capacity.
     * 
     * @throws IllegalArgumentException if {@code capacity < 0}.
     */
    public BiObjectIntMap(int capacity) {
        map = new IdentityHashMap<>(capacity);
        list = new Array<>(capacity);
    }
    
    /**
     * Maps an integer key to an object.
     * 
     * @param key The key.
     * @param value The object.
     * 
     * @throws IndexOutOfBoundsException if {@code key < 0}.
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public void put(int key, V value) {
        if(key < 0)
            throw new IndexOutOfBoundsException("key is negative!");
        if(value == null)
            throw new NullPointerException("value is null!");
        
        map.put(value, Integer.valueOf(key));
        list.setWithExpand(key, value, 1.25f);
    }
    
    /**
     * Gets the integer key to which the specified value is mapped.
     * 
     * @param value The value.
     * 
     * @return The object's mapped key, or {@code -1} if {@code value} lacks an
     * associated key.
     */
    public int getKey(Object value) {
        Integer val = map.get(value);
        return val == null ? -1 : val.intValue();
    }
    
    /**
     * Gets the object to which the specified key is mapped.
     * 
     * @param key The integer key.
     * 
     * @return The object, or {@code null} if {@code key} lacks a mapping.
     * @throws IndexOutOfBoundsException if {@code key < 0}.
     */
    public V getObject(int key) {
        return list.getSemiSafe(key);
    }
    
    /**
     * Checks for whether or not a value is mapped to the specified key.
     * 
     * @return {@code true} if the key has a mapping; {@code false} otherwise.
     * @throws IndexOutOfBoundsException if {@code key < 0}.
     */
    public boolean containsKey(int key) {
        return getObject(key) != null;
    }
    
    /**
     * Gets the iterator for all the mapped objects. The returned iterator does
     * not support {@code remove()}.
     */
    @Override
    public Iterator<V> iterator() {
        return list.iteratorNullsFiltered();
    }
    
    /**
     * Resizes the backing array to {@code size} if its length significantly
     * exceeds size.
     * 
     * @throws NegativeArraySizeException if size is negative.
     */
    public void clampSize(int size) {
        // We use 16 as an arbitrary cutoff value to guard against having to
        // resize to e.g. 3999 if our current size is 4000, which would just be
        // a waste of time. Admittedly +16 makes little difference for such
        // large arrays, but it's better than nothing.
        if(list.length() > size + 16)
            list.resize(size);
    }
    
}