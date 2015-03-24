package com.stabilise.util.shape;

import com.stabilise.util.maths.Vec2;

/**
 * An unifying interface provided for both AABB implementations, intended to
 * allow for uniform optimisation.
 */
public interface AABB {
	
	/**
	 * Gets this AABB's bottom-left vertex.
	 */
	Vec2 getV00();
	
	/**
	 * Gets this AABB's top-right vertex.
	 */
	Vec2 getV11();
	
}
