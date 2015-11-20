package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.world.World;


public class CDamageAmplifier implements Component {
    
    public static final CDamageAmplifier AMPLIFIER = new CDamageAmplifier();
    
    @Override public void init(Entity e) {}
    @Override public void update(World w, Entity e) {}
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.DAMAGED) {
            EDamaged dmg = (EDamaged)ev;
            dmg.src.damage *= 2;
        }
        return false;
    }
    
}
