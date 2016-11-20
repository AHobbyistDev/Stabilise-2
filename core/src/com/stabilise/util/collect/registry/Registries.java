package com.stabilise.util.collect.registry;

/**
 * Provides static factory methods for registries.
 */
public class Registries {
    
    private Registries() {} // non-instantiable
    
    /**
     * Equivalent to {@code new Registry<>(new RegistryParams())}.
     */
    public static <K, V> Registry<K, V> registry() {
        return new Registry<>(new RegistryParams());
    }
    
    /**
     * Equivalent to {@code new TypeFactory<>(new RegistryParams())}.
     */
    public static <T> TypeFactory<T> typeFactory() {
        return new TypeFactory<>(new RegistryParams());
    }
    
}
