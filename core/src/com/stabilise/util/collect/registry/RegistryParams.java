package com.stabilise.util.collect.registry;

import java.util.Objects;

import com.stabilise.util.Checks;


/**
 * Contains construction parameters for registries.
 * 
 * <p>For all cases, {@code name} and {@code dupePolicy} must be non-null, and
 * {@code initialCapacity} must be greater than zero.
 * 
 * <p>If not specified, the default values are:
 * 
 * <ul>
 * <li>{@code name}: {@code "Registry"}
 * <li>{@code initialCapacity}: {@code 16}
 * <li>{@code dupePolicy}: {@link DuplicatePolicy#THROW_EXCEPTION}
 * </ul>
 */
public class RegistryParams {
    
    final String name;
    final DuplicatePolicy dupePolicy;
    final int capacity;
    
    public RegistryParams() {
        this("Registry");
    }
    
    public RegistryParams(String name) {
        this(name, 16);
    }
    
    public RegistryParams(String name, int initialCapacity) {
        this(name, initialCapacity, DuplicatePolicy.THROW_EXCEPTION);
    }
    
    public RegistryParams(String name, DuplicatePolicy dupePolicy) {
        this(name, 16, dupePolicy);
    }
    
    public RegistryParams(String name, int initialCapacity, DuplicatePolicy dupePolicy) {
        this.name = Objects.requireNonNull(name);
        this.capacity = Checks.testMin(initialCapacity, 1);
        this.dupePolicy = Objects.requireNonNull(dupePolicy);
    }
    
}
