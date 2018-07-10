package com.stabilise.entity;

import com.stabilise.entity.component.Component;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.controller.CPlayerController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.core.CPhantom;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.damage.IDamageSource;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.collect.WeightingArrayList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
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
    
  //public final Position pos;     // inherited from GameObject
    /** This entity's velocity, in tiles/sec (NOT tiles/tick). */
    public float       dx, dy;
    /** Every entity has an associated AABB. This is used for physics (i.e.
     * collision with tiles) and for hitbox detection. However, for entities
     * which do not move nor get hit (e.g. portals), this often goes unused. */
    public AABB        aabb;
    
    // The three privileged components
    public CCore       core;
    public CPhysics    physics;
    public CController controller;
    
    // ad hoc components
    public WeightingArrayList<Component> components =
            new WeightingArrayList<>(new Component[2]);
    
    
    /**
     * Creates a new Entity, but does not initialise any components. This
     * should only really be used if you're constructing an entity from a
     * DataCompound via {@link #importFromCompound(DataCompound)}.
     */
    public Entity() {
        super(true);
    }
    
    /**
     * Creates a new Entity. The given components are all {@link
     * Component#init(Entity) initialised}.
     * 
     * @throws NullPointerException if any argument is null.
     */
    public Entity(CCore c, CPhysics p, CController co) {
        super(true);
        
        core       = c;
        physics    = p;
        controller = co;
        
        initComponents();
    }
    
    private void initComponents() {
        aabb = core.getAABB();
        
        core.init(this);
        physics.init(this);
        controller.init(this);
    }
    
    @Override
    protected void update(World world, float dt) {
        age++;
        
        // Run controller first so that input is dealt with first
        // Then run the core to affect any core behaviour
        // Then run physics to move the entity around
        // Finally run all the ad hoc components. These come last largely so
        // that the camera component (if it exists) updates and any
        // CNearbyPortal components update the phantom positions after all
        // other movement has been effected.
        
        world.profiler().next("controller");
        controller.update(world, this, dt);
        world.profiler().next("core");
        core.update(world, this, dt);
        world.profiler().next("physics");
        physics.update(world, this, dt);
        world.profiler().end();
        
        world.profiler().start("components");
        components.iterate(c -> {
            c.update(world, this, dt);
            return c.shouldRemove();
        });
        
        // After all is said and done, realign the entity's position
        pos.align();
    }
    
    @Override
    public boolean updateAndCheck(World world, float dt) {
    	if(super.updateAndCheck(world, dt)) {
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
        // TODO: think about how to deal with in the future:
        // WeightingArrayList allows elements to be added to the list while the
        // list is being iterated over. For us this means that a component may
        // add a new component when it is updated. However due to the way 
        
        if(components.add(c))
        	c.init(this);
        return this;
    }
    
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
     * @return true if if the event was fully handled, i.e. no component
     * consumed the event; false if the event was halted by some component.
     */
    public boolean post(World w, EntityEvent ev) {
        return !components.any(c -> c.handle(w, this, ev))
            && !core.handle(w, this, ev)
            && !controller.handle(w, this, ev)
            && !physics.handle(w, this, ev);
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
     * Swaps the components of this entity with the given entity (which should
     * be this entity's phantom.)
     * 
     * @throws IllegalStateException if the other entity doesn't have the same
     * ID as this one.
     */
    public void swapComponents(Entity other) {
        if(other.id != id)
            Checks.ISE("Swapping components of entities with different IDs: " +
                    id + ", " + other.id);
        
        // Swap destroyed flags
        boolean tmp1 = other.destroyed;
        other.destroyed = destroyed;
        destroyed = tmp1;
        
        // No need to swap positions
        //Position tmp2 = other.pos.clone();
        //other.pos.set(pos);
        //pos.set(tmp2);
        
        // Swap age
        long tmp3 = other.age;
        other.age = age;
        age = tmp3;
        
        // Swap dx, dy
        float tmp4 = other.dx;
        other.dx = dx;
        dx = tmp4;
        tmp4 = other.dy;
        other.dy = dy;
        dy = tmp4;
        
        // Swap AABB
        AABB tmp5 = other.aabb;
        other.aabb = aabb;
        aabb = tmp5;
        
        // Swap core
        CCore tmp6 = other.core;
        other.core = core;
        core = tmp6;
        
        // Swap physics
        CPhysics tmp7 = other.physics;
        other.physics = physics;
        physics = tmp7;
        
        // Swap controller
        CController tmp8 = other.controller;
        other.controller = controller;
        controller = tmp8;
        
        // Swap ad hoc components
        WeightingArrayList<Component> tmp9 = other.components;
        other.components = components;
        components = tmp9;
    }
    
    /**
     * Returns true if this entity is controlled by the player, i.e., if its
     * {@link #controller} is a {@link CPlayerController}.
     */
    public boolean isPlayerControlled() {
        return controller instanceof CPlayerController;
    }
    
    /**
     * Returns true if this entity is a portal.
     * 
     * @see CPortal
     */
    public boolean isPortal() {
        return core instanceof CPortal;
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
    public void importFromCompound(DataCompound dc) {
        id = dc.getI64("id");
        age = dc.getI64("age");
        
        dc.getInto(pos);
        dx = dc.getF32("dx");
        dy = dc.getF32("dy");
        
        DataCompound comp = dc.getCompound("components");
        
        core = (CCore) Component.fromCompound(comp.getCompound("core"));
        controller = (CController) Component.fromCompound(comp.getCompound("controller"));
        physics = (CPhysics) Component.fromCompound(comp.getCompound("physics"));
        
        initComponents();
        
        DataList adhoc = comp.getList("ad hoc");
        while(adhoc.hasNext()) {
            Component c = Component.fromCompound(adhoc.getCompound());
            if(c == null)
                throw new RuntimeException("invalid component oh no");
            
            // By assumption of the entity being saved in a valid state, this
            // should return true, so no need to check if(components.add(c))
            components.add(c);
            c.init(this);
        }
    }
    
    @Override
    public void exportToCompound(DataCompound dc) {
        dc.put("id", id);
        dc.put("age", age);
        
        dc.put(pos);
        dc.put("dx", dx);
        dc.put("dy", dy);
        
        DataCompound comp = dc.childCompound("components");
        Component.toCompound(comp.childCompound("core"), core);
        Component.toCompound(comp.childCompound("controller"), controller);
        Component.toCompound(comp.childCompound("physics"), physics);
        
        DataList adhoc = comp.childList("ad hoc");
        components.forEach(c -> Component.toCompound(adhoc.childCompound(), c));
    }
    
    
    
    
    
    /**
     * Reads an entity from the given DataCompound. Equivalent to
     * 
     * <pre>
     * Entity e = new Entity();
     * e.importFromCompound(c);
     * return e;
     * </pre>
     */
    public static Entity fromCompound(DataCompound c) {
        Entity e = new Entity();
        e.importFromCompound(c);
        return e;
    }
    
    
}
