package com.stabilise.util.maths;

import com.stabilise.util.annotation.Immutable;

/**
 * An immutable 2D vector.
 */
@Immutable
public class Vec2 {
	
	public final float x, y;
	
	
	/**
	 * Creates the zero vector.
	 */
	public Vec2() {
		x = y = 0f;
	}
	
	/**
	 * Creates a new vector.
	 */
	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * @return The dot product of this vector with another.
	 * @throws NullPointerException if {@code v} is {@code null}.
	 */
	public float dot(Vec2 v) {
		return x * v.x + y * v.y;
	}
	
	/**
	 * @return The dot product of this vector with the specified vector
	 * components.
	 */
	public float dot(float x, float y) {
		return this.x * x + this.y * y;
	}
	
	/**
	 * Subtracts another vector from this vector and returns the resulting
	 * vector.
	 * 
	 * @return this - v
	 */
	public Vec2 sub(Vec2 v) {
		return new Vec2(x - v.x, y - v.y);
	}
	
	/**
	 * Rotates this vector anticlockwise by the specified angle.
	 * 
	 * @param radians The angle, in radians.
	 * 
	 * @return The new rotated vector.
	 */
	public Vec2 rotate(float radians) {
		return rotate((float)Math.cos(radians), (float)Math.sin(radians));
	}
	
	/**
	 * Rotates this vector.
	 * 
	 * <p>This method is faster than {@link #rotate(float)}, as values for cos
	 * and sine - obviously - do not need to be computed.
	 * 
	 * @param cos The cosine of the angle by which to rotate this vector.
	 * @param sin The sine of the angle by which to rotate this vector.
	 * 
	 * @return The new rotated vector.
	 */
	public Vec2 rotate(float cos, float sin) {
		return new Vec2(
				x * cos - y * sin,
				x * sin + y * cos
		);
	}
	
	/**
	 * Rotates this vector 90 degrees anticlockwise and returns the result.
	 */
	public Vec2 rotate90Degrees() {
		return new Vec2(-y, x);
	}
	
	@Override
	public int hashCode() {
		return Float.floatToRawIntBits(x) ^ Float.floatToRawIntBits(y);
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Vec2)) return false;
		Vec2 v = (Vec2)o;
		return x == v.x && y == v.y;
	}
	
	@Override
	public String toString() {
		return "Vec2[" + x + "," + y + "]";
	}
	
}
