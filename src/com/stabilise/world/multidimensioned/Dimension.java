package com.stabilise.world.multidimensioned;

import static com.stabilise.util.collect.InstantiationRegistry.*;

import com.stabilise.util.collect.DuplicatePolicy;
import com.stabilise.util.collect.Registry;
import com.stabilise.world.HostWorld;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multidimensioned.dimension.DimOverworld;


public abstract class Dimension {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Registry of dimensions. */
	private static Registry<String, Class<? extends Dimension>> DIMENSIONS =
			new Registry<>("Dimensions", 2, DuplicatePolicy.THROW_EXCEPTION);
	
	/** The default dimension. */
	private static String defaultDim = null;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	public final DimensionInfo info;
	
	
	/**
	 * Every subclass of Dimension should have this constructor.
	 */
	public Dimension(DimensionInfo info) {
		this.info = info;
	}
	
	/**
	 * Creates the HostWorld object upon which this dimension will be hosted.
	 * Subclasses may override this to return a custom implementation of
	 * HostWorld to implement dimension-specific logic.
	 * 
	 * @param provider The world provider.
	 * 
	 * @throws NullPointerException if {@code provider} is {@code null}.
	 */
	public HostWorld createHost(WorldProvider provider) {
		return new HostWorld(provider, this);
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
	 * @param info The dimension info.
	 * 
	 * @return The Dimension, or {@code null} if there is no such dimension
	 * with the specified name.
	 * @throws RuntimeException if the Dimension object could not be
	 * instantiated.
	 */
	public static Dimension getDimension(DimensionInfo info) {
		Class<? extends Dimension> dimClass = DIMENSIONS.get(info.name);
		if(dimClass == null)
			return null;
		Factory<Dimension> factory = new ReflectiveFactory<>(dimClass, DimensionInfo.class);
		return factory.create(info);
	}
	
	/**
	 * @return The name of the default dimension.
	 */
	public static String defaultDimension() {
		return defaultDim;
	}
	
	/**
	 * Registers all the dimensions.
	 * 
	 * @throws IllegalStateException if the dimensions have already been
	 * registered.
	 */
	public static void registerDimensions() {
		registerDimension(true, "overworld", DimOverworld.class);
		
		DIMENSIONS.lock();
		if(defaultDim == null)
			throw new Error("A default dimension must be set!");
	}
	
	/**
	 * Registers a dimension.
	 * 
	 * @param isDefault Whether or not this is the default dimension.
	 * @param name The name of the dimension.
	 * @param dimClass The dimension's class.
	 * 
	 * @throws IllegalStateException if {@code isDefault} is {@code true}, but
	 * the default dimension has already been set.
	 * @throws RuntimeException see {@link Registry#register(Object, Object)}.
	 */
	private static void registerDimension(boolean isDefault, String name, Class<? extends Dimension> dimClass) {
		if(isDefault) {
			if(defaultDim != null)
				throw new IllegalStateException("Default dimension had already been set!");
			else
				defaultDim = name;
		}
		DIMENSIONS.register(name, dimClass);
	}
	
}
