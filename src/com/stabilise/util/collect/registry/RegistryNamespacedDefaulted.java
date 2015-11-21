package com.stabilise.util.collect.registry;

import java.util.Objects;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * This class allows for the creation of a namespaced registry which returns a
 * specified default value if no other value could be found.
 * 
 * <p>This class has been reconstructed from the decompiled Minecraft 1.7.10
 * source.
 */
@NotThreadSafe
public class RegistryNamespacedDefaulted<V> extends RegistryNamespaced<V> {
    
    /** The name mapped to the default value. */
    private final String defaultName;
    /** The default value. */
    private V defaultValue = null;
    
    
    /**
     * Creates a new namespaced registry.
     * 
     * @param defaultNamespace The default namespace under which to register
     * objects.
     * @param defaultName The name under which the default return value will be
     * registered.
     * 
     * @throws NullPointerException if any of the arguments are {@code null}.
     * @throws IllegalArgumentException if there is a colon (:) in {@code
     * defaultNamespace}.
     */
    public RegistryNamespacedDefaulted(RegistryParams params, String defaultNamespace,
            String defaultName) {
        super(params, defaultNamespace);
        this.defaultName = Objects.requireNonNull(defaultName);
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>If the name of the object matches that of the name of the default
     * return value, the object will be set as the default return value.
     * 
     * @return {@code true} if the object was successfully registered;
     * {@code false} otherwise.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
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
