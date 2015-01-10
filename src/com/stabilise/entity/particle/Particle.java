package com.stabilise.entity.particle;

import com.stabilise.entity.FreeGameObject;
import com.stabilise.world.BaseWorld;

/**
 * A particle is a non-functional GameObject with solely aesthetic purposes.
 */
public abstract class Particle extends FreeGameObject {
	
	/** The age of the particle, in ticks. */
	public int age = 0;
	
	
	/**
	 * Creates a new Particle.
	 * 
	 * @param world The world in which the Particle will be placed.
	 */
	public Particle(BaseWorld world) {
		super();
		this.world = world;
	}
	
	@Override
	public void update() {
		age++;
	}
	
}
