package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;


public class CDamageAmplifier extends AbstractComponent {
    
    public static final CDamageAmplifier AMPLIFIER = new CDamageAmplifier();
    
    @Override public void init(Entity e) {}
    @Override public void update(World w, Entity e) {}
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.DAMAGED) {
            EDamaged dmg = (EDamaged)ev;
            dmg.src.setDamage(dmg.src.damage() * 10);
        }
        return false;
    }
    
    @Override
    public Action resolve(Component c) {
        return Action.KEEP_BOTH; // for the lols
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
