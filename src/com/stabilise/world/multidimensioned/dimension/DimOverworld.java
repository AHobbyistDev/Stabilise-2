package com.stabilise.world.multidimensioned.dimension;

import com.stabilise.world.HostWorld;
import com.stabilise.world.gen.PerlinNoiseGenerator;
import com.stabilise.world.gen.WorldGenerator;
import com.stabilise.world.multidimensioned.Dimension;
import com.stabilise.world.multidimensioned.WorldProvider;


public class DimOverworld extends Dimension {
	
	public DimOverworld(String name) {
		super(name);
	}
	
	@Override
	public WorldGenerator createWorldGenerator(WorldProvider provider, HostWorld world) {
		return new PerlinNoiseGenerator(provider, world);
	}
	
}
