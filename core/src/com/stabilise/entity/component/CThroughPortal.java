package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.util.Checks;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * A CThroughPortal component is added to an entity when it moves through a
 * portal to another dimension. When an entity moves through a portal to
 * another dimension, it has all its variables and components swapped out with
 * its phantom. If this happens in the middle of an update tick, all of a
 * sudden it will be the phantom entity that is in the process of being
 * updated. As a result we may be left with a half-updated entity (with the
 * possibility of a number of component updates being erroneously made on the
 * phantom). To avoid this we swap out the entity with its phantom at the
 * <em>end</em> of the update tick. How to we accomplish this? You guessed it
 * - we add a CThroughPortal component with an extremely large weight to the
 * entity, which will be the very last thing to be updated on an entity in a
 * given tick.
 */
public class CThroughPortal extends AbstractComponent {
    
    public long portalID;
    
    public CThroughPortal(long portalID) {
        this.portalID = portalID;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        Entity phantom = getPhantom(e);
        if(phantom == null)
            Checks.ISE("No phantom?");
        
        
        
    }
    
    // A pretty inefficient way to get the phantom since we need to iterate
    // through all the entity's components, but at the moment I can't think of
    // a better way of going about this. Ah well, guess it's just one of the
    // tradeoffs of using this approach to going through portals.
    private Entity getPhantom(Entity e) {
        CNearbyPortal np;
        for(Component c : e.components) {
            if(c instanceof CNearbyPortal) {
                np = (CNearbyPortal) c;
                if(np.portalID == portalID) {
                    return np.phantom;
                }
            }
        }
        return null;
    }
    
    @Override
    public boolean shouldRemove() {
        return true;
    }
    
    @Override
    public int getWeight() {
        return Component.WEIGHT_CHANGE_DIMENSION;
    }
    
    @Override
    public Action resolve(Component c) {
        // Restrict an entity to only going through one portal per tick for
        // now. Deal with resolving multiple portal thingies later down the
        // line.
        return Action.REJECT;
    }
    
    // A CThroughPortal should only be present on an entity during an update
    // tick, after which is promptly removed. That is, there is no opportunity
    // for it to be serialised between ticks. As such we chuck ISEs.
    
    @Override
    public void importFromCompound(DataCompound c) {
        throw Checks.ISE();
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        throw Checks.ISE();
    }
    
}
