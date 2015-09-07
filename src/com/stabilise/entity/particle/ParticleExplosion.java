package com.stabilise.entity.particle;

import com.badlogic.gdx.graphics.Color;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.world.World;

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
    
    private static final Interpolation interpCol = Interpolation.CIRCULAR.EASE_IN;
    private static final Interpolation interpSize = Interpolation.CIRCULAR.EASE_OUT;
    
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
     * @param initialRadius The initial radius of the explosion, in
     * tile-lengths.
     * @param finalRadius The final radius of the explosion, in tile-lengths.
     */
    public ParticleExplosion(float initialRadius, float finalRadius) {
        radiusInit = initialRadius;
        radiusFinal = finalRadius;
        
        colour = new Color(COLOUR_INIT);
        radius = radiusInit;
    }
    
    @Override
    public void update(World world) {
        super.update(world);
        
        float ratio = (float)age/DESPAWN_TICKS;
        
        colour.r = interpCol.apply(COLOUR_INIT.r, COLOUR_FINAL.r, ratio);
        colour.g = interpCol.apply(COLOUR_INIT.g, COLOUR_FINAL.g, ratio);
        colour.b = interpCol.apply(COLOUR_INIT.b, COLOUR_FINAL.b, ratio);
        colour.a = interpCol.apply(COLOUR_INIT.a, COLOUR_FINAL.a, ratio);
        
        radius = interpSize.apply(radiusInit, radiusFinal, ratio);
        
        if(age == DESPAWN_TICKS)
            destroy();
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderExplosion(this);
    }
    
    @Override
    public Particle duplicate() {
        return new ParticleExplosion(0,0);
    }
    
}
