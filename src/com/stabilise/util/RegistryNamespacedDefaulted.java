package com.stabilise.util;

/**
 * This class allows for the creation of a namespaced registry which returns a
 * specified default value if no other value could be found.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 */
public class RegistryNamespacedDefaulted<V> extends RegistryNamespaced<V> {
	
	/** The name mapped to the default value. */
	private final String defaultName;
	/** The default value. */
	private V defaultValue;
	
	
	/**
	 * Creates a new namespaced registry.
	 * 
	 * @param name The name of the registry.
	 * @param capacity The initial registry capacity.
	 * @param defaultNamespace The default namespace under which to register
	 * objects.
	 * @param defaultName The name under which the default return value will be
	 * registered.
	 * 
	 * @throws NullPointerException if either {@code name}, {@code
	 * defaultNamespace}, or {@code defaultName} are {@code null}.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 */
	public RegistryNamespacedDefaulted(String name, int capacity, String defaultNamespace, String defaultName) {
		super(name, capacity, defaultNamespace);
		if(defaultName == null)
			throw new NullPointerException("defaultName is null");
		this.defaultName = defaultName;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>If the name of the object matches that of the name of the default
	 * return value, the object will be set as the default return value.
	 * 
	 * @throws IndexOufOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if either {@code name} or {@code object}
	 * are {@code null}.
	 */
	@Override
	public void registerObject(int id, String name, V object) {
		if(defaultName.equals(name))
			defaultValue = object;
		
		super.registerObject(id, name, object);
	}
	
	/**
	 * Gets the object to which the specified name is mapped.
	 * 
	 * @param name The name.
	 * 
	 * @return The object, or the default value if the name lacks a mapping.
	 */
	@Override
	public V getObject(String name) {
		V obj = super.getObject(name);
		return obj == null ? defaultValue : obj;
	}
	
	/**
	 * Gets the object to which the specified ID is mapped.
	 * 
	 * @param id The ID.
	 * 
	 * @return The object, or the default value if the ID lacks a mapping.
	 */
	@Override
	public V getObject(int id) {
		V obj = super.getObject(id);
		return obj == null ? defaultValue : obj;
	}
	
}
