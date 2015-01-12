package com.stabilise.entity;

import com.stabilise.entity.collision.LinkedHitbox;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.shape.AxisAlignedBoundingBox;
import com.stabilise.util.shape.Rectangle;
import com.stabilise.world.BaseWorld;

/**
 * A basic projectile.
 */
public class EntityArrow extends EntityProjectile {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The arrow hitbox template. */
	private static final Rectangle ARROW_BOUNDING_BOX = new Rectangle(-0.005f, -0.005f, 0.01f, 0.01f);
	/** Default arrow damage. */
	private static final int DEFAULT_ARROW_DAMAGE = 10;
	
	/** The number of ticks after which an arrow despawns. */
	private static final int DESPAWN_TICKS = 3600;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** Whether or not the arrow is currently flying. */
	private boolean flying = true;
	
	/** A flag which indicates whether or not the arrow's hitbox is to be removed. */
	private boolean removeHitbox = false;
	
	
	/**
	 * Creates a new arrow entity.
	 * 
	 * @param world The world in which the arrow will be placed.
	 * @param owner The arrow's owner.
	 */
	public EntityArrow(BaseWorld world, Entity owner) {
		this(world, owner, DEFAULT_ARROW_DAMAGE);
	}
	
	/**
	 * Creates a new arrow entity.
	 * 
	 * @param world The world in which the arrow will be placed.
	 * @param owner The arrow's owner.
	 * @param damage The arrow's damage.
	 */
	public EntityArrow(BaseWorld world, Entity owner, int damage) {
		super(world, owner, new LinkedHitbox(world, owner, ARROW_BOUNDING_BOX, damage));
		((LinkedHitbox)hitbox).linkedEntity = this;
		hitbox.force = 0.3f;
	}
	
	@Override
	protected AxisAlignedBoundingBox getAABB() {
		return new AxisAlignedBoundingBox(-0.005f, -0.005f, 0.01f, 0.01f);
	}
	
	@Override
	public void update() {
		if(removeHitbox && hitbox != null) {
			hitbox.destroy();
			hitbox = null;
			removeHitbox = false;
		}
		
		super.update();
		
		if(flying) {
			float div = Math.abs(dx) + Math.abs(dy);
			if(div != 0) {
				hitbox.fx = dx / div;
				hitbox.fy = dy / div;
			}
		}
		
		if(age == DESPAWN_TICKS)
			destroy();
	}
	
	@Override
	protected void rotate() {
		if(flying)
			super.rotate();
	}
	
	@Override
	protected float getXFriction() {
		return flying ? super.getXFriction() : 1;
	}
	
	@Override
	protected float getYFriction() {
		return flying ? super.getYFriction() : 1;
	}
	
	@Override
	protected void impact(float dv, boolean tileCollision) {
		flying = false;
		// If an arrow hits an entity in the same tick it collides with a tile,
		// removing the hitbox is postponed a tick so that it may first damage
		// the entity it hits
		removeHitbox = true;
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		// no rendering for arrows...
	}

}
