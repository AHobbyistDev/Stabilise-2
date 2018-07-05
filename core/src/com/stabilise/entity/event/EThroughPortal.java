package com.stabilise.entity.event;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CPortal;

/**
 * This event is posted when an entity goes through a portal, immediately
 * before being teleported/moved to the new dimension. Handlers can access
 * the new dimension through the portal entity's core.
 */
public class EThroughPortal extends EntityEvent {
    
    /** The portal entity that we went through. */
    public final Entity portal;
    /** The core of the portal that we went through. */
    public final CPortal portalCore;
    
    
    /** The new entity object for the entity that went through the portal. That
     * is, this will be its former phantom if the portal was interdimensional,
     * and is set by the CNearbyPortal component. This is needed so that the
     * physics component continues to update the correct entity after its
     * components are swapped out with those of the phantom. This is null if
     * the portal is intradimensional. */
    public Entity newEntity = null;
    
    
    public EThroughPortal(Entity portal, CPortal portalCore) {
        super(Type.THROUGH_PORTAL);
        this.portal = portal;
        this.portalCore = portalCore;
    }
    
}
