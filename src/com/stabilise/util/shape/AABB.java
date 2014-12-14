package com.stabilise.util.shape;

import org.lwjgl.util.vector.Vector2f;

/**
 * An unifying interface provided for both AABB implementations, intended to
 * allow for uniform optimisation.
 */
interface AABB {
	
	/**
	 * Gets the AABB's min (i.e. bottom-left) vertex.
	 * 
	 * @return The AABB's min vertex.
	 */
	Vector2f getV00();
	
	/**
	 * Gets the AABB's max (i.e. top-right) vertex.
	 * 
	 * @return The AABB's max vertex.
	 */
	Vector2f getV11();
	
}
