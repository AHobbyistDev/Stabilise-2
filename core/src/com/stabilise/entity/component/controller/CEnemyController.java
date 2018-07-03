package com.stabilise.entity.component.controller;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.core.CBaseMob;
import com.stabilise.entity.event.EDamaged;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Direction;
import com.stabilise.util.io.data.DataCompound;
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
    private int actionTimeout = 30;
    /** The enemy's current action. */
    private EnumAction action = EnumAction.IDLE;
    
    private CBaseMob mob;
    
    private boolean scared = false;
    private boolean aggro = false;
    private long aggressorID = -1;
    
    
    @Override
    public void init(Entity e) {
        mob = (CBaseMob)e.core;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        if(true/*!e.dead*/) {
            if(--actionTimeout <= 0) {
                refreshAction(w, e);
            }
            
            if(action == EnumAction.MOVE) {
                if(mob.facingRight)
                    mob.move(Direction.RIGHT);
                else
                    mob.move(Direction.LEFT);
            }
        }
    }
    
    private void refreshAction(World w, Entity e) {
        float rnd = w.rnd().nextFloat();
        if(scared) {
            if(rnd < 0.85) {
                Entity aggressor = w.getEntity(aggressorID);
                if(aggressor != null) // run away from aggressor
                    mob.facingRight = e.pos.diffX(aggressor.pos) < 0;
                action = EnumAction.MOVE;
                actionTimeout = 30 + (int)(w.rnd().nextFloat() * 60);
            } else {
                scared = false;
                action = EnumAction.IDLE;
                actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
            }
        } else {
            if(rnd < 0.45) { // idle
                if(aggro && rnd < 0.15) { // attack!
                    Entity aggressor = w.getEntity(aggressorID);
                    if(aggressor != null && e.pos.distSq(aggressor.pos) < 4*4) {
                        if(rnd < 0.005)
                            mob.specialAttack(w, Direction.DOWN);
                        else  
                            mob.attack(w, mob.facingRight ? Direction.RIGHT : Direction.LEFT);
                    }
                } else if(rnd < 0.0005) // attack for no reason very rarely
                    mob.attack(w, mob.facingRight ? Direction.RIGHT : Direction.LEFT);
                
                action = EnumAction.IDLE;
                actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
            } else if(rnd < 0.55) { // idle but change direction we're facing
                action = EnumAction.IDLE;
                mob.facingRight = !mob.facingRight;
                actionTimeout = 120 + (int)(w.rnd().nextFloat() * 180);
            } else if(rnd < 0.70) { // do a random jump
                mob.jump();
                if(rnd < 0.65) { // move 2/3rds of the time
                    action = EnumAction.MOVE;
                    if(rnd < 0.60) // flip direction half the time
                        mob.facingRight = !mob.facingRight;
                    actionTimeout = 30 + (int)(w.rnd().nextFloat() * 30);
                } else {
                    action = EnumAction.IDLE;
                    actionTimeout = 180 + (int)(w.rnd().nextFloat() * 180);
                }
            } else { // move in the direction we're facing
                if(rnd < 0.85) mob.facingRight = !mob.facingRight;
                action = EnumAction.MOVE;
                actionTimeout = 30 + (int)(w.rnd().nextFloat() * 90);
            }
        }
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.DAMAGED) {
            EDamaged evd = (EDamaged) ev;
            if(!aggro)
                scared = true;
            aggro = true;
            aggressorID = evd.src.sourceID();
            if(w.chance(5))
                refreshAction(w, e);
        }
        return false;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        actionTimeout = c.getI32("actionTimeout");
        action = EnumAction.values()[c.getI32("action")];
        scared = c.getBool("scared");
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        c.put("actionTimeout", actionTimeout);
        c.put("action", action.ordinal());
        c.put("scared", scared);
    }
    
}
