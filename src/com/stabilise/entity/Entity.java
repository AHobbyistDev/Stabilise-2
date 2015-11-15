package com.stabilise.entity;

import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.controller.PlayerController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.damage.DamageSource;
import com.stabilise.entity.effect.Effect;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


public class Entity extends FreeGameObject {
    
    private long        id;
    public  long        age;
    private boolean     initialised = false;
    public  boolean     facingRight;
    
    public  float       dx, dy;
    
    public  AABB        aabb;
    public  boolean     invulnerable = false;
    
    public  CPhysics    physics;
    public  CController controller;
    public  CCore       core;
    
    
    /**
     * Sets the components of this entity. None of them should be null.
     */
    public Entity construct(CPhysics p, CController c, CCore s) {
        physics    = p;
        controller = c;
        core      = s;
        return this;
    }
    
    /**
     * Initialises this entity. This is invoked when it is added to the world.
     */
    public void init(World w) {
        if(!initialised) {
            initialised = true;
            
            aabb = core.getAABB();
            
            physics.init(w, this);
            controller.init(w, this);
            core.init(w, this);
        }
    }
    
    @Override
    public void update(World world) {
        age++;
        
        controller.update(world, this);
        core.update(world, this);
        physics.update(world, this);
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        core.render(renderer, this);
    }
    
    /**
     * Sets the ID of this entity. This should only ever be invoked when it is
     * added to the world.
     */
    public void setID(long id) {
        this.id = id;
    }
    
    /**
     * @return This entity's ID.
     */
    public long id() {
        return id;
    }
    
    /**
     * Attempts to damage this entity.
     * 
     * @return true if the entity was damaged; false if not.
     */
    public boolean damage(World w, DamageSource src) {
        return core.damage(w, this, src);
    }
    
    public void applyEffect(Effect effect) {
        core.applyEffect(effect);
    }
    
    public boolean isPlayerControlled() {
        return controller instanceof PlayerController;
    }
    
}
