package com.stabilise.entity;

import com.stabilise.item.ItemStack;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.IWorld;

/**
 * An item entity.
 */
public class EntityItem extends Entity {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of ticks after which an item despawns. */
	private static final int DESPAWN_TICKS = 3600;
	
	/** The range from which a player may attract the item. */
	private static final float ATTRACTION_RANGE = 5.0f;
	/** The attraction range squared. */
	@SuppressWarnings("unused")
	private static final float ATTRACTION_RANGE_SQUARED = ATTRACTION_RANGE * ATTRACTION_RANGE;
	/** The range from which a player may pick up the item. */
	private static final float PICKUP_RANGE = 0.5f;
	/** The pickup range squared. */
	@SuppressWarnings("unused")
	private static final float PICKUP_RANGE_SQUARED = PICKUP_RANGE * PICKUP_RANGE;
	/** The speed at which items accelerate towards a player. */
	@SuppressWarnings("unused")
	private static final float ATTRACTION_SPEED = 0.05f;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The item stack the entity represents. */
	public ItemStack stack;
	/** The number of the item the entity holds. */
	public int count;
	
	
	/**
	 * Creates a new item entity.
	 * 
	 * @param world The world in which the entity will be placed.
	 * @param stack The item the entity represents.
	 */
	public EntityItem(IWorld world, ItemStack stack) {
		super(world);
		
		this.stack = stack;
	}
	
	@Override
	public void update() {
		super.update();
		
		if(age == DESPAWN_TICKS)
			destroy();
		
		// Note: Won't work if uncommented.
		/*
		for(EntityMob m : world.players.values()) {
			if(!(m instanceof EntityPlayer)) continue;
			EntityPlayer p = (EntityPlayer)m;
			if(p.inventory.canAddItem(item)) {
				float distX = (float) (x - p.x);
				float distY = (float) (y - p.y);
				float distSquared = distX*distX + distY*distY;
				if(distSquared < PICKUP_RANGE_SQUARED) {
					// TODO: player picks the item up
					count = p.inventory.addItem(item, count);
					if(count == 0) {
						destroyed = true;
						world.removeEntity(id);
						break;
					}
				} else if(distSquared <= ATTRACTION_RANGE_SQUARED) {
					if(distX > 0) {
						//dx -= ATTRACTION_SPEED / (ATTRACTION_RANGE - distX)*(ATTRACTION_RANGE - distX);
						dx -= ATTRACTION_SPEED;
					} else {
						//dx += ATTRACTION_SPEED / (ATTRACTION_RANGE + distX)*(ATTRACTION_RANGE + distX);
						dx += ATTRACTION_SPEED;
					}
					if(distY > 0) {
						//dy -= ATTRACTION_SPEED / (ATTRACTION_RANGE - distY)*(ATTRACTION_RANGE - distY);
						dy -= ATTRACTION_SPEED;
					} else {
						//dy += ATTRACTION_SPEED / (ATTRACTION_RANGE + distY)*(ATTRACTION_RANGE + distY);
						dy += ATTRACTION_SPEED;
					}
					break;
				}
			}
		}
		*/
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		renderer.renderItem(this);
	}
	
}
