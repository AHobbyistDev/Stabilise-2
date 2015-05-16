package com.stabilise.world.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.RegionCache;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.provider.WorldProvider;

/**
 * The private player-local client-only dimension.
 */
public class DimPrivate extends Dimension {
	
	public DimPrivate(Info info) {
		super(info);
	}
	
	@Override
	public WorldGenerator createWorldGenerator(WorldProvider<?> provider, 
			HostWorld world, RegionCache cache) {
		return null;
	}
	
}
