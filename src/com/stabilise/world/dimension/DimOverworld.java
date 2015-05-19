package com.stabilise.world.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.gen.PerlinNoiseGenerator;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multiverse.Multiverse;

/**
 * The Overworld is the default world dimension.
 */
public class DimOverworld extends Dimension {
	
	public DimOverworld(Info info) {
		super(info);
	}
	
	@Override
	public WorldGenerator generatorFor(Multiverse<?> multiverse,
			HostWorld world) {
		return new PerlinNoiseGenerator(multiverse, world);
	}
	
}
