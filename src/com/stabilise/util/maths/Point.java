package com.stabilise.util.maths;

import com.stabilise.util.annotation.Immutable;

/**
 * A Point is a point, which, unlike {@link MutablePoint}, is immutable, and
 * precomputes its hash code upon construction.
 * 
 * <p>Point objects are compatible with MutablePoint objects as map keys: e.g.,
 * {@code new Point(-5, 2).equals(new MutablePoint(-5, 2)) == true}.
 * 
 * @see MutablePoint
 */
@Immutable
public class Point extends AbstractPoint {
	
	public final int x, y;
	private final int hash;
	
	
	/**
	 * Creates a point with x = 0 and y = 0.
	 */
	public Point() {
		this(0, 0);
	}
	
	/**
	 * Creates a point with the specified components.
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		hash = genHash();
	}
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public Point set(int x, int y) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("A Point is immutable!");
	}
	
}
