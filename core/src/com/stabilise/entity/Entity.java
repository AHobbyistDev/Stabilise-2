package com.stabilise.entity;

import com.stabilise.entity.component.Component;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.controller.CPlayerController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.core.CPhantom;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.damage.IDamageSource;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.collect.WeightingArrayList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Exportable;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;

/**
 * Entities are the main actors in the game; essentially all objects that are
 * not tiles are entities, including the player, other mobs, items, etc.
 * 
 * <p>Implementation-wise, an entity is essentially just a bag of {@link
 * Component components}, an approach I have opted for over inheritance since
 * it is far more flexible.
 */
public class Entity extends GameObject implements Exportable {
    
    private long       id;
    public  long       age;
    
    // Core physical properties
    public float       dx, dy;
    public AABB        aabb;
    public boolean     facingRight;
    
    // Components
    public CPhysics    physics;
    public CController controller;
    public CCore       core;
    
    public final WeightingArrayList<Component> components =
            new WeightingArrayList<>(new Component[2]);
    
    
    /**
     * Creates a new Entity. The given components are all {@link
     * Component#init(Entity) initialised}.
     * 
     * @throws NullPointerException if any argument is null.
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
    protected void update(World world) {
        age++;
        
        world.profiler().start("components");
        
        components.iterate(c -> {
            c.update(world, this);
            return c.shouldRemove();
        });
        
        world.profiler().next("controller");
        controller.update(world, this);
        world.profiler().next("core");
        core.update(world, this);
        world.profiler().next("physics");
        physics.update(world, this);
        world.profiler().end();
        
        // After all is said and done, realign the entity's position
        pos.align();
    }
    
    @Override
    public boolean updateAndCheck(World world) {
    	if(super.updateAndCheck(world)) {
    		post(world, EntityEvent.REMOVED_FROM_WORLD);
    		return true;
    	}
    	return false;
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
     * Adds a component to this entity. Invokes {@link Component#init(Entity)}
     * on the component.
     * 
     * @return This entity.
     * @throws NullPointerException if {@code c} is {@code null}.
     */
    public Entity addComponent(Component c) {
        if(components.add(c))
        	c.init(this);
        return this;
    }
    
    /**
     * Gets the first component on this entity which satisfies the given
     * predicate.
     * 
     * @return the first such component, or {@code null} if no component
     * matching the predicate exists.
     */
    /*
    public Component getComponent(Predicate<Component> pred) {
    	for(int i = 0; i < components.size(); i++)
    		if(pred.test(components.get(i)))
    			return components.get(i);
    	return null;
    }
    */
    
    /**
     * Gets the first component on this entity which is an instance of the
     * specified class. Since this method uses a linear search it is obviously
     * not very efficient when an entity has many components, so try not to
     * overuse this.
     * 
     * @return the first such component, or {@code null} if no component of the
     * given class exists.
     */
    public <T extends Component> T getComponent(Class<T> clazz) {
    	for(int i = 0; i < components.size(); i++)
    		if(clazz.isInstance(components.get(i)))
    			return clazz.cast(components.get(i));
    	return null;
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
        return components.iterateUntil(c -> c.handle(w, this, e))
            && !core.handle(w, this, e)
            && !controller.handle(w, this, e)
            && !physics.handle(w, this, e);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        post(null, EntityEvent.DESTROYED);
    }
    
    /**
     * Attempts to damage this entity.
     * 
     * @return true if the entity was damaged; false if not (e.g., due to
     * invulnerability).
     */
    public boolean damage(World w, IDamageSource src) {
        return post(w, EDamaged.damaged(src));
    }
    
    /**
     * Returns true if this entity is controlled by the player, i.e., if its
     * {@link #controller} is a {@link CPlayerController}.
     */
    public boolean isPlayerControlled() {
        return controller instanceof CPlayerController;
    }
    
    /**
     * Returns true if this entity is a phantom (a "clone" of an entity in a
     * different dimension).
     * 
     * @see CPhantom
     */
    public boolean isPhantom() {
    	return core instanceof CPhantom;
    }
    
    @Override
    public void importFromCompound(DataCompound o) {
        
    }
    
    @Override
    public void exportToCompound(DataCompound o) {
        
    }
    
}
