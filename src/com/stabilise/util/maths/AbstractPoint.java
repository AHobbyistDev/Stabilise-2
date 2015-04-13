package com.stabilise.util.maths;

/**
 * Unifying interface for different point implementations.
 * 
 * <p>An AbstractPoint uses {@link #genHash()} to create its hash code.
 */
public abstract class AbstractPoint {
	
	/**
	 * Returns the x component of this point.
	 */
	public abstract int getX();
	
	/**
	 * Returns the y component of this point.
	 */
	public abstract int getY();
	
	/**
	 * Sets the components of this point, and returns this point.
	 * 
	 * @throws UnsupportedOperationException if this point is immutable.
	 */
	public abstract AbstractPoint set(int x, int y);
	
	/**
	 * Generates and returns this point's hash code.
s	 */
	protected int genHash() {
		// This has too many basic collisions
		//hash = x ^ y;
		
		// Collisions are nicely distributed this way (though there's collision
		// clumping nearby (0,0) as there's more or less mirroring about (0,0))
		//hash = x ^ (y << 16) ^ (y >>> 16); // Cyclicly shift y by 16 bits
		
		// This eliminates higher-order bits, and as such is susceptible to
		// collisions between two points (x0, y0) and (x1, y1) when:
		// Maths.wrappedRem(x0, 65536) == Maths.wrappedRem(x1, 65536) &&
		// Maths.wrappedRem(y0, 65536) == Maths.wrappedRem(y1, 65536)
		// I feel this is the best option for collision distribution since
		// nearby points shouldn't have hash collisions at all.
		return (getX() << 16) | (getY() & 0xFFFF);
	}
	
	@Override
	public int hashCode() {
		return genHash();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof AbstractPoint)) return false;
		AbstractPoint p = (AbstractPoint)o;
		return getX() == p.getX() && getY() == p.getY();
	}
	
	/**
	 * @return {@code true} if this point holds the specified coordinates;
	 * {@code false} otherwise.
	 */
	public boolean equals(int x, int y) {
		return getX() == x && getY() == y;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getX() + "," + getY() + "]";
	}
	
}
