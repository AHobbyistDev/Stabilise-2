package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;


/**
 * An effect that produces a brief trail of fire at the entity's base.
 */
public class EffectFireTrail extends ParticleEffect<ParticleFlame> {
    
    public EffectFireTrail(int duration) {
        super(duration);
    }
    
    @Override
    public void init(Entity e) {}
    
    @Override
    protected Class<ParticleFlame> particleClass() {
        return ParticleFlame.class;
    }
    
    @Override
    public void update(World w, Entity e) {
        super.update(w, e);
        
        //particleSrc.createAt(e.x, e.y); // one particle per tick for now
        //particleSrc.createBurst(2, e.x, e.y, 0.2f, 1.0f, 0f, Maths.TAUf);
        Position dummyPos = particleSrc.dummyPos;
        particleSrc.createBurst(5, dummyPos.set(e.pos, -0.2f, -0.2f), 0.4f, 0.4f, 0.2f, 1.0f, 0f, Maths.TAUf);
    }
    
    @Override
    public int getWeight() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public Action resolve(Component c) {
        return Action.KEEP_BOTH;
    }
    
}
