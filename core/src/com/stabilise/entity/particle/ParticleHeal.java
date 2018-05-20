package com.stabilise.entity.particle;

import com.stabilise.render.WorldRenderer;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.world.World;


public class ParticleHeal extends ParticlePhysical {
    
    public ParticleHeal() {
        
    }
    
    private int despawn;
    public float opacity;
    
    @Override
    public void reset() {
        super.reset();
        opacity = 1f;
        despawn = -1;
    }
    
    @Override
    protected void update(World world) {
        dx *= 0.92f;
        dy -= (dy - 0.4f) * 0.08f;
        
        super.update(world);
        
        if(despawn == -1)
            despawn = 120 + (int)(30*world.rnd().nextGaussian());
        
        opacity = 1f - Interpolation.CUBIC.easeInTransform((float)age/despawn);
        
        if(age >= despawn)
            destroy();
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderHeal(this);
    }
    
}
