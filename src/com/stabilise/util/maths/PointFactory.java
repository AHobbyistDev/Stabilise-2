package com.stabilise.util.maths;

import java.util.Objects;

import com.stabilise.util.BiIntFunction;
import com.stabilise.util.annotation.ThreadSafe;

/**
 * A {@code PointFactory} produces {@code Point} and {@code MutablePoint}
 * objects which use a specified hash function to generate their hash codes.
 */
@ThreadSafe
public class PointFactory {
	
	final BiIntFunction hasher;
	
	
	/**
	 * Creates a new point factory.
	 * 
	 * @param hasher The function with which to produce a point's hash code.
	 * 
	 * @throws NullPointerException if {@code hasher} is {@code null}.
	 */
	public PointFactory(BiIntFunction hasher) {
		this.hasher = Objects.requireNonNull(hasher);
	}
	
	/**
	 * Creates a new Point, as if by {@link Point#Point(int, int) new
	 * Point(x, y)}.
	 */
	public Point newPoint(int x, int y) {
		return new OwnedPoint(x, y);
	}
	
	/**
	 * Creates a new MutablePoint, as if by {@link
	 * MutablePoint#MutablePoint(int, int) new MutablePoint(x, y)}.
	 */
	public MutablePoint newMutablePoint(int x, int y) {
		return new OwnedMutablePoint(x, y);
	}
	
	// Nested classes ---------------------------------------------------------
	
	private class OwnedPoint extends Point {
		private OwnedPoint(int x, int y) { super(x,y); }
		@Override protected int genHash() { return hasher.apply(x, y); }
	}
	
	private class OwnedMutablePoint extends MutablePoint {
		private OwnedMutablePoint(int x, int y) { super(x,y); }
		@Override protected int genHash() { return hasher.apply(x, y); }
	}

}
