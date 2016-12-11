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
    /** Caches all the particle pools used and shared by each ParticleSource. */
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
        // Don't try to change this to map.computeIfAbsent(), it isn't worth
        // the hassle
        @SuppressWarnings("unchecked")
        ParticleSource<T> src = (ParticleSource<T>) sources.get(particleClass);
        if(src == null) {
            src = new ParticleSource<T>(world, particleClass);
            sources.put(particleClass, src);
        }
        return src;
    }
    
    /**
     * Tries to release any unused pooled particles if possible, in order to
     * free up memory.
     */
    public void cleanup() {
        sources.values().forEach(ParticleSource::cleanup);
    }
    
}
