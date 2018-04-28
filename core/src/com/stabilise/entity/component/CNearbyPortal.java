package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CPortal;
import com.stabilise.util.Checks;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * Component that is present on an entity if there is a nearby portal. This
 * component manipulates the phantom of this entity in the other dimension.
 * 
 * @see CPortal
 * @see CPhantom
 */
@Incomplete
public class CNearbyPortal extends AbstractComponent {
    
    /** The portal that we are nearby. */
    private Entity portal;
    /** The phantom entity in the other dimension that we are controlling. */
    private Entity phantom;
    
    
    public CNearbyPortal() {
        
    }
    
    @Override
    public void init(Entity e) {
        // TODO
    }
    
    @Override
    public void update(World w, Entity e) {
        CPortal p = (CPortal) portal.core;
        phantom.pos.setSum(e.pos, p.offset).align();
    }
    
    @Override
    public int getWeight() {
        // Put this at the very end of the component list so that we update the
        // phantom's state after all other components have effected their
        // changes.
        return Integer.MAX_VALUE;
    }
    
    @Override
    public Action resolve(Component c) {
        // There may be multiple nearby portals, so keep both
        return Action.KEEP_BOTH;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
}
