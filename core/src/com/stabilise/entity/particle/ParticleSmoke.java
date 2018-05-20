package com.stabilise.entity.particle;

import com.stabilise.render.WorldRenderer;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.world.World;

/**
 * A smoke particle which appears when a Mob dies.
 */
public class ParticleSmoke extends ParticlePhysical {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The number of ticks after which a smoke particle despawns. */
    public static final int DESPAWN_TICKS = 120;
    
    
    private int despawn;
    public float opacity;
    
    
    @Override
    public void reset() {
        super.reset();
        opacity = 1f;
        despawn = -1;
    }
    
    @Override
    protected void update(World world) {
        dx *= 0.92f;
        dy -= (dy - 0.4f) * 0.08f;
        
        super.update(world);
        
        if(despawn == -1)
            despawn = 120 + (int)(30*world.rnd().nextGaussian());
        
        opacity = 1f - Interpolation.CUBIC.easeInTransform((float)age/despawn);
        
        if(age >= despawn)
            destroy();
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderSmoke(this);
    }
    
}
