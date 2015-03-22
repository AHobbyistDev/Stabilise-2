package com.stabilise.entity.particle;

import com.stabilise.world.World;

/**
 * A physical particle (for want of a better name) is a particle with a dx and
 * dy.
 */
public abstract class ParticlePhysical extends Particle {
	
	/** The particle's velocity along the x-axis. */
	public float dx;
	/** The particle's velocity along the y-axis. */
	public float dy;
	
	
	@Override
	public void update(World world) {
		super.update(world);
		
		x += dx;
		y += dy;
	}
	
}
