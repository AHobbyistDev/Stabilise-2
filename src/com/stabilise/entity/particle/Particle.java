package com.stabilise.entity.particle;

import com.stabilise.entity.FreeGameObject;
import com.stabilise.world.IWorld;

/**
 * A particle is a non-functional GameObject with solely aesthetic purposes.
 */
public abstract class Particle extends FreeGameObject {
	
	/** The age of the particle, in ticks. */
	public int age = 0;
	
	
	/**
	 * Creates a new Particle.
	 */
	public Particle() {
		super();
	}
	
	@Override
	public void update(IWorld world) {
		age++;
	}
	
}
