package com.stabilise.entity.particle;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Colour;
import com.stabilise.world.World;

/**
 * A flashy explosion particle.
 */
public class ParticleExplosion extends Particle {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of ticks after which an explosion particle despawns. */
	private static final int DESPAWN_TICKS = 10;
	
	/** The initial colour of the explosion. */
	private static final Colour COLOUR_INIT = new Colour(0xFFFFFFFF);
	/** The final colour of the explosion. */
	private static final Colour COLOUR_FINAL = new Colour(0xAAFFA200);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The colour of the explosion. */
	public Colour colour;
	/** The alpha of the explosion. */
	public float alpha;
	
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
	public ParticleExplosion(World world, float initialRadius, float finalRadius) {
		super(world);
		
		radiusInit = initialRadius;
		radiusFinal = finalRadius;
		
		colour = new Colour(COLOUR_INIT);
		alpha = COLOUR_INIT.getAlpha();
		radius = radiusInit;
	}
	
	@Override
	public void update() {
		super.update();
		
		float ratio = (float)age/DESPAWN_TICKS;
		
		colour.setRed(COLOUR_FINAL.getRed() * ratio + COLOUR_INIT.getRed() * (1 - ratio));
		colour.setGreen(COLOUR_FINAL.getGreen() * ratio + COLOUR_INIT.getGreen() * (1 - ratio));
		colour.setBlue(COLOUR_FINAL.getBlue() * ratio + COLOUR_INIT.getBlue() * (1 - ratio));
		
		alpha = COLOUR_FINAL.getAlpha() * ratio + COLOUR_INIT.getAlpha() * (1 - ratio);
		
		radius = radiusFinal * ratio + radiusInit * (1 - ratio);
		
		if(age == DESPAWN_TICKS)
			destroy();
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		renderer.renderExplosion(this);
	}
	
}
