package com.stabilise.entity.event;


public class EPortalInRange extends EntityEvent {
    
    public final long portalID;
    /** If true, this event is being sent as a notification and shouldn't be
     * eaten by {@link CNearbyPortal}. */
    public boolean notification = false;
    
    
    public EPortalInRange(long portalID) {
        super(Type.PORTAL_IN_RANGE);
        this.portalID = portalID;
    }
    
    /**
     * Returns true if he given portal ID matches that of this event,
     * <em>and</em> this event is not a notification.
     */
    public boolean matches(long portalID) {
        return !notification && this.portalID == portalID;
    }
    
}
