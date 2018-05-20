package com.stabilise.entity.event;

/**
 * This event is posted to an entity when it switches dimensions, <em>after
 * </em> the switching has been effected (i.e. after the components of the
 * original and the phantom have been swapped).
 */
public class EDimensionChange extends EntityEvent {
	
	public EDimensionChange() {
		super(Type.DIMENSION_CHANGE);
	}
	
}
