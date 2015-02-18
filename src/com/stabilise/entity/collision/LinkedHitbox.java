package com.stabilise.entity.collision;

import com.stabilise.entity.Entity;
import com.stabilise.util.shape.Shape;

/**
 * A LinkedHitbox is a hitbox which is linked to a specific entity other than
 * its owner (e.g., as in a projectile). When the hitbox is destroyed, its
 * linked entity is too.
 */
public class LinkedHitbox extends Hitbox {
	
	/** The entity which the hitbox is linked to - not necessarily its owner. */
	public Entity linkedEntity;
	
	
	/**
	 * Creates a new Hitbox.
	 * 
	 * @param owner The Hitbox's owner.
	 * @param boundingBox The Hitbox's bounding box.
	 * @param damage The damage the hitbox deals.
	 */
	public LinkedHitbox(Entity owner, Shape boundingBox, int damage) {
		super(owner, boundingBox, damage);
		
		// It is worth noting that ideally the linked Entity would be an extra
		// parameter for this constructor, such that for things like arrows, it
		// is possible to do (noting that that code may update, leaving this
		// example outdated):
		// public EntityArrow(World world, int owner) {
		//     super(world, owner, new LinkedHitbox(world, owner, ARROW_BOUNDING_BOX.clone(), this));
		// }
		// However, apparently one is unable to refer to 'this' within a
		// super(), so that's not possible, thus making this less elegant.
	}
	
	@Override
	protected void moveToOwner() {
		x = linkedEntity.x;
		y = linkedEntity.y;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		//linkedEntity.destroy(); // TODO
	}
	
}
