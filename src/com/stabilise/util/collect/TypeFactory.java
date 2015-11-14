package com.stabilise.util.collect;

import java.util.function.Supplier;

/**
 * A TypeFactory is a TypeRegistry for which the value mapped to each class is
 * a {@link Supplier}{@code <T>}.
 */
public class TypeFactory<T> extends TypeRegistry<T, Supplier<T>> {
    
    /**
     * @throws NullPointerException if {@code params} is {@code null}.
     */
    public TypeFactory(RegistryParams params) {
        super(params);
    }
    
    /**
     * Creates a new object.
     * 
     * @return A new object, or {@code null} if the specified ID lacks a
     * mapping.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     */
    public T create(int id) {
        return getOrDefault(id, () -> null).get();
    }
    
}
