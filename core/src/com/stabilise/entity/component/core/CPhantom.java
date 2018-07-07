package com.stabilise.entity.component.core;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


/**
 * When an entity is near a portal to another dimension, we create a "phantom"
 * clone of it in the other dimension. The phantom receives events (e.g.,
 * hitboxes where appropriate, etc.) and forwards them to the original entity.
 * When the entity crosses the portal boundary it has its components hotswapped
 * out with those of the phantom; the original then becomes the phantom.
 * 
 * @see CPortal
 * @see CNearbyPortal
 */
public class CPhantom extends CCore {
    
    /** The entity in the other dimension that we are the phantom of. */
    public Entity original;
    
    /** A phantom may be linked to multiple nearby portals to/from the same
     * dimension. If the original entity moves out of range of one of the
     * portals we don't want the phantom to disappear immediately since it is
     * still linked to the others. As such we keep a count of how many portals
     * this phantom is 'anchored' to and only remove it when all anchors are
     * gone. Default: 1. */
    public int anchors = 1;
    
    
    public CPhantom() {}
    
    /**
     * @param original the entity in the other dimension that we are the
     * phantom of
     */
    public CPhantom(Entity original) {
    	this.original = original;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        // Do nothing?
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        // Do nothing?
    }
    
    @Override
    public AABB getAABB() {
        return original.core.getAABB();
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
        
        // TODO: propagate hits but not lifetime events such as REMOVED_FROM_WORLD
        //return base.post(w, ev);
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
}
