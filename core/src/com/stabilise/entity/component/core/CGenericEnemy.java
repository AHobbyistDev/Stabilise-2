package com.stabilise.entity.component.core;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.hitbox.Hitbox;
import com.stabilise.entity.particle.ParticleFlame;
import com.stabilise.entity.particle.manager.ParticleEmitter;
import com.stabilise.item.Items;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.shape.AABB;
import com.stabilise.util.shape.Polygon;
import com.stabilise.world.World;


public class CGenericEnemy extends CBaseMob {
    
    private static final AABB ENEMY_AABB = new AABB(-0.5f, 0, 1, 2);
    
    private ParticleEmitter<?> srcFlame;
    
    @Override
    public void init(Entity e) {
        super.init(e);
        // Temporary initial value setting
        maxHealth = 30;
        health = 30;
        
        jumpVelocity = 15f;
        jumpCrouchDuration = 8;
        acceleration = 0.8f;
        airAcceleration = 0.2f;
        maxDx = 13f;
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
        Polygon p = new Polygon(0.5f,0.0f, 0.5f,2.0f, 3.5f,3.0f, 3.5f,-1.0f);
        Hitbox h = new Hitbox(e.id(), facingRight
                ? p
                : p.reflect(),
                w.rnd().nextInt(5) + 8);
        h.hits = -1;
        h.force = 75f;
        h.fx = facingRight ? 1f : -1f;
        h.fy = 0.2f;
        h.persistent = true;
        h.persistenceTimer = 5;
        w.addHitbox(h, e.pos);
        
        float d = Math.abs(e.dx);
        
        Position dummyPos = srcSmoke.dummyPos;
        if(facingRight) {
            dummyPos.set(e.pos, 0.5f, 0f);
            srcSmoke.createBurst(w, 150, dummyPos, 0f, 2f, 3f+d, 30f+d, -Maths.PIf/4, Maths.PIf/4);
            srcFlame.createBurst(w, 150, dummyPos, 0f, 2f, 2f+d, 10f+d, -Maths.PIf/4, Maths.PIf/4);
        } else {
            dummyPos.set(e.pos, -0.5f, 0f);
            srcSmoke.createBurst(w, 150, dummyPos, 0f, 2f, 3f+d, 30f+d, 3*Maths.PIf/4, 5*Maths.PIf/4);
            srcFlame.createBurst(w, 150, dummyPos, 0f, 2f, 2f+d, 10f+d, 3*Maths.PIf/4, 5*Maths.PIf/4);
        }
    }
    
    @Override
    public void specialAttack(World w, Direction direction) {
        Polygon p = new Polygon(new float[] { 0.5f,0.0f, 0.5f,2.0f, 20.0f,3.0f, 20.0f,-1.0f });
        Hitbox h = new Hitbox(e.id(), facingRight
                ? p
                : p.reflect(),
                w.rnd().nextInt(5) + 8);
        h.hits = -1;
        h.force = 45f;
        h.fx = facingRight ? 1f : -1f;
        h.fy = 0.2f;
        h.persistent = true;
        h.persistenceTimer = 5;
        w.addHitbox(h, e.pos);
        
        float d = Math.abs(e.dx);
        
        Position dummyPos = srcSmoke.dummyPos;
        if(facingRight) {
            dummyPos.set(e.pos, 0.5f, 0f);
            srcSmoke.createBurst(w, 150, dummyPos, 0f, 2f, 2f+d, 90f+d, -Maths.PIf/16, Maths.PIf/16);
            srcFlame.createBurst(w, 150, dummyPos, 0f, 2f, 2f+d, 35f+d, -Maths.PIf/16, Maths.PIf/16);
        } else {
            dummyPos.set(e.pos, -0.5f, 0f);
            srcSmoke.createBurst(w, 150, dummyPos, 0f, 2f, 2f+d, 90f+d, 15*Maths.PIf/16, 17*Maths.PIf/16);
            srcFlame.createBurst(w, 150, dummyPos, 0f, 2f, 2f+d, 35f+d, 15*Maths.PIf/16, 17*Maths.PIf/16);
        }
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD)
            srcFlame = w.particleEmitter(ParticleFlame.class);
        else if(ev.type() == EntityEvent.Type.KILLED) {
            dropItem(w, e, Items.APPLE, 1, 0.02f);
            dropItem(w, e, Items.SWORD, 1, 0.02f);
            dropItem(w, e, Items.ARROW, 1, 0.02f);
        }
        return super.handle(w, e, ev);
    }
    
}
