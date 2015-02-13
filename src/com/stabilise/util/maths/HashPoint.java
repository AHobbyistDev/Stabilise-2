package com.stabilise.util.maths;

import com.stabilise.util.annotation.Immutable;

/**
 * A HashPoint is an immutable point with a decently distributed precomputed
 * hashcode which may as such be used to compress two integers into a single
 * Map key both more effectively and more efficiently than an ordinary Point.
 * 
 * @see Point
 */
@Immutable
public class HashPoint {
	
	public final int x, y;
	private final int hash;
	
	/**
	 * Creates a point with x = 0 and y = 0.
	 */
	public HashPoint() {
		this(0, 0);
	}
	
	/**
	 * Creates a point with the specified components.
	 */
	public HashPoint(int x, int y) {
		this.x = x;
		this.y = y;
		
		// This has too many basic collisions
		//hash = x ^ y;
		// This loses information
		//hash = ((x & 0xFFFF) << 16) + (y & 0xFFFF);
		
		// Collisions are nicely distributed this way (though there's collision
		// clumping nearby (0,0) as there's more or less mirroring about (0,0))
		hash = x ^ (y << 16) ^ (y >>> 16); // Cyclicly shift y by 16 bits
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
	
	/**
	 * @return {@code true} if this HashPoint holds the specified coordinates;
	 * {@code false} otherwise.
	 */
	public boolean equals(int x, int y) {
		return this.x == x && this.y == y;
	}
	
	@Override
	public String toString() {
		return "HashPoint[" + x + "," + y + "]";
	}
	
}
