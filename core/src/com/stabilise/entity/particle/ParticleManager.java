package com.stabilise.entity.particle;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.concurrent.NotThreadSafe;

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
     * Returns a generator, or <i>source</i>, for particles of the specified
     * type.
     * 
     * @throws NullPointerException if {@code particleClass} is {@code null}.
     * @throws IllegalStateException if the given class has not been
     * registered.
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
     * Tries to release any unused pooled particles if possible, in order to
     * free up memory.
     */
    public void cleanup() {
        for(ParticleSource<?> src : sources.values())
            src.cleanup();
    }
    
}
