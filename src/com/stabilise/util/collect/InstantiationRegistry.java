package com.stabilise.util.collect;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.stabilise.util.collect.Registry.DuplicatePolicy;

public class InstantiationRegistry<T> {
	
	/** The factory registry. */
	private final RegistryNamespaced<Factory<T>> factories;
	/** The map of object classes to their factory. */
	private final Map<Class<? extends T>, Factory<T>> classMap;
	
	/** The default constructor arguments. */
	private final Class<?>[] defaultArgs;
	
	
	/**
	 * Creates a new instantiation registry.
	 * 
	 * @param name The name of the registry.
	 * @param defaultNamespace The default namespace under which to register
	 * objects.
	 * @param capacity The initial registry capacity.
	 * @param dupePolicy The duplicate entry policy.
	 * 
	 * @throws NullPointerException if either {@code name}, {@code
	 * defaultNamespace}, or {@code dupePolicy} are {@code null}.
	 * @throws IllegalArgumentException if there is a colon (:) in {@code
	 * defaultNamespace}, or {@code capacity < 0}.
	 * @see DuplicatePolicy
	 */
	public InstantiationRegistry(String name, String defaultNamespace, int capacity,
			DuplicatePolicy dupePolicy, Class<?>... defaultArgs) {
		factories = new RegistryNamespaced<Factory<T>>(name, defaultNamespace, capacity, dupePolicy);
		classMap = new HashMap<Class<? extends T>, Factory<T>>(capacity);
		this.defaultArgs = defaultArgs != null ? defaultArgs : new Class<?>[0];
	}
	
	/**
	 * Registers a reflective object factory.
	 * 
	 * @param id The ID of the object type.
	 * @param name The name of the object type.
	 * @param objClass The objects' class.
	 * 
	 * @throws RuntimeException if the specified class does not have a
	 * constructor accepting the default arguments.
	 * @throws IndexOutOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if either {@code id} or {@code name}
	 * are already mapped to a factory and this registry uses the {@link
	 * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
	 */
	public void registerDefaultArgs(int id, String name, Class<? extends T> objClass) {
		register(id, name, objClass, objClass, defaultArgs);
	}
	
	/**
	 * Registers a reflective object factory.
	 * 
	 * @param id The ID of the object type.
	 * @param name The name of the object type.
	 * @param objClass The objects' class.
	 * @param args The desired constructor's arguments.
	 * 
	 * @throws RuntimeException if the specified class does not have a
	 * constructor accepting arguments of the specified type.
	 * @throws IndexOutOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if either {@code id} or {@code name}
	 * are already mapped to a factory and this registry uses the {@link
	 * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
	 */
	public void register(int id, String name, Class<? extends T> objClass, Class<?>... args) {
		register(id, name, objClass, new ReflectiveFactory<T>(objClass, args));
	}
	
	/**
	 * Registers a tile entity.
	 * 
	 * @param id The ID of the tile entity.
	 * @param name The name of the tile entity.
	 * @param objClass The tile entity's class.
	 * @param factory The factory object with which to create instances of the
	 * tile entity.
	 * 
	 * @throws IndexOutOfBoundsException if {@code id < 0}.
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if either {@code id} or {@code name}
	 * are already mapped to a factory and this registry uses the {@link
	 * DuplicatePolicy#THROW_EXCEPTION THROW_EXCEPTION} duplicate policy.
	 */
	public void register(int id, String name, Class<? extends T> objClass, Factory<T> factory) {
		if(factories.register(id, name, factory))
			classMap.put(objClass, factory);
	}
	
	/**
	 * Instantiates an object which has been registered with the specified ID.
	 * 
	 * @param id The ID of the object type.
	 * @param args The constructor arguments.
	 * 
	 * @return The newly-created object, or {@code null} if the specified ID
	 * lacks a mapping.
	 * @throws RuntimeException if object creation failed.
	 */
	public T instantiate(int id, Object... args) {
		Factory<T> factory = factories.get(id);
		if(factory != null)
			return factory.create(args);
		return null;
	}
	
	/**
	 * Instantiates an object which has been registered with the specified
	 * name.
	 * 
	 * @param name The name of the object type.
	 * @param args The constructor arguments.
	 * 
	 * @return The newly-created object, or {@code null} if the specified name
	 * lacks a mapping.
	 * @throws RuntimeException if object creation failed.
	 */
	public T instantiate(String name, Object... args) {
		Factory<T> factory = factories.get(name);
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
	public int getID(Class<? extends T> objClass) {
		return factories.getObjectID(classMap.get(objClass));
	}
	
	/**
	 * Gets the name of the specified object class.
	 * 
	 * @return The name, or {@code null} if the object class has not been
	 * registered.
	 */
	public String getName(Class<? extends T> objClass) {
		return factories.getObjectName(classMap.get(objClass));
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A Factory instantiates objects of the specified type on request.
	 */
	public interface Factory<T> {
		
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
	 * A ReflectiveFactory utilises reflection to instantiate its objects.
	 */
	private static class ReflectiveFactory<T> implements Factory<T> {
		
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
		 * constructor accepting only two integer parameters.
		 */
		protected ReflectiveFactory(Class<? extends T> objClass, Class<?>... args) {
			try {
				constructor = objClass.getConstructor(args);
			} catch(Exception e) {
				throw new RuntimeException("Constructor for " + objClass.getCanonicalName() +
						" with requested arguments does not exist!");
			}
		}
		
		@Override
		public T create(Object... args) {
			try {
				return constructor.newInstance(args);
			} catch(Exception e) {
				throw new RuntimeException("Could not reflectively instantiate object of class \""
						+ constructor.getDeclaringClass().getSimpleName() + "\"!", e);
			}
		}
		
	}
	
}