package com.stabilise.entity.controller;

import com.stabilise.entity.EntityMob;

/**
 * A MobController is that which provides a mob with the actions it is to
 * perform.
 */
public abstract class MobController {
	
	/** The Mob which is the subject of the controller. */
	protected EntityMob mob;
	
	
	/**
	 * Creates a new MobController.
	 */
	public MobController() {
		// nothing to see here, move along
	}
	
	/**
	 * Updates the MobController.
	 */
	public void update() {
		// nothing to see here, move along
	}
	
	/**
	 * Sets the mob which is to be the subject of the controller.
	 * 
	 * @param mob The mob.
	 * 
	 * @return This MobController, for chaining operations.
	 */
	public MobController setControlledMob(EntityMob mob) {
		this.mob = mob;
		return this;
	}
	
}
