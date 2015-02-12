package com.stabilise.world.multidimensioned.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.gen.PerlinNoiseGenerator;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multidimensioned.Dimension;
import com.stabilise.world.multidimensioned.WorldProvider;

/**
 * The Overworld is the default world dimension.
 */
public class DimOverworld extends Dimension {
	
	public DimOverworld(Info info) {
		super(info);
	}
	
	@Override
	public WorldGenerator createWorldGenerator(WorldProvider provider, HostWorld world) {
		return new PerlinNoiseGenerator(provider, world);
	}
	
}
