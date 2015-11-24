package com.stabilise.entity;

import com.stabilise.entity.component.Component;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.controller.PlayerController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.damage.IDamageSource;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.collect.WeightingArrayList;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


public class Entity extends FreeGameObject {
    
    private      long        id;
    public       long        age;
    
    // Core physical properties
    public       float       dx, dy;
    public       AABB        aabb;
    public       boolean     facingRight;
    
    // Components
    public final CPhysics    physics;
    public       CController controller;
    public final CCore       core;
    
    public final WeightingArrayList<Component> components =
            new WeightingArrayList<>(new Component[2]);
    
    
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
        
        world.profiler().start("components");
        
        components.iterate(c -> {
            c.update(world, this);
            return c.remove();
        });
        
        world.profiler().next("controller");
        controller.update(world, this);
        world.profiler().next("core");
        core.update(world, this);
        world.profiler().next("physics");
        physics.update(world, this);
        world.profiler().end();
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
     * Adds a component to this entity.
     * 
     * @return This entity.
     * @throws NullPointerException if {@code c} is {@code null}.
     */
    public Entity addComponent(Component c) {
        components.add(c);
        return this;
    }
    
    /**
     * Posts an event to this entity. It is implicitly trusted that the event
     * is not null.
     * 
     * <p>A posted event first propagates through the ad-hoc list of
     * components, before finally being posted to the core, controller and
     * physics components. If any of the components' {@link
     * Component#handle(World, Entity, EntityEvent) handle} method returns
     * true, propagation of the event is halted and this method returns false.
     * 
     * @return true if no component consumed the event; false if it was fully
     * handled.
     */
    public boolean post(World w, EntityEvent e) {
        for(Component c : components)
            if(c.handle(w, this, e))
                return false;
        return !core.handle(w, this, e)
            && !controller.handle(w, this, e)
            && !physics.handle(w, this, e);
    }
    
    /**
     * Invoked when this entity is added to the world.
     */
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
    public boolean damage(World w, IDamageSource src) {
        return post(w, EDamaged.damaged(src));
    }
    
    public boolean isPlayerControlled() {
        return controller instanceof PlayerController;
    }
    
}
