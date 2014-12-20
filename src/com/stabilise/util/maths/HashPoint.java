package com.stabilise.util.maths;

/**
 * A HashPoint is an optionally immutable point with a decently distributed
 * hashcode which may as such be used to compress two integers into a single
 * Map key more efficiently than an ordinary Point.
 * 
 * <p>A HashPoint precomputes its hash code whenever it is modified.
 * 
 * @see Point
 */
public class HashPoint {
	
	private int x, y;
	private int hash;
	private final boolean mutable; // somewhat ironic
	
	
	/**
	 * Creates a mutable point with x = 0 and y = 0.
	 */
	public HashPoint() {
		this(0, 0, true);
	}
	
	/**
	 * Creates mutable point with the specified components.
	 */
	public HashPoint(int x, int y) {
		this(x, y, true);
	}
	
	/**
	 * Creates a point with x = 0 and y = 0.
	 * 
	 * @param mutable Whether or not this point's components may be modified.
	 */
	public HashPoint(boolean mutable) {
		this(0, 0, mutable);
	}
	
	/**
	 * Creates a point.
	 * 
	 * @param x The point's x component.
	 * @param y The point's y component.
	 * @param mutable Whether or not this point's components may be modified.
	 */
	public HashPoint(int x, int y, boolean mutable) {
		this.x = x;
		this.y = y;
		this.mutable = mutable;
		genHash();
	}
	
	/**
	 * Sets the components of this point.
	 * 
	 * @return This point, for chaining operations.
	 * 
	 * @throws IllegalStateException if this point is immutable.
	 */
	public HashPoint set(int x, int y) {
		checkCanModify();
		this.x = x;
		this.y = y;
		genHash();
		return this;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	/**
	 * @throws IllegalStateException if this point is immutable.
	 */
	public void setX(int x) {
		checkCanModify();
		this.x = x;
		genHash();
	}
	
	/**
	 * @throws IllegalStateException if this point is immutable.
	 */
	public void setY(int y) {
		checkCanModify();
		this.y = y;
		genHash();
	}
	
	/**
	 * Checks for whether or not this point is mutable.
	 * 
	 * @return {@code true} if this point is mutable; {@code false} if it is
	 * immutable.
	 */
	public boolean isMutable() {
		return mutable;
	}
	
	/**
	 * @throws IllegalStateException if this point is immutable.
	 */
	private void checkCanModify() {
		if(!mutable)
			throw new IllegalStateException("This HashPoint is immutable");
	}
	
	private void genHash() {
		// This has too many basic collisions
		//hash = x ^ y;
		// This loses information
		//hash = ((x & 0xFFFF) << 16) + (y & 0xFFFF);
		
		// Shifts y by 16 bits modularly
		hash = x ^ (y << 16) ^ (y >>> 16);
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(!(o instanceof HashPoint)) return false;
		HashPoint p = (HashPoint)o;
		return x == p.x && y == p.y;
	}
	
	@Override
	public String toString() {
		return "HashPoint[" + x + "," + y + "]";
	}
	
}
