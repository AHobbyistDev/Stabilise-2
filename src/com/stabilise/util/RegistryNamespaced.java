package com.stabilise.util;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * The namespaced registry extends upon a standard registry to also include
 * names for registered objects, which are stored under a namespace in the
 * format: <tt>namespace:objectname</tt>.
 * 
 * <p>A namespaced registry also provides bi-directional integer-object
 * mappings. As such, it it recommended to register entries through {@link
 * #register(int, String, Object)} instead of {@link
 * #register(Object, Object)}.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 */
public class RegistryNamespaced<V> extends Registry<String, V> {
	
	/** The default namespace. */
	public final String defaultNamespace;
	
	/** The map of objects to their IDs. */
	protected final BiObjectIntMap<V> idMap;
	/** The map of objects to their names. */
	protected final Map<V, String> nameMap;
	
	
	/**
	 * Creates a new namespaced registry.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The intial registry capacity.
	 * @param defaultNamespace The default namespace under which to register
	 * objects.
	 * 
	 * @throws NullPointerException if either {@code name} or {@code 
	 * defaultNamespace} are {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public RegistryNamespaced(String name, int capacity, String defaultNamespace) {
		super(name, capacity);
		
		if(defaultNamespace == null)
			throw new NullPointerException("defaultNamespace is null");
		
		this.defaultNamespace = defaultNamespace + ":";
		
		idMap = new BiObjectIntMap<V>(capacity);
		nameMap = ((BiMap<String, V>)objects).inverse();
	}
	
	@Override
	protected Map<String, V> createUnderlyingMap(int capacity) {
		// Java automatically infers generic types, which is nice
		return HashBiMap.create(capacity);
	}
	
	/**
	 * Registers an object.
	 * 
	 * @param id The object's ID.
	 * @param name The object's name.
	 * @param object The object.
	 * 
	 * @throws IndexOufOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if either {@code name} or {@code object}
	 * are {@code null}.
	 */
	public void register(int id, String name, V object) {
		if(idMap.getObject(id) != null)
			log.logCritical("Adding duplicate id \"" + id + "\"!");
		super.register(ensureNamespaced(name), object);
		idMap.put(id, object);
	}
	
	/**
	 * Throws an UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException if this method is invoked.
	 * Remember to use {@link #register(int, String, Object)} for a namespaced
	 * registry!
	 */
	@Override
	public void register(String name, V object) {
		throw new UnsupportedOperationException(
				"Attempted to use register(String, Object)! Use register(int, String, V) instead!"
		);
	}
	
	@Override
	public V get(String key) {
		return super.get(ensureNamespaced(key));
	}
	
	/**
	 * Gets the object mapped to the specified ID.
	 * 
	 * @param id The ID.
	 * 
	 * @return The object, or {@code null} if the key is negative or otherwise
	 * lacks a mapping.
	 * @throws IndexOutOfBoundsException if {@code key < 0}.
	 */
	public V get(int id) {
		return idMap.getObject(id);
	}
	
	/**
	 * Gets the name of the specified object.
	 * 
	 * @param object The object.
	 * 
	 * @return The object's name, or {@code null} if the object isn't
	 * registered.
	 */
	public String getObjectName(V object) {
		return nameMap.get(object);
	}
	
	/**
	 * Gets the associative ID for the specified object.
	 * 
	 * @param object The object.
	 * 
	 * @return The object's ID, or {@code -1} if the object isn't registered.
	 */
	public int getObjectID(V object) {
		return idMap.getKey(object);
	}
	
	@Override
	public boolean containsKey(String name) {
		return super.containsKey(ensureNamespaced(name));
	}
	
	/**
	 * Checks for whether or not an object with the specified ID has been
	 * registered.
	 * 
	 * @param id The ID.
	 * 
	 * @return {@code true} if an object with the given ID exists;
	 * {@code false} otherwise.
	 */
	public boolean containsID(int id) {
		return idMap.objectExists(id);
	}
	
	/**
	 * Gets the iterator for the registered objects.
	 * 
	 * @return The iterator.
	 */
	@Override
	public Iterator<V> iterator() {
		return idMap.iterator();
	}
	
	/**
	 * Ensures that a name exists within a colon-delimited namespace,
	 * prepending <tt><i>defaultNamespace</i>:</tt> if it lacks one (where
	 * <i>defaultNamespace</i> is defined in this registry's constructor).
	 * 
	 * @param name The name.
	 * 
	 * @return The name, in the default namespace if it lacked one beforehand.
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	private String ensureNamespaced(String name) {
		return name.indexOf(':') == -1 ? defaultNamespace + name : name;
	}
	
}