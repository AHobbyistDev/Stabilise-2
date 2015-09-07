package com.stabilise.util.collect;

import java.lang.reflect.Constructor;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.stabilise.util.TheUnsafe;
import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A registry which provides instantiation facilities for registered classes.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * static class MyClass {} // generic superclass
 * 
 * static class MyOtherClass extends MyClass {
 *     public MyOtherClass(int x, int y) {}
 * }
 * static class YetAnotherClass extends MyClass {
 *     public YetAnotherClass(String foo) {}
 * }
 * 
 * static final InstantiationRegistry{@code <MyClass>} registry =
 *     new InstantiationRegistry{@code <>}(2, DuplicatePolicy.THROW_EXCEPTION);
 * 
 * static {
 *     registry.register(0, MyOtherClass.class, Integer.TYPE, Integer.TYPE);
 *     registry.register(1, YetAnotherClass.class, args ->
 *         new YetAnotherClass((String)args[0]));
 *         
 *     {@code // Henceforth the following blocks of code are equivalent:}
 *     
 *     MyClass obj1, obj2;
 *     
 *     obj1 = new MyOtherClass(0, 1);
 *     obj2 = new YetAnotherClass("Penguin");
 *     
 *     obj1 = registry.instantiate(0, 0, 1);
 *     obj2 = registry.instantiate(1, "Penguin");
 * }</pre>
 * 
 * @param <E> The type of object to instantiate.
 */
@NotThreadSafe
public class InstantiationRegistry<E> extends AbstractRegistry<Class<? extends E>> {
    
    /** Maps ID -> Factory. */
    private final Array<Factory<? extends E>> factoryArr;
    /** Max ID registered. -1 indicates nothing is registered. */
    private int maxID = -1;
    /** Maps Class -> ID. */
    private final Map<Class<? extends E>, Integer> idMap;
    
    /** The default constructor arguments. */
    private final Class<?>[] defaultArgs;
    
    
    /**
     * Creates a new instantiation registry, naming it appropriate to the base
     * class.
     * 
     * @param capacity The initial registry capacity.
     * @param dupePolicy The duplicate entry policy.
     * @param baseClass The base class - i.e. the {@code Class} object which T
     * technically defines.
     * @param defaultArgs The default constructor arguments. Permitted to be
     * empty.
     * 
     * @throws NullPointerException if {@code dupePolicy} or any of the {@code
     * defaultArgs} - if any are provided - are {@code null}.
     * @throws IllegalArgumentException if {@code capacity < 0}.
     * @see DuplicatePolicy
     */
    /*
    public InstantiationRegistry(int capacity, DuplicatePolicy dupePolicy,
            Class<E> baseClass, Class<?>... defaultArgs) {
        this(capacity, dupePolicy, baseClass.getSimpleName() + "InstRegistry",
                defaultArgs);
    }
    */
    
    /**
     * Creates a new instantiation registry.
     * 
     * @param capacity The initial registry capacity.
     * @param dupePolicy The duplicate entry policy.
     * @param registryName The name of this registry.
     * @param defaultArgs The default constructor arguments. Permitted to be
     * empty.
     * 
     * @throws NullPointerException if {@code dupePolicy} or any of the {@code
     * defaultArgs} - if any are provided - are {@code null}.
     * @throws IllegalArgumentException if {@code capacity < 0}.
     * @see DuplicatePolicy
     */
    public InstantiationRegistry(int capacity, DuplicatePolicy dupePolicy,
            String registryName, Class<?>... defaultArgs) {
        super(registryName, dupePolicy);
        
        factoryArr = new Array<Factory<? extends E>>(capacity);
        idMap = new IdentityHashMap<>(capacity);
        this.defaultArgs = defaultArgs != null ? defaultArgs : new Class<?>[0];
        
        for(Class<?> c : this.defaultArgs)
            if(c == null)
                throw new NullPointerException("A default arg is null!");
    }
    
    /**
     * Creates a new InstantiationRegistry with a generic name.
     * 
     * @see #InstantiationRegistry(int, DuplicatePolicy, String, Class...)
     */
    public InstantiationRegistry(int capacity, DuplicatePolicy dupePolicy,
            Class<?>... defaultArgs) {
        this(capacity, dupePolicy, "InstRegistry", defaultArgs);
    }
    
    /**
     * Registers a reflective object factory.
     * 
     * <p>Invoking this method is equivalent to invoking {@link
     * #register(int, String, Class, Class...)
     * register(id, name, objClass, defaultArgs)}, where {@code defaultArgs}
     * is specified in the constructor.
     * 
     * @param id The ID of the object type.
     * @param objClass The objects' class.
     * 
     * @throws RuntimeException if the specified class does not have a
     * constructor accepting the default arguments.
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code id} is already mapped
     * to a factory and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public void registerDefaultArgs(int id, Class<? extends E> objClass) {
        register(id, objClass, defaultArgs);
    }
    
    /**
     * Registers a {@link ReflectiveFactory reflective object factory}.
     * 
     * @param id The ID of the object type.
     * @param objClass The objects' class.
     * @param args The desired constructor's arguments.
     * 
     * @throws RuntimeException if the specified class does not have a
     * constructor accepting the specified parameter types.
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code id} is already mapped
     * to a factory and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public <S extends E> void register(int id, Class<S> objClass, Class<?>... args) {
        register(id, objClass, new ReflectiveFactory<S>(objClass, args));
    }
    
    /**
     * Registers an object factory.
     * 
     * @param id The ID of the object type.
     * @param name The name of the object type.
     * @param objClass The objects' class.
     * @param factory The factory object with which to create instances of the
     * object.
     * 
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws IllegalArgumentException if {@code id} is already mapped
     * to a factory and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public <S extends E> void register(int id, Class<S> objClass, Factory<S> factory) {
        checkLock();
        if(factoryArr.getSemiSafe(id) != null && dupePolicy.handle(log, "Duplicate id " + id))
            return;
        if(idMap.containsKey(objClass) && dupePolicy.handle(log, "Duplicate class " + objClass.getSimpleName()))
            return;
        factoryArr.setWithExpand(id, factory, 1.25f);
        idMap.put(objClass, Integer.valueOf(id));
        size++;
        if(id > maxID)
            maxID = id;
    }
    
    /**
     * Registers an {@link UnsafeFactory unsafe object factory}.
     * 
     * @param id The ID of the object type.
     * @param objClass The objects' class.
     * 
     * @throws IllegalStateException if this registry is {@link #lock()
     * locked}.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if {@code objClass} is {@code null}.
     * @throws IllegalArgumentException if {@code id} is already mapped
     * to a factory and this registry uses the {@link
     * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
     */
    public <S extends E> void registerUnsafe(int id, Class<S> objClass) {
        register(id, objClass, new UnsafeFactory<S>(objClass));
    }
    
    /**
     * Instantiates an object which has been registered with the specified ID.
     * 
     * @param id The ID of the object type.
     * @param args The constructor arguments.
     * 
     * @return The newly-created object, or {@code null} if the specified ID
     * lacks a mapping.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws RuntimeException if object creation failed.
     */
    public E instantiate(int id, Object... args) {
        Factory<? extends E> factory = factoryArr.getSemiSafe(id);
        return factory == null ? null : factory.create(args);
    }
    
    /**
     * Gets the ID of the specified object class.
     * 
     * @return The ID, or {@code -1} if the object class has not been
     * registered.
     */
    public int getID(Class<? extends E> objClass) {
        Integer i = idMap.get(objClass);
        return i == null ? -1 : i.intValue();
    }
    
    /**
     * Checks for whether or not this registry contains a mapping for the
     * specified ID.
     * 
     * @return {@code true} if {@code id} has a mapping; {@code false}
     * otherwise.
     */
    public boolean containsID(int id) {
        return factoryArr.getSafe(id) != null;
    }
    
    @Override
    public void lock() {
        if(!isLocked())
            factoryArr.resize(maxID + 1);
        super.lock();
    }
    
    @Override
    public Iterator<Class<? extends E>> iterator() {
        return idMap.keySet().iterator();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A Factory instantiates objects of the specified type on request.
     */
    public static interface Factory<T> {
        
        /**
         * Creates an object.
         * 
         * @param args The object arguments.
         * 
         * @return The newly-created object.
         * @throws RuntimeException if object creation failed.
         */
        public abstract T create(Object... args);
        
    }
    
    /**
     * A Factory which utilises reflection to instantiate its objects.
     */
    public static class ReflectiveFactory<T> implements Factory<T> {
        
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
        
    }
    
    /**
     * A factory which utilises {@link sun.misc.Unsafe} to instantiate objects.
     * 
     * <p>As implied by the name, it is generally not safe to use an
     * UnsafeFactory to instantiate objects, as instantiation is done
     * <i>without invoking a constructor</i>, so object fields will be
     * <i>uninitialised</i>. If you do choose to use this, be careful!
     */
    public static class UnsafeFactory<T> implements Factory<T> {
        
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
        
        @SuppressWarnings("unchecked")
        @Override
        public T create(Object... args) {
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
