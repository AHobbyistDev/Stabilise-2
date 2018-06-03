package com.stabilise.entity.event;


public class EPortalInRange extends EntityEvent {
    
    public final long portalID;
    
    
    public EPortalInRange(long portalID) {
        super(Type.PORTAL_IN_RANGE);
        this.portalID = portalID;
    }
    
}
