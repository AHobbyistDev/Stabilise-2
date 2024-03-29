package com.stabilise.entity.hitbox;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.ELinkedHitboxCollision;
import com.stabilise.util.shape.Shape;
import com.stabilise.world.World;

/**
 * A LinkedHitbox is a hitbox which is linked to a specific entity other than
 * its owner (e.g., as in a projectile). When the hitbox is destroyed, its
 * linked entity is too.
 */
public class LinkedHitbox extends Hitbox {
    
    /** The entity which the hitbox is linked to - not necessarily its owner. */
    public final long linkedID;
    
    
    /**
     * Creates a new Hitbox.
     * 
     * @param ownerID The ID of the Hitbox's owner.
     * @param boundingBox The Hitbox's bounding box.
     * @param damage The damage the hitbox deals.
     */
    public LinkedHitbox(long ownerID, Shape boundingBox, int damage, long linkedID) {
        super(ownerID, boundingBox, damage);
        this.linkedID = linkedID;
    }
    
    @Override
    protected void moveToOwner(World w) {
        Entity e = w.getEntity(linkedID);
        if(e != null)
            pos.set(e.pos);
        else
            destroy();
    }
    
    @Override
    protected void onHit(World w) {
        super.onHit(w);
        
        // Inform the linked entity that we hit something
        Entity e = w.getEntity(linkedID);
        if(e != null)
            e.post(w, new ELinkedHitboxCollision(hits));
    }
    
}
