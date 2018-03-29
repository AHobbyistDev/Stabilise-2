package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.particle.Particle;
import com.stabilise.entity.particle.ParticleSource;
import com.stabilise.world.World;


/**
 * An Effect which produces particles on an entity. This is a convenience class
 * which helps abstract over any effect with an associated ParticleSource.
 */
public abstract class ParticleEffect<T extends Particle> extends Effect {
    
    protected ParticleSource<T> particleSrc;
    
    public ParticleEffect(int duration) {
        super(duration);
    }
    
    @Override
    public void update(World w, Entity e) {
        super.update(w, e);
        
        if(particleSrc == null)
            particleSrc = w.getParticleManager().getSource(particleClass());
    }
    
    /**
     * Gets the class of this effect's associated particle. With good generics
     * this shouldn't be necessary. Such is life.
     */
    protected abstract Class<T> particleClass();
    
}