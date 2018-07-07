package com.stabilise.entity;

import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.event.EPortalInRange;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.event.EntityEvent.Type;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;


public class CDebug extends AbstractComponent {
    
    public void update(World w, Entity e, float dt) {
        
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type().equals(Type.PORTAL_IN_RANGE))
            System.out.println("Portal in range: " + ((EPortalInRange)ev).portalID);
        return false;
    }
    
    @Override
    public Action resolve(Component c) {
        return Action.REJECT;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        
    }
}
