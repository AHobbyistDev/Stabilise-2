package com.stabilise.util.collect;

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
	private V defaultValue = null;
	
	
	/**
	 * Creates a new namespaced registry with an initial capacity of 16 and the
	 * {@link DuplicatePolicy#REJECT REJECT} duplicate policy.
	 * 
	 * @param name The name of the registry.
	 * @param defaultNamespace The default namespace under which to register
	 * objects.
	 * @param defaultName The name under which the default return value will be
	 * registered.
	 * 
	 * @throws NullPointerException if either {@code name}, {@code
	 * defaultNamespace}, or {@code defaultName} are {@code null}.
	 * @throws IllegalArgumentException if there is a colon (:) in {@code
	 * defaultNamespace}.
	 */
	public RegistryNamespacedDefaulted(String name, String defaultNamespace, String defaultName) {
		this(name, defaultNamespace, defaultName, 16);
	}
	
	/**
	 * Creates a new namespaced registry with the {@link DuplicatePolicy#REJECT
	 * REJECT} duplicate policy.
	 * 
	 * @param name The name of the registry.
	 * @param defaultNamespace The default namespace under which to register
	 * objects.
	 * @param defaultName The name under which the default return value will be
	 * registered.
	 * @param capacity The initial registry capacity.
	 * 
	 * @throws NullPointerException if either {@code name}, {@code
	 * defaultNamespace}, or {@code defaultName} are {@code null}.
	 * @throws IllegalArgumentException if there is a colon (:) in {@code
	 * defaultNamespace}, or {@code capacity < 0}.
	 */
	public RegistryNamespacedDefaulted(String name, String defaultNamespace, String defaultName,
			int capacity) {
		this(name, defaultNamespace, defaultName, capacity, DuplicatePolicy.REJECT);
	}
	
	/**
	 * Creates a new namespaced registry.
	 * 
	 * @param name The name of the registry.
	 * @param defaultNamespace The default namespace under which to register
	 * objects.
	 * @param defaultName The name under which the default return value will be
	 * registered.
	 * @param capacity The initial registry capacity.
	 * @param dupePolicy The duplicate entry policy.
	 * 
	 * @throws NullPointerException if any of the arguments are {@code null}.
	 * @throws IllegalArgumentException if there is a colon (:) in {@code
	 * defaultNamespace}, or {@code capacity < 0}.
	 * @see DuplicatePolicy
	 */
	public RegistryNamespacedDefaulted(String name, String defaultNamespace, String defaultName,
			int capacity, DuplicatePolicy dupePolicy) {
		super(name, defaultNamespace, capacity, dupePolicy);
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
	 * @return {@code true} if the object was successfully registered;
	 * {@code false} otherwise.
	 * @throws IndexOufOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if either {@code id} or {@code key} is
	 * are already mapped to an entry and this registry uses the {@link
	 * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
	 */
	@Override
	public boolean register(int id, String name, V object) {
		if(!super.register(id, name, object))
			return false;
		if(defaultName.equals(name))
			defaultValue = object; // dupe checking already verified by super.register()
		return true;
	}
	
	/**
	 * Gets the object to which the specified name is mapped.
	 * 
	 * @param name The name.
	 * 
	 * @return The object, or the default value (note that this may be {@code
	 * null}) if the name lacks a mapping.
	 */
	@Override
	public V get(String name) {
		V obj = super.get(name);
		return obj == null ? defaultValue : obj;
	}
	
	/**
	 * Gets the object to which the specified ID is mapped.
	 * 
	 * @param id The ID.
	 * 
	 * @return The object, or the default value (note that this may be {@code
	 * null}) if the ID lacks a mapping.
	 */
	@Override
	public V get(int id) {
		V obj = super.get(id);
		return obj == null ? defaultValue : obj;
	}
	
}
