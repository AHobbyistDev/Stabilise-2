package com.stabilise.world.multidimensioned;

import static com.stabilise.util.collect.InstantiationRegistry.*;

import com.stabilise.util.collect.DuplicatePolicy;
import com.stabilise.util.collect.Registry;
import com.stabilise.world.HostWorld;
import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multidimensioned.dimension.DimOverworld;


public abstract class Dimension {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Registry of dimensions. */
	public static Registry<String, Class<? extends Dimension>> DIMENSIONS =
			new Registry<>("Dimensions", 2, DuplicatePolicy.THROW_EXCEPTION);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	public final String name;
	
	
	/**
	 * Every subclass of Dimension should have this constructor.
	 */
	public Dimension(String name) {
		this.name = name;
	}
	
	/**
	 * Creates the HostWorld object upon which this dimension will be hosted.
	 * Subclasses may override this to return a custom implementation of
	 * HostWorld to implement dimension-specific logic.
	 * 
	 * @param provider The world provider.
	 * @param info The world's info.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public HostWorld createHost(WorldProvider provider, WorldInfo info) {
		return new HostWorld(provider, this, info);
	}
	
	/**
	 * Creates the world generator to use for generating this dimension.
	 * 
	 * @param provider The world provider.
	 * @param world This dimension's host world.
	 * 
	 * @return The world generator.
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public abstract WorldGenerator createWorldGenerator(WorldProvider provider, HostWorld world);
	
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
		Factory<Dimension> factory = new ReflectiveFactory<>(dimClass, String.class);
		return factory.create(name);
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
	
}
