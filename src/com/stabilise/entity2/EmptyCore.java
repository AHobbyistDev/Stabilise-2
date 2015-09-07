package com.stabilise.entity2;


/**
 * In preference to {@code nulls}.
 */
class EmptyCore extends ECore {
    
    public EmptyCore() {
        super(-1, -1);
    }
    
    @Override
    public boolean update(WorldDemo world) {
        world.emptySlots++;
        return false;
    }
    
}
