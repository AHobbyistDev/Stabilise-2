package com.stabilise.entity.component.effect;

import com.stabilise.core.Constants;
import com.stabilise.core.Settings;
import com.stabilise.entity.Entity;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.damage.GeneralSource;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.ParticleSource;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;

/**
 * The effect a Mob has when it is on fire.
 * 
 * <p>A mob which is on fire will take 2 damage a second and constantly produce
 * flame particles.
 */
public class EffectFire extends Effect {
    
    private ParticleSource<?> particleSrc;
    
    
    /**
     * Creates a new fire effect.
     * 
     * @param duration The duration of the effect, in ticks.
     */
    public EffectFire(int duration) {
        super(duration);
    }
    
    @Override
    public void init(Entity e) {}
    
    @Override
    public void update(World w, Entity e) {
        super.update(w, e);
        
        if(particleSrc == null)
            particleSrc = w.getParticleManager().getSource(ParticleFlame.class);
        
        if(Settings.settingParticlesAll())
            createFireParticle(w, e);
        else if(Settings.settingParticlesReduced() && age % 3 == 0)
            createFireParticle(w, e);
        
        if(age % Constants.TICKS_PER_SECOND == 0)
            e.damage(w, GeneralSource.fire(2 + w.rnd().nextInt(2)));
    }
    
    /**
     * Creates a fire particle.
     * 
     * @param target The target of the effect.
     */
    private void createFireParticle(World world, Entity target) {
        particleSrc.createBurst(1, 0.2f, 2.0f, Maths.PIf / 6.0f,
                Maths.PIf * 5.0f / 6.0f, target);
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
    }

    @Override
    public int getWeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Action resolve(Component c) {
        // If we get a new fire effect while active just add the durations.
        if(c instanceof EffectFire) {
            EffectFire e = (EffectFire)c;
            duration += e.duration;
            return Action.REJECT;
        } else
            return Action.KEEP_BOTH;
    }
    
}
