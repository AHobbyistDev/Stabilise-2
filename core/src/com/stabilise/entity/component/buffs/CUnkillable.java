package com.stabilise.entity.component.buffs;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;


public class CUnkillable extends AbstractComponent {
    
    @Override
    public int getWeight() {
        // Above all the 0s so that we can just outright reject death
        return -1;
    }
    
    @Override
    public Action resolve(Component other) {
        return Action.REJECT;
    }
    
    @Override public void update(World w, Entity e, float dt) {}
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return ev.type() == EntityEvent.Type.KILLED;
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
