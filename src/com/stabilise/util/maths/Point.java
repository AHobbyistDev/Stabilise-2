package com.stabilise.util.maths;

/**
 * A mutable 2-dimensional point with integer components.
 */
public class Point {
	
	public int x, y;
	
	
	/**
	 * Creates a new point with x and y components 0.
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
	 * Sets the components of this point.
	 * 
	 * @return This point, for chaining operations.
	 */
	public Point set(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}

}
