package com.stabilise.entity.component.state;

import java.util.Random;

import com.stabilise.core.Constants;
import com.stabilise.entity.Entity;
import com.stabilise.entity.component.ComponentEvent;
import com.stabilise.item.ItemStack;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;

@SuppressWarnings("unused")
public class CItem implements CState {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The number of ticks after which an item despawns. */
    private static final int DESPAWN_TICKS = 60*Constants.TICKS_PER_SECOND;
    
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
    
    private static final AABB ENT_AABB = new AABB(-0.4f, 0f, 0.8f, 0.8f);
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The item stack the entity represents. */
    public ItemStack stack;
    /** The number of the item the entity holds. */
    public int count;
    
    public CItem() {}
    public CItem(ItemStack stack) { this.stack = stack; }
    
    /**
     * Adds some velocity to the item to give it a nice popping effect.
     */
    public void pop(Entity e, Random rnd) {
        e.dx = rnd.nextFloat() * 4.0f - 2f;
        e.dy = 4f + rnd.nextFloat() * 2f;
    }
    
    @Override
    public void init(World w, Entity e) {
        
    }
    
    @Override
    public void update(World w, Entity e) {
        if(e.age == DESPAWN_TICKS)
            e.destroy();
        
        for(Entity m : w.getPlayers()) {
            /*
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
            */
        }
    }
    
    @Override
    public AABB getAABB() {
        return ENT_AABB;
    }
    
    @Override
    public void handle(World w, Entity e, ComponentEvent ev) {
        
    }
    
}
