package com.stabilise.entity.particle;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.AbstractWorld;

/**
 * A smoke particle which appears when a Mob dies.
 */
public class ParticleSmoke extends ParticlePhysical {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of ticks after which a smoke particle despawns. */
	private static final int DESPAWN_TICKS = 120;
	
	/** The value by which a particle's dx is multiplied by each tick. */
	private static final float DX_MULT = 0.9f;
	
	
	/**
	 * Creates a new smoke particle.
	 * 
	 * @param world The world in which the particle will be placed.
	 */
	public ParticleSmoke(AbstractWorld world) {
		super(world);
		
		/*
		double angle = (0.16666667f + world.rng.nextFloat() / 3) * Math.PI;
		float velocity = 0.05f + world.rng.nextFloat() * 0.4f;
		boolean right = world.rng.nextBoolean();
		
		dx = (float) (right ? Math.cos(angle) * velocity : -Math.cos(angle) * velocity);
		dy = (float) (Math.sin(angle) * velocity);
		*/
		
		/*
		dx = (world.rng.nextFloat() * 0.4f) - 0.2f;
		dy = world.rng.nextFloat() * 0.015f;
		*/
	}
	
	@Override
	public void update() {
		super.update();
		
		dx *= DX_MULT;
		//dy *= DX_MULT;
		
		if(age == DESPAWN_TICKS)
			destroy();
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		renderer.renderSmoke(this);
	}
	
}
