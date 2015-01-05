package com.stabilise.entity.particle;

import com.stabilise.entity.GameObject;
import com.stabilise.world.World;

/**
 * A particle is a non-functional GameObject with solely aesthetic purposes.
 */
public abstract class Particle extends GameObject {
	
	/** The age of the particle, in ticks. */
	public int age = 0;
	
	
	/**
	 * Creates a new Particle.
	 * 
	 * @param world The world in which the Particle will be placed.
	 */
	public Particle(World world) {
		super();
		this.world = world;
	}
	
	@Override
	public void update() {
		age++;
	}
	
}
