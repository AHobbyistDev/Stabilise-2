package com.stabilise.world.gen;

import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.provider.WorldProvider;

/**
 * The generator for a player character's private dimension.
 */
public class PrivateDimensionGenerator extends WorldGenerator {
	
	public PrivateDimensionGenerator(WorldProvider<?> worldProv, HostWorld world) {
		super(worldProv, world);
	}
	
	@Override
	protected void generateRegion(Region r) {
		
	}
	
}
