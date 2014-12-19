package com.stabilise.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;

/**
 * BiObjectIntMap provides a bi-directional integer to object mapping
 * implementation. This class internally uses an {@link ArrayList} for fast
 * key-value retrieval, and an {@link IdentityHashMap} for object-key
 * retrieval.
 * 
 * <p>As a consequence of the integer-object mapping method used, client code
 * should refrain from using arbitrarily large integer keys, as doing so will
 * similarly create an arbitrarily large array filled with {@code null} values.
 * Also, negative keys are not accepted.
 * 
 * <p>It should also be noted that mappings are permanent and can not be
 * removed.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 * 
 * @param V The type of object to store.
 */
public class BiObjectIntMap<V> implements Iterable<V> {
	
	/** The value-to-key map. */
	private IdentityHashMap<V, Integer> map;
	/** The list, which provides easy key-to-value mappings. */
	private List<V> list;
	
	
	/**
	 * Creates a new BiObjectIntMap.
	 * 
	 * @param capacity The initial entry capacity.
	 * 
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public BiObjectIntMap(int capacity) {
		map = new IdentityHashMap<V, Integer>(capacity);
		list = new ArrayList<V>(capacity);
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
		
		// Fill up any intermediate values in the array
		while(list.size() <= key) {
			list.add(null);
		}
		
		list.set(key, value);
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
		return key < list.size() ? list.get(key) : null;
	}
	
	/**
	 * Checks for whether or not a value is mapped to the specified key.
	 * 
	 * @param key The key.
	 * 
	 * @return {@code true} if the key has a mapping; {@code false} otherwise.
	 * @throws IndexOutOfBoundsException if {@code key < 0}.
	 */
	public boolean objectExists(int key) {
		return getObject(key) != null;
	}
	
	/**
	 * Gets the iterator for all the mapped objects.
	 * 
	 * @return The iterator.
	 */
	@Override
	public Iterator<V> iterator() {
		// As the ArrayList may be filled with null intermediates, filter the
		// nulls out
		return Iterators.filter(list.iterator(), Predicates.notNull());
	}
	
}