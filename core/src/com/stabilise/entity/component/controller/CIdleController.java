package com.stabilise.entity.component.controller;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * A controller implementation which does nothing. This is suitable for
 * pretty much every non-mob entity.
 */
public class CIdleController extends CController {
    
    /** The global IdleController instance. Since an IdleController does
     * nothing, this may be shared between multiple entities. */
    public static final CIdleController INSTANCE = new CIdleController();
    
    
    // Only privately instantiable
    private CIdleController() {}
    
    @Override
    public void init(Entity e) {
        // do nothing
    }

    @Override
    public void update(World w, Entity e) {
        // do nothing
    }

    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        // nothing to do
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        // nothing to do
    }
    
}
