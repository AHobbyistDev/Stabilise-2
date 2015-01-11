package com.stabilise.util;

/**
 * The Direction enum allows for the representation of all four cardinal
 * directions and compound directions.
 */
public enum Direction {
	
	UP((byte)(Bit.VERTICAL_BIT | Bit.UP_BIT)),			// 0101
	DOWN(Bit.VERTICAL_BIT),								// 0001
	RIGHT((byte)(Bit.HORIZONTAL_BIT | Bit.RIGHT_BIT)),	// 1010
	LEFT(Bit.HORIZONTAL_BIT),							// 0010
	UP_LEFT(UP, LEFT),									// 0111
	UP_RIGHT(UP, RIGHT),								// 1111
	DOWN_LEFT(DOWN, LEFT),								// 0011
	DOWN_RIGHT(DOWN, RIGHT);							// 1011
	
	/** The direction's identifying value.
	 * The first (least significant) bit determines whether or not it has a
	 * vertical value. The second bit determines the vertical value if it has
	 * one; 1 = up, 0 = down.
	 * The third bit determines whether or not it has a horizontal value. 
	 * The fourth bit determines the horizontal value if it has one; 1 = right,
	 * 0 = left. */
	private final byte bitmask;
	
	/** Whether or not the Direction has a vertical component. */
	private final boolean verticalComponent;
	/** Whether or not the Direction has a horizontal component. */
	private final boolean horizontalComponent;
	/** Whether or not the Direction has an up component. */
	private final boolean upComponent;
	/** Whether or not the Direction has a right component. */
	private final boolean rightComponent;
	
	// Convenience values
	/** Whether or not the Direction has a down component. */
	private final boolean downComponent;
	/** Whether or not the Direction has a left component. */
	private final boolean leftComponent;
	
	
	/**
	 * Creates a new Direction that is a combination of two other Directions.
	 * 
	 * @param d1 The first Direction.
	 * @param d2 The second Direction.
	 */
	private Direction(Direction d1, Direction d2) {
		this((byte)(d1.bitmask | d2.bitmask));
	}
	
	/**
	 * Creates a new Direction.
	 * 
	 * @param bitmask The Direction's bitmask.
	 */
	private Direction(byte bitmask) {
		this.bitmask = bitmask;
		
		// In preference to constantly recalculating all these later
		
		verticalComponent = (bitmask & Bit.VERTICAL_BIT) == Bit.VERTICAL_BIT;
		horizontalComponent = (bitmask & Bit.HORIZONTAL_BIT) == Bit.HORIZONTAL_BIT;
		
		// Checking just to make sure; if a component bit is not present, make
		// sure the component value bit is also not present
		if(!verticalComponent)
			bitmask &= ~Bit.UP_BIT;
		if(!horizontalComponent)
			bitmask &= ~Bit.RIGHT_BIT;
		
		upComponent = (bitmask & Bit.UP_BIT) == Bit.UP_BIT;
		rightComponent = (bitmask & Bit.RIGHT_BIT) == Bit.RIGHT_BIT;
		
		downComponent = verticalComponent && !upComponent;
		leftComponent = horizontalComponent && !rightComponent;
	}
	
	
	/**
	 * Checks for whether or not this direction composes a part of the given
	 * direction. For example, {@code RIGHT.isPartOf(TOP_RIGHT)} returns {@code
	 * true}, while {@code RIGHT.isPartOf(TOP_LEFT)} returns {@code false}.
	 * 
	 * @param d The direction to check.
	 * 
	 * @return {@code true} if this direction is a component of the given
	 * direction; {@code false} otherwise.
	 */
	public boolean isPartOf(Direction d) {
		return (bitmask & d.bitmask) == bitmask;
	}
	
	/**
	 * Checks for whether or not the Direction has a vertical component.
	 * 
	 * @return {@code true} if the Direction has a vertical component;
	 * {@code false} otherwise.
	 */
	public boolean hasVerticalComponent() {
		return verticalComponent;
	}
	
	/**
	 * Checks for whether or not the Direction has a horizontal component.
	 * 
	 * @return {@code true} if the Direction has a horizontal component;
	 * {@code false} otherwise.
	 */
	public boolean hasHorizontalComponent() {
		return horizontalComponent;
	}
	
	/**
	 * Checks for whether or not the Direction has an up component.
	 * 
	 * @return {@code true} if the Direction has an up component; {@code false}
	 * otherwise.
	 */
	public boolean hasUp() {
		return upComponent;
	}
	
	/**
	 * Checks for whether or not the Direction has a down component.
	 * 
	 * @return {@code true} if the Direction has a down component;
	 * {@code false} otherwise.
	 */
	public boolean hasDown() {
		return downComponent;
	}
	
	/**
	 * Checks for whether or not the Direction has a right component.
	 * 
	 * @return {@code true} if the Direction has a right component;
	 * {@code false} otherwise.
	 */
	public boolean hasRight() {
		return rightComponent;
	}
	
	/**
	 * Checks for whether or not the Direction has a left component.
	 * 
	 * @return {@code true} if the Direction has a left component;
	 * {@code false} otherwise.
	 */
	public boolean hasLeft() {
		return leftComponent;
	}
	
	
	// N.B. Statics are being put in a nested class because otherwise Java complains
	// (Statics can not be defined before enum values, and are considered undefined
	// for those values if they are defined after them.)
	private static class Bit {
		/** The bit indicating a Direction contains a vertical component. */
		private static final byte VERTICAL_BIT = 1;
		/** The bit indicating a Direction contains a horizontal component. */
		private static final byte HORIZONTAL_BIT = 2;
		/** The bit indicating whether or not a Direction has an up component
		 * (down otherwise). */
		private static final byte UP_BIT = 4;
		/** The bit indicating whether or not a Direction has a right component
		 * (left otherwise). */
		private static final byte RIGHT_BIT = 8;
	}
	
}
