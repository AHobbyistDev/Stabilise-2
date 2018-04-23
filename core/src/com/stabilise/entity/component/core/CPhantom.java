package com.stabilise.entity.component.core;

import com.stabilise.entity.Entity;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


/**
 * When an entity is near a portal to another dimension, we create a "phantom"
 * clone of it in the other dimension. The phantom receives events (e.g.,
 * hitboxes where appropriate, etc.) and forwards them to the original entity.
 * When the entity crosses the portal boundary it has its components hotswapped
 * out with those of the phantom; the original then becomes the phantom.
 */
public class CPhantom extends CCore {
    
    /** The entity in the other dimension that we are the phantom of. */
    public Entity base;
    /** The portal that connects us to the dimension that our base is from. */
    public Entity portal;
    
    
    /**
     * @param base the entity in the other dimension that we are the phantom of
     * @param portal the portal that connects to the dimension the base is in
     */
    public CPhantom(Entity base, Entity portal) {
    	this.base = base;
    	this.portal = portal;
    }
    
    @Override
    public void update(World w, Entity e) {
        // Do nothing?
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        // Do nothing?
    }
    
    @Override
    public AABB getAABB() {
        return base.core.getAABB();
    }
    
}
