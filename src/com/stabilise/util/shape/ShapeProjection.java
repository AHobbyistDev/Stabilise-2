package com.stabilise.util.shape;

/**
 * A shape projection represents the 1D 'shadow' cast by a shape when it is
 * projected onto an axis.
 */
class ShapeProjection {
	
	/** The minimum and maximum values/coordinates of the projection. */
	public final float min, max;
	
	
	/**
	 * Creates a new ShapeProjection.
	 * 
	 * @param min The projection's minimum value.
	 * @param max The projection's maximum value.
	 */
	public ShapeProjection(float min, float max) {
		this.min = min;
		this.max = max;
	}
	
	/**
	 * @param p The projection to compare against.
	 * 
	 * @return {@code true} if the two projections overlap; {@code false}
	 * if they do not.
	 * @throws NullPointerException if {@code p} is {@code null}.
	 */
	public boolean intersects(ShapeProjection p) {
		return min <= p.max && max >= p.min;
	}
	
	/**
	 * @param p The projection to compare against.
	 * 
	 * @return {@code true} if this projection contains the other; {@code
	 * false} otherwise.
	 * @throws NullPointerException if {@code p} is {@code null}.
	 */
	public boolean contains(ShapeProjection p) {
		return min <= p.min && max >= p.max;
	}
	
	/**
	 * @param x The point.
	 * 
	 * @return {@code true} if the projection contains the point; {@code false}
	 * if it doesn't.
	 */
	public boolean containsPoint(float x) {
		return x >= min && x <= max;
	}

}
