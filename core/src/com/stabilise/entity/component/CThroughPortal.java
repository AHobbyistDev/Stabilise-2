package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CPhantom;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.entity.event.EThroughPortalInter;
import com.stabilise.util.Checks;
import com.stabilise.util.Log;
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
        switchDim(w, e);
    }
    
    private void switchDim(World w1, Entity e1) {
        Entity pe = w1.getEntity(portalID);
        CPortal pc = (CPortal) pe.core;
        
        World w2 = pc.pairedWorld(w1);
        Entity e2 = w2.getEntity(e1.id()); // the phantom, should have same ID
        
        if(e2 == null) {
            Log.get().postWarning("No phantom to switch into!");
            return;
        }
        
        ((CPhantom)e2.core).original = e2;
        
        e1.swapComponents(e2);
        
        if(e2.isPlayerControlled()) {
            w1.asAbstract().unsetPlayer(e1);
            w2.asAbstract().setPlayer(e2);
        }
        
        // Phantom is now the new entity; post the event
        e2.post(w2, new EThroughPortalInter(pe, pc, e1, w1));
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
