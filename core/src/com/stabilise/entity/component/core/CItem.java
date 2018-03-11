package com.stabilise.entity.component.core;

import java.util.Random;

import com.stabilise.core.Constants;
import com.stabilise.core.state.SingleplayerState;
import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.item.IContainer;
import com.stabilise.item.ItemStack;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;

public class CItem extends CCore {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The number of ticks after which an item despawns. */
    private static final int DESPAWN_TICKS = 5*60*Constants.TICKS_PER_SECOND;
    
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
    
    private static final double MERGE_RANGE_SQ = 2*2;
    
    private static final AABB ENT_AABB = new AABB(-0.4f, 0f, 0.8f, 0.8f);
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** The item stack the entity represents. */
    public ItemStack stack;
    
    
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
    public void init(Entity e) {
        
    }
    
    @Override
    public void update(World w, Entity e) {
        if(e.age == DESPAWN_TICKS)
            e.destroy();
        
        if(e.age == 60 || e.age == 120 || e.age == 600) {
            for(Entity o : w.getEntities()) {
                if(o.core instanceof CItem && tryMerge(e, o, (CItem)o.core))
                    return;
            }
        }
        
        for(Entity p : w.getPlayers()) {
            if(!(p.core instanceof IContainer)) continue;
            IContainer c = (IContainer)p.core;
            if(c.canAddStack(stack)) {
                float distX = p.pos.diffX(e.pos);
                float distY = p.pos.diffY(e.pos);
                float distSquared = distX*distX + distY*distY;
                if(distSquared == 0)
                    distSquared = 0.0001f;
                if(distSquared < PICKUP_RANGE_SQUARED) {
                    // TODO: player picks the item up
                    if(c.addStack(stack)) {
                        SingleplayerState.pop.play(1f, 1.2f, 0f);
                        e.destroy();
                        break;
                    }
                } else if(distSquared <= ATTRACTION_RANGE_SQUARED) {
                    if(distX > 0)
                        e.dx -= ATTRACTION_SPEED / distSquared;
                    else
                        e.dx += ATTRACTION_SPEED / distSquared;
                    if(distY > 0)
                        e.dy -= ATTRACTION_SPEED / distSquared;
                    else
                        e.dy += ATTRACTION_SPEED  / distSquared;
                    break;
                }
            }
        }
    }
    
    private boolean tryMerge(Entity e, Entity o, CItem c) {
        // Don't add to a destroyed stack or self
        if(o.isDestroyed() || o == e) return false;
        
        float dx = o.pos.diffX(e.pos); // e.x - o.x;
        float dy = o.pos.diffY(e.pos); // e.y - o.y;
        if(dx*dx + dy*dy <= MERGE_RANGE_SQ) {
            if(c.stack.add(stack)) {
                o.age = 0; // reset the other's timer
                e.destroy();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void render(WorldRenderer renderer, Entity e) {
        renderer.renderItem(e, this);
    }
    
    @Override
    public AABB getAABB() {
        return ENT_AABB;
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type() == EntityEvent.Type.ADDED_TO_WORLD)
            pop(e, w.rnd());
        return false;
    }
    
}
