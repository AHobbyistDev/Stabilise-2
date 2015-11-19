package com.stabilise.util.collect;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A namespaced registry is an IDRegistry whose keys are namespaced strings
 * with the format: <tt>namespace:objectname</tt>.
 * 
 * <p>This class is based on a similar one from the decompiled Minecraft 1.7.10
 * source.
 */
@NotThreadSafe
public class RegistryNamespaced<V> extends IDRegistry<String, V> {
    
    /** The default namespace. */
    public final String defaultNamespace;
    
    /**
     * Creates a new namespaced registry.
     * 
     * @param defaultNamespace The default namespace under which to register
     * objects.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws IllegalArgumentException if there is a colon (:) in {@code
     * defaultNamespace}.
     */
    public RegistryNamespaced(RegistryParams params, String defaultNamespace) {
        super(params);
        
        if(defaultNamespace.indexOf(':') != -1)
            throw new IllegalArgumentException("\':\' should not appear in defaultNamespace!");
        
        this.defaultNamespace = defaultNamespace + ":";
    }
    
    @Override
    public boolean register(int id, String key, V object) {
        return super.register(id, ensureNamespaced(key), object);
    }
    
    @Override
    public V get(String key) {
        return super.get(ensureNamespaced(key));
    }
    
    @Override
    public boolean containsKey(String name) {
        return super.containsKey(ensureNamespaced(name));
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