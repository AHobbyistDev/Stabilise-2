package com.stabilise.entity.particle;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.world.AbstractWorld;


/**
 * Manages particles for a world by performing the following tasks:
 * 
 * <ul>
 * <li>Decides whether or not to add particles to the world based on the
 *     game settings.
 * <li>Acts as a particle generator.
 * <li>Handles particle pooling to avoid excessive object creation.
 * </ul>
 */
@NotThreadSafe
public class ParticleManager {
    
    private final AbstractWorld world;
    /** Caches all the particle pools used and shared by each
     * ParticleSource. */
    private final Map<Class<? extends Particle>, ParticleSource<? extends Particle>> sources =
            new IdentityHashMap<>();
    
    
    
    /**
     * Creates a new particle manager.
     * 
     * @throws NullPointerException if {@code world} is {@code null}.
     */
    public ParticleManager(AbstractWorld world) {
        this.world = Objects.requireNonNull(world);
    }
    
    /**
     * Returns a generator, or <i>source</i>, of particles of the same type
     * as the specified particle.
     * 
     * @param templateParticle The particle to use as the template for all
     * particles created by the returned {@code ParticleSource}.
     * 
     * @throws NullPointerException if {@code templateParticle} is {@code
     * null}.
     */
    public <T extends Particle> ParticleSource<T> getSource(Class<T> particleClass) {
        @SuppressWarnings("unchecked")
        ParticleSource<T> source = (ParticleSource<T>)sources.get(particleClass);
        if(source == null) {
            source = new ParticleSource<>(
                    world,
                    new ParticlePool<>(particleClass),
                    ParticlePhysical.class.isAssignableFrom(particleClass)
            );
        }
        return source;
    }
    
    /**
     * Tries to release any apparently unused pooled particles if possible,
     * in order to free up memory.
     */
    public void cleanup() {
        for(ParticleSource<?> src : sources.values())
            src.cleanup();
    }
    
}
