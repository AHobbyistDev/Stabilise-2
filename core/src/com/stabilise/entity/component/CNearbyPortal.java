package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CPhantom;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.entity.event.EPortalInRange;
import com.stabilise.entity.event.EPortalOutOfRange;
import com.stabilise.entity.event.EThroughPortalInter;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * Component that is present on an entity if there is a nearby portal. This
 * component manipulates the phantom of this entity in the other dimension.
 * 
 * @see CPortal
 * @see CPhantom
 */
public class CNearbyPortal extends AbstractComponent {
    
    /** The ID of the portal that we are nearby. */
    public long portalID;
    private boolean remove = false;
    
    /** The phantom entity we are controlling; null if we don't have one
     * (because the portal leads to the same dimension). */
    public Entity phantom;
    
    
    
    
    public CNearbyPortal() {}
    
    public CNearbyPortal(long portalID) {
        this.portalID = portalID;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        if(remove)
            return;
        
        Entity portal = w.getEntity(portalID);
        if(portal != null) {
            // If we're far enough away, we can stop tracking the portal
            if(e.pos.distSq(portal.pos) > CPortal.NEARBY_MAX_DIST_SQ)
                onPortalOutOfRange(w, e, true);
            // If we have a phantom, update its position to match ours
            else if(phantom != null)
                updatePhantomPos(e, (CPortal)portal.core);
        } else // portal is null which means it's gone, so we forget about it
            onPortalOutOfRange(w, e, true);
    }
    
    private void onPortalOutOfRange(World w, Entity e, boolean destroyPhantom) {
        remove = true; // remove this component
        e.post(w, new EPortalOutOfRange(portalID)); // let other components know
        
        // get rid of the phantom, if it exists and it isn't needed for other portals
        if(destroyPhantom && phantom != null && --((CPhantom)phantom.core).anchors == 0)
            phantom.destroy();
    }
    
    /**
     * Updates the phantom's position appropriately (assuming {@link #phantom}
     * is non-null).
     * 
     * @param e the entity this component is attached to
     * @param portalCore the core of the portal though which the phantom lives
     */
    public void updatePhantomPos(Entity e, CPortal portalCore) {
        phantom.pos.setSum(e.pos, portalCore.offset).align();
    }
    
    @Override
    public boolean shouldRemove() {
        return remove;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        switch(ev.type()) {
            case TRY_NEARBY_PORTAL:
                return ((EPortalInRange) ev).portalID == portalID;
            case THROUGH_PORTAL_INTER:
                EThroughPortalInter ev0 = (EThroughPortalInter) ev;
                if(ev0.portal.id() == portalID) {
                    // We now swap to being the associated "nearby portal"
                    // component for the portal we just came out of, and the
                    // original entity is now the phantom.
                    portalID = ev0.portalCore.pairID;
                    phantom = ev0.oldEntity;
                } else if(e == phantom)
                    // The entity just got swapped into its phantom! This means
                    // that the entity went through a different portal to the
                    // same dimension. In this case we shut down this component
                    // *carefully*, i.e. without our phantom/the entity.
                    onPortalOutOfRange(w, e, false);
                else
                    // we're in a completely different dimension -- time for
                    // the phantom to go!
                    onPortalOutOfRange(w, e, true);
                return false;
            default:
                return false;
        }
    }
    
    @Override
    public int getWeight() {
        return Component.WEIGHT_NEARBY_PORTAL;
    }
    
    @Override
    public Action resolve(Component c) {
        // There may be multiple nearby portals, so keep both
        return Action.KEEP_BOTH;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        portalID = c.getI64("portalID");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        c.put("portalID", portalID);
    }
    
}
