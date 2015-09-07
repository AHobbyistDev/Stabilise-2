package com.stabilise.entity;

import com.stabilise.entity.controller.IdleController;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Direction;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;
import com.stabilise.world.tile.Tile;

/**
 * A generic test enemy.
 */
public class EntityEnemy extends EntityMob {
    
    /** Actions for the current, incredibly simplistic, AI. */
    private static enum EnumAction {
        IDLE, MOVE;
    };
    
    /** The AABB for enemy entities. */
    private static final AABB ENEMY_AABB =
            new AABB(-0.5f, 0, 1, 2);
    
    /** The number of ticks for which the enemy is to continue its current
     * action.*/
    private int actionTimeout = 1;
    /** The enemy's current action. */
    private EnumAction action = EnumAction.IDLE;
    
    
    @Override
    protected AABB getAABB() {
        return ENEMY_AABB;
    }
    
    @Override
    protected void initProperties() {
        // Temporary initial value setting
        maxHealth = 20;
        health = 20;
        
        jumpVelocity = 15f;
        jumpCrouchDuration = 8;
        //jumpVelocity = PhysicsUtil.jumpHeightToInitialJumpVelocity(4, gravity);
        swimAcceleration = 0.08f;
        acceleration = 1.3f;
        airAcceleration = AIR_TRACTION;
        maxDx = 13f;
        
        state = State.IDLE;
        
        setController(IdleController.INSTANCE);
    }
    
    @Override
    public void onAdd() {
        //world.hostileMobCount++;
    }
    
    @Override
    public void update(World world) {
        if(!dead) {
            if(--actionTimeout == 0) {
                float rnd = world.getRnd().nextFloat();
                if(rnd < 0.45) {
                    action = EnumAction.IDLE;
                    actionTimeout = 180 + (int)(world.getRnd().nextFloat() * 180);
                } else if(rnd < 0.55) {
                    action = EnumAction.IDLE;
                    setFacingRight(!facingRight);
                    actionTimeout = 120 + (int)(world.getRnd().nextFloat() * 180);
                } else if(rnd < 0.70) {
                    action = EnumAction.IDLE;
                    if(onGround) dy = jumpVelocity;
                    actionTimeout = 180 + (int)(world.getRnd().nextFloat() * 180);
                } else {
                    if(rnd < 0.85) setFacingRight(!facingRight);
                    action = EnumAction.MOVE;
                    actionTimeout = 30 + (int)(world.getRnd().nextFloat() * 90);
                }
            }
            
            if(action == EnumAction.MOVE) {
                if(facingRight)
                    accelerate(acceleration);
                else
                    accelerate(-acceleration);
            }
        }
        
        super.update(world);
    }
    
    /**
     * Accelerates the mob.
     * 
     * @param ddx The base amount by which to modify the velocity.
     */
    private void accelerate(float ddx) {
        if(!state.canMove)
            return;
        
        // Some sort of scaling
        ddx *= (maxDx - Math.abs(dx));
        
        // Note that for the purposes of acceleration the friction of a tile is
        // treated as its traction.
        if(onGround)
            dx += ddx * Tile.getTile(floorTile).getFriction();
        else
            dx += ddx * airAcceleration;
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderEnemy(this);
    }

    @Override
    public void kill() {
        super.kill();
        
        /*
        dropItem(1, 1, 0.75f);        // sword
        dropItem(2, 1, 0.75f);        // apple
        dropItem(3, 1, 0.75f);        // arrow
        */
    }
    
    @Override
    public void destroy() {
        super.destroy();
        //world.hostileMobCount--;
    }
    
    @Override
    public void attack(Direction direction) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public void specialAttack(Direction direction) {
        // TODO Auto-generated method stub
    }
    
}
