package com.stabilise.entity.particle;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.IWorld;

/**
 * A tiny flame particle.
 */
public class ParticleFlame extends ParticlePhysical {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of ticks after which the particle despawns. */
	public static final int DESPAWN_TICKS = 30;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The opacity of the particle. */
	public float opacity = 1.0f;
	
	
	/**
	 * Creates a name flame particle.
	 * 
	 * @param world The world in which the particle is to be placed.
	 */
	public ParticleFlame(IWorld world) {
		super(world);
	}
	
	@Override
	public void update() {
		super.update();
		
		dy += -0.02f / 32f; //world.gravity / 32f;
		
		opacity = (float)(DESPAWN_TICKS - age) / DESPAWN_TICKS;
		
		if(age == DESPAWN_TICKS)
			destroy();
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		renderer.renderFlame(this);
	}
	
}
