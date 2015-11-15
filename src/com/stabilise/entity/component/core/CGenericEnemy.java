package com.stabilise.entity.component.core;

import com.stabilise.entity.Entity;
import com.stabilise.entity.hitbox.Hitbox;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.world.AbstractWorld.ParticleSource;
import com.stabilise.world.World;


public class CGenericEnemy extends BaseMob {
    
    private static final AABB ENEMY_AABB = new AABB(-0.5f, 0, 1, 2);
    
    private ParticleSource srcFlame;
    
    @Override
    public void init(World w, Entity e) {
        super.init(w, e);
        // Temporary initial value setting
        maxHealth = 20;
        health = 20;
        
        jumpVelocity = 15f;
        jumpCrouchDuration = 8;
        swimAcceleration = 0.08f;
        acceleration = 1.3f;
        airAcceleration = 1f;
        maxDx = 13f;
        
        srcFlame = w.getParticleManager().getSource(new ParticleFlame());
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderEnemy(e, this);
    }
    
    @Override
    public AABB getAABB() {
        return ENEMY_AABB;
    }
    
    @Override
    public void attack(World w, Direction direction) {
        Polygon p = new Polygon(new float[] { 0.5f,0.0f, 0.5f,2.0f, 3.0f,3.0f, 3.0f,-1.0f });
        Hitbox h = new Hitbox(e.id(), e.facingRight
                ? p
                : p.reflect(),
                w.getRnd().nextInt(5) + 8);
        h.hits = -1;
        h.force = 15f;
        h.fx = e.facingRight ? 1f : -1f;
        h.fy = 0.2f;
        h.persistent = true;
        h.persistenceTimer = 5;
        w.addHitbox(h, e.x, e.y);
        
        float d = Math.abs(e.dx);
        
        if(e.facingRight) {
            srcSmoke.createBurst(150, e.x+0.5d, e.x+0.5d, e.y, e.y+2d, 3f+d, 30f+d, -Maths.PIf/4, Maths.PIf/4);
            srcFlame.createBurst(150, e.x+0.5d, e.x+0.5d, e.y, e.y+2d, 2f+d, 10f+d, -Maths.PIf/4, Maths.PIf/4);
        } else {
            srcSmoke.createBurst(150, e.x-0.5d, e.x-0.5d, e.y, e.y+2d, 3f+d, 30f+d, 3*Maths.PIf/4, 5*Maths.PIf/4);
            srcFlame.createBurst(150, e.x-0.5d, e.x-0.5d, e.y, e.y+2d, 2f+d, 10f+d, 3*Maths.PIf/4, 5*Maths.PIf/4);
        }
    }
    
    @Override
    public void specialAttack(World w, Direction direction) {
        Polygon p = new Polygon(new float[] { 0.5f,0.0f, 0.5f,2.0f, 20.0f,3.0f, 20.0f,-1.0f });
        Hitbox h = new Hitbox(e.id(), e.facingRight
                ? p
                : p.reflect(),
                w.getRnd().nextInt(5) + 8);
        h.hits = -1;
        h.force = 15f;
        h.fx = e.facingRight ? 1f : -1f;
        h.fy = 0.2f;
        h.persistent = true;
        h.persistenceTimer = 5;
        w.addHitbox(h, e.x, e.y);
        
        float d = Math.abs(e.dx);
        
        if(e.facingRight) {
            srcSmoke.createBurst(150, e.x+0.5d, e.x+0.5d, e.y, e.y+2d, 2f+d, 90f+d, -Maths.PIf/16, Maths.PIf/16);
            srcFlame.createBurst(150, e.x+0.5d, e.x+0.5d, e.y, e.y+2d, 2f+d, 35f+d, -Maths.PIf/16, Maths.PIf/16);
        } else {
            srcSmoke.createBurst(150, e.x-0.5d, e.x-0.5d, e.y, e.y+2d, 2f+d, 90f+d, 15*Maths.PIf/16, 17*Maths.PIf/16);
            srcFlame.createBurst(150, e.x-0.5d, e.x-0.5d, e.y, e.y+2d, 2f+d, 35f+d, 15*Maths.PIf/16, 17*Maths.PIf/16);
        }
    }
    
}
