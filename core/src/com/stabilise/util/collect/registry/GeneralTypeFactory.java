package com.stabilise.util.collect.registry;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Supplier;

import com.stabilise.util.TheUnsafe;
import com.stabilise.util.collect.registry.GeneralTypeFactory.Factory;

/**
 * A GeneralTypeFactory is much like a {@link TypeFactory}; however it allows
 * registration of non-parameterless constructors.
 * 
 * <h3>Example code:</h3>
 * 
 * <pre>
 * class S {}
 * class A extends S { A(int x, int y) {} }
 * class B extends S { B(int x, int y) {} }
 * 
 * GeneralTypeFactory{@code <S>} factory = new GeneralTypeFactory<>(
 *     new RegistryParams(), int.class, int.class);
 * factory.register(0, A.class);
 * factory.register(1, B.class);
 * 
 * S a = factory.create(0, 0, 0); // arbitrary x,y values
 * S b = factory.create(1, 50, -50); // arbitrary x,y values
 * </pre>
 */
public class GeneralTypeFactory<T> extends TypeRegistry<T, Factory<T>> {
    
    private final Class<?>[] argTypes;
    
    
    /**
     * Creates a new GeneralTypeFactory.
     * 
     * @throws NullPointerException if either argument is {@code null}, or
     * {@code argTypes} contains any null elements.
     */
    public GeneralTypeFactory(RegistryParams params, Class<?>... argTypes) {
        super(params);
        this.argTypes = Objects.requireNonNull(argTypes);
        for(Class<?> c : argTypes)
            Objects.requireNonNull(c);
    }
    
    /**
     * Registers a {@link ReflectiveFactory} for the specified class.
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
     * @throws RuntimeException if the specified class does not have a
     * constructor with argument types as specified in this class' constructor.
     */
    public void register(int id, Class<? extends T> objClass) {
        register(id, objClass, new ReflectiveFactory<>(objClass, argTypes));
    }
    
    /**
     * Creates a new object.
     * 
     * @return A new object, or {@code null} if the specified ID lacks a
     * mapping.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws RuntimeException if the specified object could not be
     * instantiated.
     */
    public T create(int id, Object... args) {
        return getOrDefault(id, (objs) -> null).create(args);
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    public static interface Factory<T> {
        
        /**
         * @throws RuntimeException if instantiation failed
         */
        T create(Object... args);
        
    }
    
    /**
     * A Factory which utilises reflection to instantiate its objects.
     */
    public static class ReflectiveFactory<T> implements Factory<T>, Supplier<T> {
        
        /** The object constructor. */
        private final Constructor<? extends T> constructor;
        
        
        /**
         * Creates a new ReflectiveFactory for objects of the specified class.
         * 
         * @param objClass The objects' class.
         * @param args The desired constructor's arguments.
         * 
         * @throws NullPointerException if {@code objClass} is {@code null}.
         * @throws RuntimeException if the specified class does not have a
         * constructor accepting the specified parameter types.
         */
        public ReflectiveFactory(Class<? extends T> objClass, Class<?>... args) {
            try {
                constructor = objClass.getConstructor(args);
                constructor.setAccessible(true);
            } catch(Exception e) {
                String className = objClass.getCanonicalName();
                if(className == null)
                    className = "[null]";
                throw new RuntimeException("Constructor for " + className
                        + " with requested arguments does not exist! ("
                        + e.getMessage() + ")");
            }
        }
        
        @Override
        public T create(Object... args) {
            try {
                return constructor.newInstance(args);
            } catch(Exception e) {
                throw new RuntimeException("Could not reflectively instantiate"
                        + " object of class \""
                        + constructor.getDeclaringClass().getSimpleName()
                        + "\"! (" + e.getMessage() + ")",
                        e);
            }
        }
        
        /**
         * This class implements {@code Supplier} for convenience purposes;
         * note, however, that it is not safe to use this method for
         * constructing objects unless it is known that this ReflectiveFactory
         * is for a parameterless constructor.
         */
        @Override
        public T get() {
            return create();
        }
        
    }
    
    /**
     * A factory which utilises {@link sun.misc.Unsafe} to instantiate objects.
     * 
     * <p>As implied by the name, it is generally not safe to use an
     * UnsafeFactory to instantiate objects, as instantiation is done
     * <i>without invoking a constructor</i>, so object fields will be
     * <i>uninitialised</i>. If you do choose to use this, be careful!
     */
    public static class UnsafeFactory<T> implements Factory<T>, Supplier<T> {
        
        private final Class<? extends T> objClass;
        
        /**
         * Creates a new UnsafeFactory for objects of the specified class.
         * 
         * @param objClass The objects' class.
         * 
         * @throws NullPointerException if {@code objClass} is {@code null}.
         */
        public UnsafeFactory(Class<? extends T> objClass) {
            this.objClass = Objects.requireNonNull(objClass);
        }
        
        @Override
        public T create(Object... args) {
            return get();
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            try {
                return (T) TheUnsafe.unsafe.allocateInstance(objClass);
            } catch(InstantiationException e) {
                throw new RuntimeException("Could not unsafely instantiate "
                        + "object of class \""
                        + objClass.getSimpleName()
                        + "\" (" + e.getMessage() + ")",
                        e);
            }
        }
        
    }
    
}
