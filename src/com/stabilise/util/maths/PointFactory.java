package com.stabilise.util.maths;

import java.util.Objects;

import com.stabilise.util.BiIntFunction;
import com.stabilise.util.annotation.Immutable;
import com.stabilise.util.annotation.ThreadSafe;

/**
 * A {@code PointFactory} produces {@code Point} and {@code MutablePoint}
 * objects which use a specified hash function to generate their hash codes
 * instead of the default hash function.
 * 
 * <p>Note that points created by a PointFactory may not necessarily produce
 * the same hash codes as points instantiated normally, or those produced by a
 * different PointFactory, so it is a very bad idea to intermix these different
 * 'breeds' of points.
 */
@Immutable
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
	 * Creates a new Point with the specified components.
	 */
	@ThreadSafe
	public Point newPoint(int x, int y) {
		return new OwnedPoint(x, y);
	}
	
	/**
	 * Creates a new MutablePoint with the specified components.
	 */
	@ThreadSafe
	public MutablePoint newMutablePoint(int x, int y) {
		return new OwnedMutablePoint(x, y);
	}
	
	/**
	 * Creates a new MutablePoint with components (0,0).
	 */
	@ThreadSafe
	public MutablePoint newMutablePoint() {
		return newMutablePoint(0, 0);
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
