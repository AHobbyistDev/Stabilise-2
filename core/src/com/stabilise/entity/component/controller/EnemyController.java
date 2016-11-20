package com.stabilise.entity.component.controller;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.BaseMob;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Direction;
import com.stabilise.world.World;

/**
 * Extremely simplistic mob controller.
 */
public class EnemyController extends CController {
    
    /** Actions for the current, incredibly simplistic, AI. */
    private static enum EnumAction {
        IDLE, MOVE;
    };
    
    /** The number of ticks for which the enemy is to continue its current
     * action.*/
    private int actionTimeout = 1;
    /** The enemy's current action. */
    private EnumAction action = EnumAction.IDLE;
    
    private BaseMob mob;
    
    @Override
    public void init(Entity e) {
        mob = (BaseMob)e.core;
    }
    
    @Override
    public void update(World w, Entity e) {
        if(true/*!e.dead*/) {
            if(--actionTimeout == 0) {
                refreshAction(w, e);
            }
            
            if(action == EnumAction.MOVE) {
                if(e.facingRight)
                    mob.move(Direction.RIGHT);
                else
                    mob.move(Direction.LEFT);
            }
        }
    }
    
    private void refreshAction(World w, Entity e) {
        float rnd = w.rnd().nextFloat();
        if(rnd < 0.45) {
            action = EnumAction.IDLE;
            actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
        } else if(rnd < 0.55) {
            action = EnumAction.IDLE;
            e.facingRight = (!e.facingRight);
            actionTimeout = 120 + (int)(w.rnd().nextFloat() * 180);
        } else if(rnd < 0.70) {
            action = EnumAction.IDLE;
            mob.jump();
            actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
        } else {
            if(rnd < 0.85) e.facingRight = (!e.facingRight);
            action = EnumAction.MOVE;
            actionTimeout = 30 + (int)(w.rnd().nextFloat() * 90);
        }
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.DAMAGED)
            refreshAction(w, e);
        return false;
    }
    
}
