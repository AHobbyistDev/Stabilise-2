package com.stabilise.entity.component.controller;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.ComponentEvent;
import com.stabilise.world.World;


public class EnemyController implements CController {
    
    /** Actions for the current, incredibly simplistic, AI. */
    private static enum EnumAction {
        IDLE, MOVE;
    };
    
    /** The number of ticks for which the enemy is to continue its current
     * action.*/
    private int actionTimeout = 1;
    /** The enemy's current action. */
    private EnumAction action = EnumAction.IDLE;
    
    @Override
    public void init(World w, Entity e) {}
    
    @Override
    public void update(World w, Entity e) {
        if(true/*!e.dead*/) {
            if(--actionTimeout == 0) {
                float rnd = w.getRnd().nextFloat();
                if(rnd < 0.45) {
                    action = EnumAction.IDLE;
                    actionTimeout = 180 + (int)(w.getRnd().nextFloat() * 180);
                } else if(rnd < 0.55) {
                    action = EnumAction.IDLE;
                    e.facingRight = (!e.facingRight);
                    actionTimeout = 120 + (int)(w.getRnd().nextFloat() * 180);
                } else if(rnd < 0.70) {
                    action = EnumAction.IDLE;
                    //if(e.onGround) e.dy = e.jumpVelocity;
                    actionTimeout = 180 + (int)(w.getRnd().nextFloat() * 180);
                } else {
                    if(rnd < 0.85) e.facingRight = (!e.facingRight);
                    action = EnumAction.MOVE;
                    actionTimeout = 30 + (int)(w.getRnd().nextFloat() * 90);
                }
            }
            
            if(action == EnumAction.MOVE) {
                /*
                if(e.facingRight)
                    accelerate(e.acceleration);
                else
                    accelerate(-e.acceleration);
                */
            }
        }
    }
    
    /**
     * Accelerates the mob.
     * 
     * @param ddx The base amount by which to modify the velocity.
     */
    @SuppressWarnings("unused")
    private void accelerate(Entity e, float ddx) {
        /*
        if(!mob.state.canMove)
            return;
        
        // Some sort of scaling
        ddx *= (mob.maxDx - Math.abs(mob.dx));
        
        // Note that for the purposes of acceleration the friction of a tile is
        // treated as its traction.
        
        if(e.physics.onGround())
            e.dx += ddx * Tile.getTile(e.floorTile).getFriction();
        else
            e.dx += ddx * mob.airAcceleration;
        */
    }
    
    @Override
    public void handle(World w, Entity e, ComponentEvent ev) {
        // meh
    }
    
}
