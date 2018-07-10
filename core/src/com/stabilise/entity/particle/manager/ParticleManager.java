package com.stabilise.entity.particle.manager;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.core.Application;
import com.stabilise.core.Settings;
import com.stabilise.entity.particle.Particle;
import com.stabilise.util.Checks;
import com.stabilise.util.box.I32Box;
import com.stabilise.util.concurrent.event.Event;


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
    
    /** Caches all the particle pools. */
    private final Map<Class<? extends Particle>, ParticlePool<? extends Particle>> pools =
            new IdentityHashMap<>();
    
    /** Number of particles to be produced is the reciprocal of this value.
     * Boxed so that we can pass this off to emitters.  */
    private final I32Box reductionFactor = new I32Box();
    
    
    /**
     * Creates a new particle manager.
     */
    public ParticleManager() {
        refreshReductionFactor();
        
        Settings.NOTIFIER.addListener(Application.mainThreadExecutor(),
                new Event("particles"), this::onSettingChanged);
    }
    
    /**
     * Returns an emitter, or <i>source</i>, for particles of the specified
     * type.
     * 
     * @throws NullPointerException if {@code particleClass} is {@code null}.
     * @throws IllegalStateException if the given class has not been
     * registered.
     */
    public <T extends Particle> ParticleEmitter<T> getEmitter(Class<T> particleClass) {
        // Don't try to change this to map.computeIfAbsent(), it isn't worth
        // the hassle
        @SuppressWarnings("unchecked")
        ParticlePool<T> pool = (ParticlePool<T>) pools.get(particleClass);
        if(pool == null) {
            pool = new ParticlePool<>(particleClass);
            pools.put(particleClass, pool);
        }
        return new ParticleEmitter<>(pool, reductionFactor);
    }
    
    private void onSettingChanged(Event e) {
        // ignore the event
        refreshReductionFactor();
    }
    
    private void refreshReductionFactor() {
        int setting = Settings.getSettingParticles();
        if(setting == Settings.PARTICLES_ALL)
            reductionFactor.set(1);
        else if(setting == Settings.PARTICLES_REDUCED)
            reductionFactor.set(4);
        else if(setting == Settings.PARTICLES_NONE)
            reductionFactor.set(Integer.MAX_VALUE);
        else
            throw Checks.ISE("Bad value for particles setting: " + setting);
    }
    
    /**
     * Reclaims the given particle.
     * 
     * @see ParticlePool#reclaim(Particle)
     */
    public void reclaim(Particle p) {
        pools.get(p.getClass()).reclaim(p);
    }
    
    /**
     * Tries to release any unused pooled particles if possible, in order to
     * free up memory.
     */
    public void cleanup() {
        pools.values().forEach(ParticlePool::flush);
    }
    
    /**
     * Shuts down this particle manager by deregistering its event listener.
     */
    public void shutdown() {
        Settings.NOTIFIER.removeListener(new Event("particles"), this::onSettingChanged);
        pools.clear(); // also prod the gc
    }
    
}
