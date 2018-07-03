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
    
    
    public EThroughPortal(Entity portal, CPortal portalCore) {
        super(Type.THROUGH_PORTAL);
        this.portal = portal;
        this.portalCore = portalCore;
    }
    
}
