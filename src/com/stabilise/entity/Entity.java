package com.stabilise.entity;

import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.component.state.CState;
import com.stabilise.entity.damage.DamageSource;
import com.stabilise.entity.effect.Effect;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


public class Entity extends FreeGameObject {
    
    public long        id;
    public long        age;
    public boolean     facingRight;
    
    public float       dx, dy;
    
    public AABB        aabb;
    public boolean     invulnerable = false;
    
    public CPhysics    physics;
    public CController controller;
    public CState      state;
    
    
    public Entity construct(CPhysics p, CController c, CState s) {
        physics    = p;
        controller = c;
        state      = s;
        return this;
    }
    
    public void init(World w) {
        aabb = state.getAABB();
        
        physics.init(w, this);
        controller.init(w, this);
        state.init(w, this);
    }
    
    @Override
    public void update(World world) {
        age++;
        
        controller.update(world, this);
        physics.update(world, this);
    }
    
    public long id() { return id; }
    
    public boolean damage(World w, DamageSource src) {
        return state.damage(w, this, src);
    }
    
    public void applyEffect(Effect effect) {
        state.applyEffect(effect);
    }
    
}
