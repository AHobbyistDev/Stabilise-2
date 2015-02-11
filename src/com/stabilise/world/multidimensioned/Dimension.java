package com.stabilise.world.multidimensioned;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.util.collect.DuplicatePolicy;
import com.stabilise.util.collect.Registry;
import com.stabilise.world.HostWorld;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.multidimensioned.dimension.DimOverworld;


public class Dimension {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Registry of dimensions. */
	public static BiRegistry<String, Class<? extends Dimension>> DIMENSIONS =
			new BiRegistry<>("Dimensions", 4, DuplicatePolicy.THROW_EXCEPTION);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	private final String name;
	
	
	/**
	 * Every subclass of Dimension should have a blank constructor.
	 */
	public Dimension() {
		name = DIMENSIONS.getKey(getClass());
	}
	
	/**
	 * Creates the HostWorld object upon which this dimension will be hosted.
	 * Subclasses may override this to return a custom implementation of
	 * HostWorld to implement dimension-specific logic.
	 * 
	 * @param info The world's info.
	 * @param profiler The profiler to use for profiling the world.
	 * @param log The log to use for the world.
	 * 
	 * @throws NullPointerException if any argument is {@code null}.
	 */
	public HostWorld createHost(WorldInfo info, Profiler profiler, Log log) {
		return new HostWorld(this, info, profiler, log);
	}
	
	/**
	 * @return The name of this dimension.
	 */
	public String getName() {
		return name;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets an instance of a Dimension with the specified name.
	 * 
	 * @param name The name of the dimension.
	 * 
	 * @return The Dimension, or {@code null} if there is no such dimension
	 * with the specified name.
	 * @throws RuntimeException if the Dimension object could not be
	 * instantiated.
	 */
	public static Dimension getDimension(String name) {
		Class<? extends Dimension> dimClass = DIMENSIONS.get(name);
		if(dimClass == null)
			return null;
		try {
			return dimClass.newInstance();
		} catch(Exception e) {
			throw new RuntimeException("Could not instantiate Dimension object " +
					"for dimension \"" + name + "\"", e);
		}
	}
	
	/**
	 * Registers all the dimensions.
	 * 
	 * @throws IllegalStateException if the dimensions have already been
	 * registered.
	 */
	public static void registerDimensions() {
		registerDimension("overworld", DimOverworld.class);
		
		DIMENSIONS.lock();
	}
	
	/**
	 * Registers a dimension.
	 * 
	 * @param name The name of the dimension.
	 * @param dimClass The dimension's class.
	 * 
	 * @throws RuntimeException see {@link Registry#register(Object, Object)}.
	 */
	private static void registerDimension(String name, Class<? extends Dimension> dimClass) {
		DIMENSIONS.register(name, dimClass);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A Registry which provides value-key {@code get} operations in addition
	 * to key-value {@code get} operations.
	 */
	private static class BiRegistry<K, V> extends Registry<K, V> {
		
		private final Map<V, K> keyMap;
		
		/**
		 * @see Registry#Registry(String, int, DuplicatePolicy)
		 */
		public BiRegistry(String name, int capacity, DuplicatePolicy dupePolicy) {
			super(name, capacity, dupePolicy);
			
			keyMap = ((BiMap<K, V>)objects).inverse();
		}
		
		@Override
		protected Map<K, V> createUnderlyingMap(int capacity) {
			return HashBiMap.create(capacity);
		}
		
		/**
		 * @return The key mapped to the specified object, or {@code null} if
		 * there is no such mapping.
		 */
		public K getKey(V object) {
			return keyMap.get(object);
		}
		
	}
	
}
