package com.stabilise.entity.component.core;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.damage.IDamageSource;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;

/**
 * The core component is the primary uniquifying aspect of an entity type
 * (i.e. under the old inheritance schema, a core would be equivalent to a
 * subclass of Entity).
 * 
 * <p>Cores contain secondary entity data, functionality and render data.
 */
public abstract class CCore extends AbstractComponent {
    
    @Override
    public void init(Entity e) {
        // A core is init()'d on entity construction, so there isn't anything
    	// that we'd usually need to do; thus we provide a default empty
    	// implementation.
    }
	
    /**
     * Renders the entity.
     */
    public abstract void render(WorldRenderer renderer, Entity e);
    
    /**
     * Returns the entity's AABB.
     */
    public abstract AABB getAABB();
    
    /**
     * Attempts to damage the entity.
     * 
     * <p>The default implementation does nothing and returns {@code false}.
     * 
     * @return true if the entity was damaged; {@code false} if not.
     */
    public boolean damage(World w, Entity e, IDamageSource src) { 
        return false; 
    }
    
    /**
     * Kills the entity.
     * 
     * <p>The default implementation invokes {@code e.destroy()}.
     */
    public void kill(World w, Entity e, IDamageSource src) {
        e.destroy();
    }
    
    @Override
    public int getWeight() {
        throw new RuntimeException("A CCore should not be put in the components list!");
    }
    
    @Override
    public Action resolve(Component other) {
        throw new RuntimeException("A CCore should not be put in the components list!");
    }
    
}
