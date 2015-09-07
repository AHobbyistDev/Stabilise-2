package com.stabilise.entity;

import com.stabilise.item.BoundedContainer;
import com.stabilise.item.Container;
import com.stabilise.world.World;

/**
 * The player entity. Identical to a person entity, for now.
 */
public class EntityPlayer extends EntityPerson {
    
    /** The name of the player. */
    public String name;
    
    public final Container inventory = new BoundedContainer(64);
    public int curSlot = 0;
    
    
    @Override
    public void update(World world) {
        super.update(world);
    }
    
    @Override
    public void kill() {
        // oh noes
    }
    
}
