package com.stabilise.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.maths.Point;

/**
 * Holds all slices received by a client.
 */
@Incomplete
public class SliceMapClient implements Iterable<Slice> {
	
	private final Map<Point, Slice> slices = new HashMap<Point, Slice>();
	
	public SliceMapClient() {
		
	}
	
	/**
	 * Checks to see whether all the initial slices sent to the client by the
	 * world when the client joins the world have stored.
	 * 
	 * @return {@code true} if the world may be considered loaded; {@code
	 * false} otherwise.
	 */
	public boolean isLoaded() {
		return false;
	}
	
	public void update() {
		// when a slice falls out of range, remove it from the map
	}
	
	/** puts a slice in the map when it is received by the server */
	public void putSlice(int x, int y, Slice slice) {
		slices.put(new Point(x, y), slice);
	}
	
	/** @return the slice; may be null */
	public Slice getSlice(int x, int y) {
		return slices.get(new Point(x, y));
	}
	
	public Iterator<Slice> iterator() {
		return slices.values().iterator();
	}
	
}
