package com.stabilise.entity.particle;

import com.stabilise.world.AbstractWorld;

/**
 * A physical particle (for want of a better name) is a particle with a dx and
 * dy.
 */
public abstract class ParticlePhysical extends Particle {
	
	/** The particle's velocity along the x-axis. */
	public float dx;
	/** The particle's velocity along the y-axis. */
	public float dy;
	
	
	/**
	 * Creates a new Particle.
	 * 
	 * @param world The world in which the Particle will be placed.
	 */
	public ParticlePhysical(AbstractWorld world) {
		super(world);
	}
	
	@Override
	public void update() {
		super.update();
		
		x += dx;
		y += dy;
	}
	
}
