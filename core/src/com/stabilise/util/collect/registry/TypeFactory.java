package com.stabilise.util.collect.registry;

import java.util.function.Supplier;

import com.stabilise.util.collect.registry.GeneralTypeFactory.UnsafeFactory;

/**
 * A TypeFactory is a TypeRegistry for which the value mapped to each class is
 * a {@link Supplier}{@code <T>}.
 * 
 * <h3>Example code:</h3>
 * 
 * <pre>
 * class S {}
 * class A extends S {}
 * class B extends S {}
 * 
 * TypeFactory{@code <S>} factory = new TypeFactory<>();
 * factory.register(0, A.class, A::new); // uses default constructor
 * factory.registerUnsafe(1, B.class);   // uses UnsafeFactory
 * 
 * S a = factory.create(0);
 * S b = factory.create(1);
 * </pre>
 */
public class TypeFactory<T> extends TypeRegistry<T, Supplier<T>> {
    
    /**
     * @throws NullPointerException if {@code params} is {@code null}.
     */
    public TypeFactory(RegistryParams params) {
        super(params);
    }
    
    /**
     * Registers a {@link UnsafeFactory} for the specified class. <b>Note
     * carefully the risks associated with UnsafeFactory before using this.</b>
     * 
     * @param id The ID of the class.
     * @param objClass The class.
     * 
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if {@code objClass} is {@code null}.
     * @throws IllegalArgumentException if either {@code id} or the class have
     * already been registered and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public void registerUnsafe(int id, Class<? extends T> objClass) {
        register(id, objClass, new UnsafeFactory<>(objClass));
    }
    
    /**
     * Creates a new object.
     * 
     * @return A new object, or {@code null} if the specified ID lacks a
     * mapping.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     */
    public T create(int id) {
        //return getOrDefault(id, () -> null).get();
        Supplier<T> supp = get(id);
        return supp == null ? null : supp.get();
    }
    
    /**
     * Creates a new object.
     * 
     * @param clazz The class of the object.
     * 
     * @return A new object, or {@code null} if the class is not registered.
     */
    public <S extends T> S create(Class<S> clazz) {
        int id = getID(clazz);
        if(id == -1) return null;
        @SuppressWarnings("unchecked")
        S s = (S)create(id);
        return s;
    }
    
}
