package com.stabilise.util.maths;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A mutable 2-dimensional point with integer components.
 */
@NotThreadSafe
public class Point {
	
	public int x, y;
	
	
	/**
	 * Creates a new point with x = 0 and y = 0.
	 */
	public Point() {
		x = 0;
		y = 0;
	}
	
	/**
	 * Creates a new point with the specified components.
	 */
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Creates a new point with components of those of the specified point.
	 */
	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	/**
	 * Sets the components of this point.
	 * 
	 * @return This point, for chaining operations.
	 */
	public Point set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}
	
	/**
	 * Sets the components of this point to those of the specified point.
	 * 
	 * @return This point, for chaining operations.
	 */
	public Point set(Point p) {
		x = p.x;
		y = p.y;
		return this;
	}
	
	@Override
	public int hashCode() {
		return x ^ y; // fairly basic hash
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Point)) return false;
		Point p = (Point)o;
		return x == p.x && y == p.y;
	}
	
	/**
	 * @return {@code true} if this Point holds the specified coordinates;
	 * {@code false} otherwise.
	 */
	public boolean equals(int x, int y) {
		return this.x == x && this.y == y;
	}
	
	@Override
	public String toString() {
		return "Point[" + x + "," + y + "]";
	}
	
}
