package com.stabilise.entity.component.controller;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CBaseMob;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Direction;
import com.stabilise.util.Log;
import com.stabilise.world.World;

/**
 * Extremely simplistic mob controller.
 */
public class CEnemyController extends CController {
    
    /** Actions for the current, incredibly simplistic, AI. */
    private static enum EnumAction {
        IDLE, MOVE;
    };
    
    /** The number of ticks for which the enemy is to continue its current
     * action.*/
    private int actionTimeout = 1;
    /** The enemy's current action. */
    private EnumAction action = EnumAction.IDLE;
    
    private CBaseMob mob;
    
    private boolean scared = false;
    
    @Override
    public void init(Entity e) {
        mob = (CBaseMob)e.core;
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
        if(scared) {
            if(rnd < 0.9) {
                Entity p = w.getPlayers().iterator().next();
                if(p == null) {
                    Log.get().postWarning("[EnemyController] Player list is empty???");
                    return;
                }
                // run away from the player
                e.facingRight = e.pos.diffX(p.pos) < 0;
                action = EnumAction.MOVE;
                actionTimeout = 30 + (int)(w.rnd().nextFloat() * 60);
            } else {
                scared = false;
                action = EnumAction.IDLE;
                actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
            }
        } else {
            if(rnd < 0.45) { // idle
                action = EnumAction.IDLE;
                actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
            } else if(rnd < 0.55) { // idle but change direction we're facing
                action = EnumAction.IDLE;
                e.facingRight = (!e.facingRight);
                actionTimeout = 120 + (int)(w.rnd().nextFloat() * 180);
            } else if(rnd < 0.70) { // do a random jump
                action = EnumAction.IDLE;
                mob.jump();
                actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
            } else { // move in the direction we're facing
                if(rnd < 0.85) e.facingRight = (!e.facingRight);
                action = EnumAction.MOVE;
                actionTimeout = 30 + (int)(w.rnd().nextFloat() * 90);
            }
        }
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.DAMAGED) {
            scared = true;
            refreshAction(w, e);
        }
        return false;
    }
    
}
