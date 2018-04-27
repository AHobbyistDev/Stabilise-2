package com.stabilise.entity.particle;

import java.util.function.Supplier;

import com.stabilise.entity.GameObject;
import com.stabilise.util.collect.registry.RegistryParams;
import com.stabilise.util.collect.registry.TypeFactory;
import com.stabilise.world.World;

/**
 * A particle is a non-functional GameObject with solely aesthetic purposes.
 * 
 * <p>All subclasses of Particle should have the default parameterless
 * constructor and should not perform any construction initialisation (as
 * particles may be initialised {@link TheUnsafe unsafely} at runtime);
 * instead, any initialisation code should be performed by {@link #reset()}.
 */
public abstract class Particle extends GameObject {
    
    public static final TypeFactory<Particle> REGISTRY = new TypeFactory<>(
            new RegistryParams("ParticleRegistry", 8));
    
    static {
        register(0, ParticleFlame.class, ParticleFlame::new);
        register(1, ParticleSmoke.class, ParticleSmoke::new);
        register(2, ParticleIndicator.class, ParticleIndicator::new);
        register(3, ParticleExplosion.class, ParticleExplosion::new);
    }
    
    private static void register(int id, Class<? extends Particle> clazz, Supplier<Particle> constructor) {
        REGISTRY.register(id, clazz, constructor);
        // Can't use registerUnsafe anymore since particles have a Position
        // object which needs to be initialised.
        //REGISTRY.registerUnsafe(id, clazz);
    }
    
    /** The age of the particle, in ticks. */
    public int age;
    
    
    // Package-private constructor
    Particle() {}
    
    @Override
    protected void update(World world) {
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
