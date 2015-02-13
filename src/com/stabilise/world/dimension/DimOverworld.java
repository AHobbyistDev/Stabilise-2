package com.stabilise.world.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.PerlinNoiseGenerator;
import com.stabilise.world.gen.WorldGenerator;

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
