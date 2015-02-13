package com.stabilise.util.collect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A registry allows for objects of a certain type to be registered and
 * allocated keys.
 * 
 * <p>This class is based on a similar one from the decompiled Minecraft 1.7.10
 * source.
 */
@NotThreadSafe
public class Registry<K, V> extends AbstractRegistry<V> {
	
	/** The map of objects registered in the registry. */
	protected final Map<K, V> objects;
	
	
	/**
	 * Creates a new Registry with an initial capacity of 16 and the {@link
	 * DuplicatePolicy#REJECT REJECT} duplicate policy.
	 * 
	 * @param name The name of the registry.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public Registry(String name) {
		this(name, 16);
	}
	
	/**
	 * Creates a new Registry with the {@link DuplicatePolicy#REJECT REJECT}
	 * duplicate policy.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The initial registry capacity.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public Registry(String name, int capacity) {
		this(name, capacity, DuplicatePolicy.REJECT);
	}
	
	/**
	 * Creates a new Registry.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The initial registry capacity.
	 * @param dupePolicy The duplicate entry policy.
	 * 
	 * @throws NullPointerException if any of the arguments are {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 * @see DuplicatePolicy
	 */
	public Registry(String name, int capacity, DuplicatePolicy dupePolicy) {
		super(name, dupePolicy);
		
		objects = createUnderlyingMap(capacity);
	}
	
	/**
	 * Creates the map will be used for the registry.
	 * 
	 * @param capacity The initial registry capacity.
	 * 
	 * @return The registry's map.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	protected Map<K, V> createUnderlyingMap(int capacity) {
		return new HashMap<K, V>(capacity);
	}
	
	/**
	 * Registers an object. If the specified key is already mapped to an
	 * object, the old mapping will be overwritten if this registry uses the
	 * {@link DuplicatePolicy#OVERRIDE OVERRIDE} duplicate policy.
	 * 
	 * @param key The object's key.
	 * @param object The object.
	 * 
	 * @return {@code true} if the object was successfully registered;
	 * {@code false} otherwise.
	 * @throws NullPointerException if either argument is {@code null}.
	 * @throws IllegalStateException if this registry is {@link #lock()
	 * locked}.
	 * @throws IllegalArgumentException if {@code key} is already mapped to an
	 * entry and this registry uses the {@link DuplicatePolicy#THROW_EXCEPTION
	 * THROW_EXCEPTION} duplicate policy.
	 */
	public boolean register(K key, V object) {
		checkLock();
		
		if(key == null || object == null)
			throw new NullPointerException("null key or value!");
		
		if(objects.containsKey(key) && dupePolicy.handle(log, "Duplicate key \"" + key + "\""))
			return false;
		
		objects.put(key, object);
		size++;
		return true;
	}
	
	/**
	 * Gets the object to which the specified key is mapped.
	 * 
	 * @param key The key.
	 * 
	 * @return The object, or {@code null} if the key lacks a mapping.
	 */
	public V get(K key) {
		return objects.get(key);
	}
	
	/**
	 * Checks for whether or not a value is mapped to the specified key.
	 * 
	 * @param key The key.
	 * 
	 * @return {@code true} if the key has a mapping; {@code false} otherwise.
	 */
	public boolean containsKey(K key) {
		return objects.containsKey(key);
	}
	
	/**
	 * Gets the set of keys recognised by the registry. The returned set is
	 * unmodifiable and hence should only be used for iteration.
	 * 
	 * @return The set of keys.
	 */
	public Set<K> getKeys() {
		return Collections.unmodifiableSet(objects.keySet());
	}
	
	/**
	 * Gets an iterator over all objects registered in this registry.
	 */
	@Override
	public Iterator<V> iterator() {
		return objects.values().iterator();
	}
	
}