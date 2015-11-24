package com.stabilise.entity.particle;

import com.stabilise.entity.FreeGameObject;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.world.World;

/**
 * A particle is a non-functional GameObject with solely aesthetic purposes.
 */
public abstract class Particle extends FreeGameObject {
    
    public static final TypeFactory<Particle> REGISTRY = new TypeFactory<>(
            new RegistryParams("ParticleRegistry", 8));
    
    static {
        register(0, ParticleFlame.class);
        register(1, ParticleSmoke.class);
        register(2, ParticleIndicator.class);
        register(3, ParticleExplosion.class);
    }
    
    private static void register(int id, Class<? extends Particle> clazz) {
        REGISTRY.registerUnsafe(id, clazz);
    }
    
    /** The age of the particle, in ticks. */
    public int age;
    
    
    /**
     * Creates a new Particle.
     */
    Particle() {
        reset();
    }
    
    @Override
    public void update(World world) {
        age++;
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
    
}
