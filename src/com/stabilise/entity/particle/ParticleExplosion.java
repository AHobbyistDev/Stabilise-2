package com.stabilise.entity.particle;

import com.badlogic.gdx.graphics.Color;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Interpolation;
import com.stabilise.world.IWorld;

/**
 * A flashy explosion particle.
 */
public class ParticleExplosion extends Particle {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of ticks after which an explosion particle despawns. */
	private static final int DESPAWN_TICKS = 12;
	
	/** The initial colour of the explosion. */
	private static final Color COLOUR_INIT = new Color(0xFFFFFFFF);
	/** The final colour of the explosion. */
	private static final Color COLOUR_FINAL = new Color(0xFF660000); //0xAAFFA200
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The colour of the explosion. */
	public Color colour;
	
	/** The initial radius of the explosion, in tile-lengths. */
	private float radiusInit;
	/** The final radius of the explosion, in tile-lengths. */
	private float radiusFinal;
	/** The radius of the explosion, in tile-lengths. */
	public float radius;
	
	
	/**
	 * Creates a new explosion particle.
	 * 
	 * @param world The world in which the particle will be placed.
	 * @param initialRadius The initial radius of the explosion, in
	 * tile-lengths.
	 * @param finalRadius The final radius of the explosion, in tile-lengths.
	 */
	public ParticleExplosion(IWorld world, float initialRadius, float finalRadius) {
		super(world);
		
		radiusInit = initialRadius;
		radiusFinal = finalRadius;
		
		colour = new Color(COLOUR_INIT);
		radius = radiusInit;
	}
	
	@Override
	public void update() {
		super.update();
		
		float ratio = (float)age/DESPAWN_TICKS;
		
		colour.r = Interpolation.lerp(COLOUR_INIT.r, COLOUR_FINAL.r, ratio);
		colour.g = Interpolation.lerp(COLOUR_INIT.g, COLOUR_FINAL.g, ratio);
		colour.b = Interpolation.lerp(COLOUR_INIT.b, COLOUR_FINAL.b, ratio);
		colour.a = Interpolation.lerp(COLOUR_INIT.a, COLOUR_FINAL.a, ratio);
		
		radius = Interpolation.lerp(radiusInit, radiusFinal, ratio);
		
		if(age == DESPAWN_TICKS)
			destroy();
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		renderer.renderExplosion(this);
	}
	
}
