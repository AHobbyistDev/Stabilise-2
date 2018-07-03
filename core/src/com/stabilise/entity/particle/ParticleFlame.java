package com.stabilise.entity.particle;

import com.stabilise.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A tiny flame particle.
 */
public class ParticleFlame extends ParticlePhysical {
    
    /** The number of ticks after which a flame particle despawns. */
    public static final int DESPAWN_TICKS = 30;
    
    
    
    /** The opacity of the particle. */
    public float opacity;
    
    
    @Override
    protected void update(World world, float dt) {
        super.update(world, dt);
        
        dy += -0.02f / 32f; //world.gravity / 32f;
        
        opacity = (float)(DESPAWN_TICKS - age) / DESPAWN_TICKS;
        
        if(age == DESPAWN_TICKS)
            destroy();
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderFlame(this);
    }
    
    @Override
    public void reset() {
        super.reset();
        opacity = 1.0f;
    }
    
}
