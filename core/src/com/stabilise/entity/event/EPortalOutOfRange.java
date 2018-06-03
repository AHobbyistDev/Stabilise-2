package com.stabilise.entity.event;


public class EPortalOutOfRange extends EntityEvent {
    
    public final long portalID;
    
    public EPortalOutOfRange(long portalID) {
        super(Type.PORTAL_OUT_OF_RANGE);
        this.portalID = portalID;
    }
    
}
