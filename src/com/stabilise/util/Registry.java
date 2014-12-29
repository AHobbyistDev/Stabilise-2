package com.stabilise.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A registry allows for objects of a certain type to be registered and
 * allocated keys.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 */
public class Registry<K, V> {
	
	/** The name of the registry. */
	public final String name;
	
	/** The map of objects registered in the registry. */
	protected final Map<K, V> objects;
	
	/** The registry's log. */
	protected final Log log;
	
	
	/**
	 * Creates a new Registry.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The initial registry capacity.
	 * 
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public Registry(String name, int capacity) {
		if(name == null)
			throw new NullPointerException("name is null");
		this.name = name;
		
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
	public void registerObject(K key, V object) {
		if(key == null || object == null)
			throw new NullPointerException("null key or value!");
		
		if(objects.containsKey(key))
			log.logCritical("Adding duplicate key \"" + key + "\" to registry!");
		
		objects.put(key, object);
	}
	
	/**
	 * Gets the object to which the specified key is mapped.
	 * 
	 * @param key The key.
	 * 
	 * @return The object, or {@code null} if the key lacks a mapping.
	 */
	public V getObject(K key) {
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
	 * Gets the set of keys recognised by the registry.
	 * 
	 * @return The set of keys.
	 */
	public Set<K> getKeys() {
		return Collections.unmodifiableSet(objects.keySet());
	}
	
}