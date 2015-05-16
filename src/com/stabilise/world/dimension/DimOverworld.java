package com.stabilise.world.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.RegionCache;
import com.stabilise.world.gen.PerlinNoiseGenerator;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.provider.WorldProvider;

/**
 * The Overworld is the default world dimension.
 */
public class DimOverworld extends Dimension {
	
	public DimOverworld(Info info) {
		super(info);
	}
	
	@Override
	public WorldGenerator createWorldGenerator(WorldProvider<?> provider,
			HostWorld world, RegionCache cache) {
		return new PerlinNoiseGenerator(provider, world, cache);
	}
	
}
