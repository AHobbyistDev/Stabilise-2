package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;

/**
 * An unifying interface provided for both AABB implementations, intended to
 * allow for uniform optimisation.
 */
public interface AABB {
	
	/**
	 * Gets the AABB's min (i.e. bottom-left) vertex.
	 * 
	 * @return The AABB's min vertex.
	 */
	Vector2 getV00();
	
	/**
	 * Gets the AABB's max (i.e. top-right) vertex.
	 * 
	 * @return The AABB's max vertex.
	 */
	Vector2 getV11();
	
}
