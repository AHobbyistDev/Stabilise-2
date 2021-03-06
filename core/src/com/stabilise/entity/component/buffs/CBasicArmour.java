package com.stabilise.entity.component.buffs;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.damage.DamageType;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.World;


public class CBasicArmour extends AbstractComponent {
    
    private static final int DMG_BLOCK = 8;
    private static final int BLOCK_EXTRA = 2;
    
    private int durability = 25;
    
    @Override
    public Action resolve(Component other) {
        return Action.KEEP_BOTH;
    }
    
    @Override public void update(World w, Entity e, float dt) {}
    
    @Override
    public boolean shouldRemove() {
        return durability == 0;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.DAMAGED && durability > 0) {
            EDamaged d = (EDamaged)ev;
            if(d.src.type() != DamageType.ATTACK)
                return false;
            int dmg = d.src.damage();
            int sub = Maths.min(DMG_BLOCK + w.rnd().nextInt(BLOCK_EXTRA+1), durability, dmg);
            dmg -= sub;
            durability -= sub;
            d.src.setDamage(dmg);
            
            // Don't stop the attack from propagating; we still want effects
            // to apply.
            //if(dmg == 0) return true;
        }
        return false;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        durability = c.getI32("durability");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        c.put("durability", durability);
    }
    
}
