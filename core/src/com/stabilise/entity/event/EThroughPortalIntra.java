package com.stabilise.entity.event;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CPortal;

/**
 * This event is posted when an entity goes through an intradimensional portal,
 * immediately <em>after</em> being teleported.
 */
public class EThroughPortalIntra extends EntityEvent {
    
    /** The portal entity that we went through. */
    public final Entity portal;
    /** The core of the portal that we went through. */
    public final CPortal portalCore;
    
    protected EThroughPortalIntra(Type type, Entity pe, CPortal pc) {
        super(type);
        this.portal = pe;
        this.portalCore = pc;
    }
    
    public EThroughPortalIntra(Entity portal, CPortal portalCore) {
        this(Type.THROUGH_PORTAL_INTRA, portal, portalCore);
    }
    
}
