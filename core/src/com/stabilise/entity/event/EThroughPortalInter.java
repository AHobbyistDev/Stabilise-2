package com.stabilise.entity.event;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.world.World;

/**
 * This event is posted when an entity goes through an interdimensional portal,
 * immediately <em>after</em> having its components swapped out with the
 * phantom.
 */
public class EThroughPortalInter extends EThroughPortalIntra {
    
    /** Our old entity object/the new phantom entity. */
    public final Entity oldEntity;
    /** The dimension we just moved from. */
    public final World oldWorld;
    
    
    public EThroughPortalInter(Entity portal, CPortal portalCore,
            Entity oldEntity, World oldWorld) {
        super(Type.THROUGH_PORTAL_INTER, portal, portalCore);
        this.oldEntity = oldEntity;
        this.oldWorld = oldWorld;
    }
    
}
