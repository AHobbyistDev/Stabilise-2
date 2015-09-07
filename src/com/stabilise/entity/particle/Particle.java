package com.stabilise.entity.particle;

import com.stabilise.entity.FreeGameObject;
import com.stabilise.world.AbstractWorld.ParticlePool;
import com.stabilise.world.World;

/**
 * A particle is a non-functional GameObject with solely aesthetic purposes.
 */
public abstract class Particle extends FreeGameObject {
    
    /** The age of the particle, in ticks. */
    public int age;
    
    /** The pool to which this particle belongs. May be {@code null}. */
    public ParticlePool pool;
    
    
    /**
     * Creates a new Particle.
     */
    public Particle() {
        reset();
    }
    
    @Override
    public void update(World world) {
        age++;
    }
    
    @Override
    public boolean updateAndCheck(World world) {
        if(super.updateAndCheck(world)) {
            if(pool != null)
                pool.reclaim(this);
            return true;
        }
        return false;
    }
    
    /**
     * Resets this particle to a default state, and releases any used
     * resources, etc, in such a way that this particle can henceforth be
     * reused as if constructed anew.
     * 
     * <p>Subclasses should remember to invoke {@code super.reset()} if this
     * is overridden.
     */
    public void reset() {
        destroyed = false;
        age = 0;
    }
    
    /**
     * Creates a duplicate of this particle. Unlike the contract for {@link
     * #clone()}, the fields of this object are not copied; this method simply
     * instantiates a fresh new particle.
     */
    public abstract Particle duplicate();
    
}
