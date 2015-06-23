package com.stabilise.world;

import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;
import static com.stabilise.core.Constants.LOADED_SLICE_BUFFER;
import static com.stabilise.core.Constants.MAX_LOADED_SLICES;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.stabilise.entity.GameObject;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Point;
import com.stabilise.util.maths.PointFactory;

/**
 * Holds all slices received by a client.
 */
@SuppressWarnings("unused")
@Incomplete
@NotThreadSafe
public class SliceMapClient implements Iterable<Slice> {
	
	private final PointFactory pointFactory = new PointFactory(MAX_LOADED_SLICES, true);
	/** Dummy point to use for hash table getters. */
	private final Point dummyPoint = pointFactory.newMutablePoint();
	
	/** Holds slices. Maps slice location -> slice. */
	private final Map<Point, Slice> slices = new HashMap<>(MAX_LOADED_SLICES);
	
	private final ClientWorld world;
	private GameObject target;
	
	/** The target's most recent slice positions (i.e. the centre slices of the
	 * map). */
	private int centreX, centreY;
	/** Coordinates determining which slices are to be loaded. */
	protected int minSliceX, maxSliceX, minSliceY, maxSliceY;
	
	
	public SliceMapClient(ClientWorld world, GameObject target) {
		this.world = Objects.requireNonNull(world);
		this.target = Objects.requireNonNull(target);
		
		centreX = target.getSliceX();
		centreY = target.getSliceY();
		refresh();
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
		if(centreX == (centreX = target.getSliceX())
				&& centreY == (centreY = target.getSliceY()))
			return;
		
		// Use refresh() to do all the work for us.
		refresh();
	}
	
	/**
	 * Puts a slice into the map, if the slice is within the current boundary.
	 * 
	 * @return {@code true} if the slice was added; {@code false} otherwise.
	 */
	public boolean putSlice(int x, int y, Slice slice) {
		if(!sliceInRange(slice))
			return false;
		slices.put(pointFactory.newImmutablePoint(x, y), slice);
		return true;
	}
	
	/**
	 * @return The slice, or {@code null} if no such slice is mapped.
	 */
	public Slice getSlice(int x, int y) {
		return slices.get(dummyPoint.set(x, y));
	}
	
	@Override
	public Iterator<Slice> iterator() {
		return slices.values().iterator();
	}
	
	/**
	 * Refreshes the boundaries and removes every slice no longer in bounds.
	 */
	private void refresh() {
		refreshBoundaries();
		
		Iterator<Slice> i = slices.values().iterator();
		while(i.hasNext())
			if(!sliceInRange(i.next()))
				i.remove();
	}
	
	/**
	 * Refreshes the slice boundaries and updates them to a more current value.
	 */
	private void refreshBoundaries() {
		minSliceX = centreX - LOADED_SLICE_RADIUS - LOADED_SLICE_BUFFER;
		maxSliceX = centreX + LOADED_SLICE_RADIUS + LOADED_SLICE_BUFFER;
		minSliceY = centreY - LOADED_SLICE_RADIUS - LOADED_SLICE_BUFFER;
		maxSliceY = centreY + LOADED_SLICE_RADIUS + LOADED_SLICE_BUFFER;
	}
	
	private boolean sliceInRange(Slice s) {
		return s.x >= minSliceX && s.x <= maxSliceX
				&& s.y >= minSliceY && s.y <= maxSliceY;
	}
	
	/**
	 * Unloads all slices in the map.
	 */
	public void unload() {
		slices.clear();
	}
	
}
