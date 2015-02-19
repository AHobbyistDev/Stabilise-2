package com.stabilise.util.collect;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A registry which provides instantiation facilities for registered classes.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>
 * public static class MyClass {} // generic superclass
 * 
 * public static class MyOtherClass extends MyClass {
 *     public MyOtherClass(int x, int y) {
 *         super();
 *     }
 * }
 * public static class YetAnotherClass extends MyClass {
 *     {@code // Note that this constructor is private (this means reflection would}
 *     {@code // fail at instantiating it).}
 *     private YetAnotherClass(String name) {
 *         super();
 *     }
 * }
 * 
 * public static final InstantiationRegistry{@code <MyClass>} registry =
 *     new InstantiationRegistry{@code <>}(2, DuplicatePolicy.THROW_EXCEPTION, MyClass.class);
 * 
 * static {
 *     registry.register(0, MyOtherClass.class, Integer.TYPE, Integer.TYPE);
 *     registry.register(1, YetAnotherClass.class,
 *         new InstantiationRegistry.Factory{@code <YetAnotherClass>}() {
 *             &#64;Override
 *             public YetAnotherClass create(Object... args) {
 *                 return new YetAnotherClass((String)args[0]);
 *             }
 *         });
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
	
	private final BiObjectIntMap<Factory<? extends E>> factoryMap;
	private final Map<Class<? extends E>, Integer> idMap;
	
	/** The default constructor arguments. */
	private final Class<?>[] defaultArgs;
	
	
	/**
	 * Creates a new instantiation registry.
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
	public InstantiationRegistry(int capacity, DuplicatePolicy dupePolicy, Class<E> baseClass,
			Class<?>... defaultArgs) {
		super(baseClass.getSimpleName() + "InstRegistry", dupePolicy);
		
		factoryMap = new BiObjectIntMap<>(capacity);
		idMap = new HashMap<>(capacity);
		this.defaultArgs = defaultArgs != null ? defaultArgs : new Class<?>[0];
		
		for(Class<?> c : this.defaultArgs)
			if(c == null)
				throw new NullPointerException("A default arg is null!");
	}
	
	/**
	 * Registers a reflective object factory.
	 * 
	 * <p>Invoking this method is equivalent to invoking {@link
	 * #register(int, String, Class, Class...)
	 * register(id, name, objClass, defaultArgs)}, where {@code defaultArgs}
	 * is specified in the {@link
	 * #InstantiationRegistry(int, DuplicatePolicy, Class, Class...)
	 * constructor}.
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
	 * @throws IllegalArgumentException if either {@code id} is already mapped
	 * to a factory or {@code objClass} has already been registered, and this
	 * registry uses the {@link DuplicatePolicy#THROW_EXCEPTION
	 * THROW_EXCEPTION} duplicate policy.
	 */
	public void registerDefaultArgs(int id, Class<? extends E> objClass) {
		register(id, objClass, defaultArgs);
	}
	
	/**
	 * Registers a reflective object factory.
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
	 * @throws IllegalArgumentException if either {@code id} is already mapped
	 * to a factory or {@code objClass} has already been registered, and this
	 * registry uses the {@link DuplicatePolicy#THROW_EXCEPTION
	 * THROW_EXCEPTION} duplicate policy.
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
	 * @throws IllegalArgumentException if either {@code id} is already mapped
	 * to a factory or {@code objClass} has already been registered, and this
	 * registry uses the {@link DuplicatePolicy#THROW_EXCEPTION
	 * THROW_EXCEPTION} duplicate policy.
	 */
	public <S extends E> void register(int id, Class<S> objClass, Factory<S> factory) {
		checkLock();
		if(factoryMap.containsKey(id) && dupePolicy.handle(log, "Duplicate id " + id))
			return;
		if(idMap.containsKey(objClass) && dupePolicy.handle(log, "Duplicate class " + objClass.getSimpleName()))
			return;
		factoryMap.put(id, factory);
		idMap.put(objClass, Integer.valueOf(id));
		size++;
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
		Factory<? extends E> factory = factoryMap.getObject(id);
		if(factory != null)
			return factory.create(args);
		return null;
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
	
	@Override
	public void lock() {
		super.lock();
		factoryMap.trim();
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
			} catch(Exception e) {
				String className = objClass.getCanonicalName();
				if(className == null)
					className = "[null]";
				throw new RuntimeException("Constructor for " + className
						+ " with requested arguments does not exist! (" + e.getMessage() + ")");
			}
		}
		
		@Override
		public T create(Object... args) {
			try {
				return constructor.newInstance(args);
			} catch(Exception e) {
				throw new RuntimeException("Could not reflectively instantiate object of class \""
						+ constructor.getDeclaringClass().getSimpleName() + "\"! (" + e.getMessage() + ")",
						e);
			}
		}
		
	}
	
}
