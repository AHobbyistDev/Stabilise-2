package com.stabilise.util;

import com.stabilise.core.Constants;

/**
 * This class contains a number of static methods used to determine physical
 * values for entities, namely through use of Newton's equations of motion.
 */
class PhysicsUtil {

	private PhysicsUtil() {
		// non-instantiable
	}
	
	/**
	 * Converts a per-second value to its per-tick value.
	 * 
	 * @param v The value.
	 * 
	 * @return The value divided by the number of game ticks each second.
	 * @see Constants#TICKS_PER_SECOND
	 */
	public static float perSecondToPerTick(float v) {
		return v / Constants.TICKS_PER_SECOND;
	}
	
	/**
	 * Gets the initial velocity required for a jump.
	 * 
	 * @param s The vertical displacement at the apex of the jump.
	 * @param g The value for gravity.
	 * 
	 * @return The initial velocity for the jump.
	 */
	public static float jumpHeightToInitialJumpVelocity(float s, float g) {
		// v^2 = u^2 + 2as
		// u^2 = -2as
		// u   = sqrt(-2as), which works as a is negative
		return (float)Math.sqrt(-2 * g * s);
	}
	
	/**
	 * Gets the modified value for gravity for an extended jump.
	 * 
	 * @param u The initial velocity of a jump.
	 * @param s The vertical displacement at the apex of the jump.
	 *  
	 * @return The value for gravity during an extended jump.
	 */
	public static float getGravityDuringExtendedJump(float u, float s) {
		// u^2 = -2as
		// a   = -u^2/2s
		return -(u*u/(2*s));
	}

}
