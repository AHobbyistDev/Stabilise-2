package com.stabilise.entity.particle;

import com.stabilise.world.World;

/**
 * A physical particle (for want of a better name) is a particle with a dx and
 * dy.
 */
public abstract class ParticlePhysical extends Particle {
    
    /** The particle's velocity along the x/y-axes. */
    public float dx, dy;
    
    
    @Override
    public void update(World world) {
        super.update(world);
        
        x += dx * world.getTimeIncrement();
        y += dy * world.getTimeIncrement();
    }
    
    @Override
    public void reset() {
        super.reset();
        dx = dy = 0f;
    }
    
}
