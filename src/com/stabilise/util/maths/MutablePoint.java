package com.stabilise.util.maths;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A mutable 2-dimensional point with integer components.
 * 
 * <p>MutablePoint objects are compatible with Point objects as map keys: e.g.,
 * {@code new MutablePoint(-5, 2).equals(new Point(-5, 2)) == true}.
 */
@NotThreadSafe
public class MutablePoint extends AbstractPoint {
	
	public int x, y;
	
	
	/**
	 * Creates a new point with x = 0 and y = 0.
	 */
	public MutablePoint() {
		x = 0;
		y = 0;
	}
	
	/**
	 * Creates a new point with the specified components.
	 */
	public MutablePoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Creates a new point with components of those of the specified point.
	 */
	public MutablePoint(MutablePoint p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	/**
	 * Sets the components of this point.
	 * 
	 * @return This point, for chaining operations.
	 */
	public MutablePoint set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	/**
	 * Sets the components of this point to those of the specified point.
	 * 
	 * @return This point, for chaining operations.
	 */
	public MutablePoint set(MutablePoint p) {
		x = p.x;
		y = p.y;
		return this;
	}
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	/**
	 * @return {@code true} if this MutablePoint holds the specified coordinates;
	 * {@code false} otherwise.
	 */
	public boolean equals(int x, int y) {
		return this.x == x && this.y == y;
	}
	
}
