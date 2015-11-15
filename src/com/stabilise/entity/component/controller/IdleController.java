package com.stabilise.entity.component.controller;

import com.stabilise.entity.Entity;
import com.stabilise.world.World;

/**
 * Mobs with an IdleController have no defined behaviour; they do nothing.
 */
public class IdleController implements CController {
    
    /** The global IdleController instance. Since an IdleController does
     * nothing, this may be shared between multiple mobs. */
    public static final IdleController INSTANCE = new IdleController();
    
    
    // Only privately instantiable
    private IdleController() {
        super();
    }
    
    @Override
    public void init(World w, Entity e) {
        // do nothing
    }

    @Override
    public void update(World w, Entity e) {
        // do nothing
    }
    
}
