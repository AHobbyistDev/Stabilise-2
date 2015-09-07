package com.stabilise.entity;

import java.util.Random;

import com.stabilise.core.Constants;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.item.ItemStack;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;

/**
 * An item entity.
 */
public class EntityItem extends Entity {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The number of ticks after which an item despawns. */
    private static final int DESPAWN_TICKS = 30*Constants.TICKS_PER_SECOND;
    
    /** The range from which a player may attract the item. */
    private static final float ATTRACTION_RANGE = 5.0f;
    /** The attraction range squared. */
    private static final float ATTRACTION_RANGE_SQUARED = ATTRACTION_RANGE * ATTRACTION_RANGE;
    /** The range from which a player may pick up the item. */
    private static final float PICKUP_RANGE = 0.5f;
    /** The pickup range squared. */
    private static final float PICKUP_RANGE_SQUARED = PICKUP_RANGE * PICKUP_RANGE;
    /** The speed at which items accelerate towards a player. */
    private static final float ATTRACTION_SPEED = 2.0f;
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The item stack the entity represents. */
    public ItemStack stack;
    /** The number of the item the entity holds. */
    public int count;
    
    
    public EntityItem() {
        
    }
    
    /**
     * Creates a new item entity.
     * 
     * @param stack The item the entity represents.
     */
    public EntityItem(ItemStack stack) {
        this.stack = stack;
    }
    
    /**
     * Adds some velocity to the item to give it a nice popping effect.
     */
    public void pop(Random rnd) {
        dx = rnd.nextFloat() * 4.0f - 2f;
        dy = 4f + rnd.nextFloat() * 2f;
    }
    
    protected AABB getAABB() {
        return new AABB(-0.4f, 0f, 0.8f, 0.8f);
    }
    
    @Override
    public void update(World world) {
        super.update(world);
        
        if(age == DESPAWN_TICKS)
            destroy();
        
        for(EntityMob m : world.getPlayers()) {
            if(!(m instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer)m;
            if(p.inventory.canAddStack(stack)) {
                float distX = (float) (x - p.x);
                float distY = (float) (y - p.y);
                float distSquared = distX*distX + distY*distY;
                if(distSquared == 0)
                    distSquared = 0.0001f;
                if(distSquared < PICKUP_RANGE_SQUARED) {
                    // TODO: player picks the item up
                    if(p.inventory.addStack(stack)) {
                        SingleplayerState.pop.play(1f, 1.2f, 0f);
                        destroy();
                        break;
                    }
                } else if(distSquared <= ATTRACTION_RANGE_SQUARED) {
                    if(distX > 0)
                        dx -= ATTRACTION_SPEED / distSquared;
                    else
                        dx += ATTRACTION_SPEED / distSquared;
                    if(distY > 0)
                        dy -= ATTRACTION_SPEED / distSquared;
                    else
                        dy += ATTRACTION_SPEED  / distSquared;
                    break;
                }
            }
        }
    }
    
    @Override
    public void render(WorldRenderer renderer) {
        renderer.renderItem(this);
    }
    
}
