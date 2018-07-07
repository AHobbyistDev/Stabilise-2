package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.event.EntityEvent.Type;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;


public class CDebug extends AbstractComponent {
    
    public void update(World w, Entity e, float dt) {
        
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type().equals(Type.DESTROYED))
            System.out.println("Destroyed??");
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
