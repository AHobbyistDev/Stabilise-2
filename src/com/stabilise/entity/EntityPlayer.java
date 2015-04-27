package com.stabilise.entity;

import com.stabilise.world.World;

/**
 * The player entity. Identical to a person entity, for now.
 */
public class EntityPlayer extends EntityPerson {
	
	/** The name of the player. */
	public String name;
	
	@Override
	public void update(World world) {
		super.update(world);
	}
	
	@Override
	public void kill() {
		// oh noes
	}
	
}
