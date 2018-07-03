package com.stabilise.entity.event;


/**
 * Event sent by a portal to an entity to notify it that it is "in range" of
 * the portal and should start paying attention to it. An event of this class
 * has two types: {@link Type#TRY_NEARBY_PORTAL} and {@link
 * Type#PORTAL_IN_RANGE}. The former type is used to determine whether the
 * entity is already aware of the portal; the latter is used to actually inform
 * it.
 */
public class EPortalInRange extends EntityEvent {
    
    public final long portalID;
    
    
    /**
     * @param testingIfKnown true for {@link Type#TRY_NEARBY_PORTAL}; false for
     * {@link Type#PORTAL_IN_RANGE}.
     */
    public EPortalInRange(long portalID, boolean testingIfKnown) {
        super(testingIfKnown ? Type.TRY_NEARBY_PORTAL : Type.PORTAL_IN_RANGE);
        this.portalID = portalID;
    }
    
}
