package com.stabilise.entity;

import com.stabilise.entity.component.EntityData;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.component.state.CState;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


public class Entity extends FreeGameObject {
    
    public long        id;
    public long        age;
    public boolean     facingRight;
    
    public float       dx, dy;
    
    public AABB        aabb;
    
    public CPhysics    physics;
    public CController controller;
    public CState      state;
    
    
    public void init(World w, EntityData data) {
        physics    = data.physics;
        controller = data.controller;
        state      = data.state;
        
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
    
}
