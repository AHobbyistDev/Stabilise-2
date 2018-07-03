package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;


/**
 * An effect that produces a brief trail of fire at the entity's base.
 */
public class CEffectFireTrail extends CParticleEffect<ParticleFlame> {
    
    public CEffectFireTrail(int duration) {
        super(duration);
    }
    
    @Override
    public void init(Entity e) {}
    
    @Override
    protected Class<ParticleFlame> particleClass() {
        return ParticleFlame.class;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        super.update(w, e, dt);
        
        Position dummyPos = emitter.dummyPos;
        emitter.createBurst(5, dummyPos.set(e.pos, -0.2f, -0.2f), 0.4f, 0.4f, 0.2f, 1.0f, 0f, Maths.TAUf);
    }
    
    @Override
    public Action resolve(Component c) {
        return Action.KEEP_BOTH;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        // nothing for now
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        // nothing for now
    }
    
}
