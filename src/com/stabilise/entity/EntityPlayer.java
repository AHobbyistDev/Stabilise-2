package com.stabilise.entity;

import com.stabilise.world.BaseWorld;

/**
 * The player entity. Identical to a person entity, for now.
 */
public class EntityPlayer extends EntityPerson {
	
	/** The name of the player. */
	public String name;
	
	
	/**
	 * Creates a new Player.
	 * 
	 * @param world The world in which the player will be placed.
	 */
	public EntityPlayer(BaseWorld world) {
		super(world);
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public void kill() {
		// oh noes
	}
	
}
