package com.stabilise.entity;

import java.util.LinkedList;
import java.util.List;

import com.stabilise.entity.component.Component;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.controller.PlayerController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.damage.DamageSource;
import com.stabilise.entity.effect.Effect;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


public class Entity extends FreeGameObject {
    
    private      long        id;
    public       long        age;
    
    // Core physical properties
    public       float       dx, dy;
    public       boolean     facingRight;
    public       AABB        aabb;
    public       boolean     invulnerable = false;
    
    // Components
    public final CPhysics    physics;
    public       CController controller;
    public final CCore       core;
    
    public final List<Component> components = new LinkedList<>();
    
    
    /**
     * Creates a new Entity. It is implicitly trusted that none of the
     * arguments are null.
     */
    public Entity(CPhysics p, CController co, CCore c) {
        physics    = p;
        controller = co;
        core       = c;
        
        aabb = core.getAABB();
        
        physics.init(this);
        controller.init(this);
        core.init(this);
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
    
    public Entity addComponent(Component c) {
        components.add(c);
        return this;
    }
    
    public boolean post(World w, EntityEvent e) {
        for(Component c : components)
            if(c.handle(w, this, e))
                return false;
        return core.handle(w, this, e)
            && controller.handle(w, this, e)
            && physics.handle(w, this, e);
    }
    
    public void onAdd(World w) {
        post(w, EntityEvent.ADDED_TO_WORLD);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        post(null, EntityEvent.DESTROYED);
    }
    
    /**
     * Attempts to damage this entity.
     * 
     * @return true if the entity was damaged; false if not.
     */
    public boolean damage(World w, DamageSource src) {
        return post(w, EDamaged.damaged(src));
    }
    
    public void applyEffect(Effect effect) {
        core.applyEffect(effect);
    }
    
    public boolean isPlayerControlled() {
        return controller instanceof PlayerController;
    }
    
}
