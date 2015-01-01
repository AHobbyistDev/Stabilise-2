package com.stabilise.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A registry allows for objects of a certain type to be registered and
 * allocated keys.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 */
public class Registry<K, V> implements Iterable<V> {
	
	/** The name of the registry. */
	public final String name;
	
	/** The map of objects registered in the registry. */
	protected final Map<K, V> objects;
	
	/** Whether or not duplicate keys overwrite old ones. */
	protected final boolean overwrite;
	
	/** The registry's log. This is public as to allow muting. */
	public final Log log;
	
	
	/**
	 * Creates a new Registry with an initial capacity of 16. Attempting to
	 * register duplicate keys in this registry will result in the newer entry
	 * being ignored.
	 * 
	 * @param name The name of the registry.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public Registry(String name) {
		this(name, 16, false);
	}
	
	/**
	 * Creates a new Registry. Attempting to register duplicate keys in this
	 * registry will result in the newer entry being ignored.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The initial registry capacity.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public Registry(String name, int capacity) {
		this(name, capacity, false);
	}
	
	/**
	 * Creates a new Registry.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The initial registry capacity.
	 * @param overwrite Whether or not duplicate keys overwrite old ones. If
	 * {@code false}, duplicate keys are ignored.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public Registry(String name, int capacity, boolean overwrite) {
		if(name == null)
			throw new NullPointerException("name is null");
		this.name = name;
		this.overwrite = overwrite;
		
		log = Log.getAgent("Registry: " + name);
		
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
	 * object, the old mapping will be overwritten.
	 * 
	 * @param key The object's key.
	 * @param object The object.
	 * 
	 * @throws NullPointerException if either {@code key} or {@code object} are
	 * {@code null}.
	 */
	public void register(K key, V object) {
		if(key == null || object == null)
			throw new NullPointerException("null key or value!");
		
		if(objects.containsKey(key)) {
			if(overwrite) {
				log.logCritical("Duplicate key \"" + key + "\"; replacing old mapping");
			} else {
				log.logCritical("Duplicate key \"" + key + "\"; ignoring new mapping");
				return;
			}
		}
		
		objects.put(key, object);
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
	 * Gets the iterator for the registered objects.
	 * 
	 * @return The iterator.
	 */
	@Override
	public Iterator<V> iterator() {
		return objects.values().iterator();
	}
	
}