package com.stabilise.world.old;

import java.util.HashMap;

import com.stabilise.world.HostWorld;
import com.stabilise.world.Slice;

import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;
import static com.stabilise.world.Slice.SLICE_SIZE;

/**
 * The SliceMap class represents a map of loaded slices for a client.
 * 
 * @deprecated
 */
public class SliceMapOld {
	
	/** The HashMap of all loaded slices. */
	protected HashMap<Integer, Slice> slices = new HashMap<Integer, Slice>();
	
	/** A reference to the world object. */
	protected HostWorld world;
	
	/** The x-coordinate of the slice the player is in. TODO: Refactor / is this necessary? */
	protected int playerSliceX;
	/** The y-coordinate of the slice the player is in. TODO: Refactor / is this necessary? */
	protected int playerSliceY;
	
	protected int minSliceXLoaded;
	protected int maxSliceXLoaded;
	protected int minSliceYLoaded;
	protected int maxSliceYLoaded;
	
	/** True if the SliceMap has been set up. This is to prevent reuse of init(). */
	private boolean initialised = false;
	
	
	/**
	 * Creates a new SliceMap instance.
	 * 
	 * @param world The world to base the slice map on.
	 */
	public SliceMapOld(HostWorld world) {
		this.world = world;
	}
	
	/**
	 * Sets up the SliceMap.
	 */
	public void init() {
		if(initialised) return;
		
		//----playerSliceX = (int)Math.floor(world.player.x / SLICE_SIZE);
		//----playerSliceY = (int)Math.floor(world.player.y / SLICE_SIZE);
		
		minSliceXLoaded = playerSliceX - LOADED_SLICE_RADIUS;
		maxSliceXLoaded = playerSliceX + LOADED_SLICE_RADIUS;
		minSliceYLoaded = playerSliceY - LOADED_SLICE_RADIUS;
		maxSliceYLoaded = playerSliceY + LOADED_SLICE_RADIUS;
		
		for(int x = minSliceXLoaded; x <= maxSliceXLoaded; x++) {
			for(int y = minSliceYLoaded; y <= maxSliceYLoaded; y++) {
				;//----requestSlice(x, y);
			}
		}
		
		initialised = true;
	}
	
	/**
	 * Updates the SliceMap; the map will determine which slices need to be
	 * loaded, and which need to be unloaded.
	 */
	public void update() {
		// TODO: Better method?
		if(!initialised)
			return;
		
		//----playerSliceX = (int)Math.floor(world.player.x / SLICE_SIZE);
		//----playerSliceY = (int)Math.floor(world.player.y / SLICE_SIZE);
		
		int oldMinX = minSliceXLoaded;
		int oldMaxX = maxSliceXLoaded;
		int oldMinY = minSliceYLoaded;
		int oldMaxY = maxSliceYLoaded;
		
		minSliceXLoaded = playerSliceX - LOADED_SLICE_RADIUS;
		maxSliceXLoaded = playerSliceX + LOADED_SLICE_RADIUS;
		minSliceYLoaded = playerSliceY - LOADED_SLICE_RADIUS;
		maxSliceYLoaded = playerSliceY + LOADED_SLICE_RADIUS;
		
		// The following obviously won't work if the player moves >1 slice in a tick.
		// At the moment it doesn't really matter though.
		// TODO: Better implementation.
		if(minSliceXLoaded < oldMinX) loadCol(minSliceXLoaded);
		else if(minSliceXLoaded > oldMinX) unloadCol(oldMinX, oldMinY, oldMaxY);
		
		if(maxSliceXLoaded < oldMaxX) unloadCol(oldMaxX, oldMinY, oldMaxY);
		else if(maxSliceXLoaded > oldMaxX) loadCol(maxSliceXLoaded);
		
		if(minSliceYLoaded < oldMinY) loadRow(minSliceYLoaded);
		else if(minSliceYLoaded > oldMinY) unloadRow(oldMinY, oldMinX, oldMaxX);
		
		if(maxSliceYLoaded < oldMaxY) unloadRow(oldMaxY, oldMinX, oldMaxX);
		else if(maxSliceYLoaded > oldMaxY) loadRow(maxSliceYLoaded);
	}
	
	/**
	 * Loads a column of slices.
	 * @param x The x-coordinate of the column, in slice-lengths.
	 */
	protected void loadCol(int x) {
		for(int y = minSliceYLoaded; y <= maxSliceYLoaded; y++)
			;//requestSlice(x, y);
	}
	
	/**
	 * Loads a row of slices.
	 * 
	 * @param y The y-coordinate of the row, in slice-lengths.
	 */
	protected void loadRow(int y) {
		for(int x = minSliceXLoaded; x <= maxSliceXLoaded; x++)
			;//requestSlice(x, y);
	}
	
	/**
	 * Unloads a column of slices.
	 * @param x The x-coordinate of the column, in slice-lengths.
	 * @param minY The minimum y-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 * @param maxY The maximum y-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 */
	protected void unloadCol(int x, int minY, int maxY) {
		for(int y = minY; y <= maxY; y++)
			;//unloadSlice(x, y);
	}
	
	/**
	 * Unloads a row of slices.
	 * @param y The y-coordinate of the column, in slice-lengths.
	 * @param minX The minimum x-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 * @param maxX The maximum x-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 */
	protected void unloadRow(int y, int minX, int maxX) {
		for(int x = minX; x <= maxX; x++)
			unloadSlice(x, y);
	}
	
	/**
	 * Requests a slice from the server.
	 * 
	 * @param x The x-coordinate of the slice, in slice-lengths.
	 * @param y The y-coordinate of the slice, in slice-lengths.
	 * 
	 * @deprecated Due to the removal of networking architecture.
	 */
	/*
	public void requestSlice(int x, int y) {
		if(!slices.containsKey(getSliceKey(x, y))) {
			// Send a slice request to the server
			world.client.requestSlice(x, y);
			// Put in a temporary value to indicate the slice has been
			// requested.
			slices.put(getSliceKey(x, y), null);
		}
	}
	*/
	
	/**
	 * Adds a slice to the slice map.
	 * @param slice The slice to add to the map.
	 * @param x The slice's x-coordinate, in slice-lengths.
	 * @param y The slice's y-coordinate, in slice-lengths.
	 */
	public void addSlice(Slice slice) {
		// Just in case we receive a slice which shouldn't be loaded
		if(slice.x < minSliceXLoaded || slice.x > maxSliceXLoaded || slice.y < minSliceYLoaded || slice.y > maxSliceYLoaded)
			;//world.client.notifyOfSliceUnload(slice.x, slice.y);
		else
			slices.put(getSliceKey(slice.x, slice.y), slice);
	}
	
	/**
	 * Unloads a slice from the map.
	 * 
	 * @param x The slice's x-coordinate, in slice-lengths.
	 * @param y The slice's y-coordinate, in slice-lengths.
	 */
	protected void unloadSlice(int x, int y) {
		if(slices.containsKey(getSliceKey(x, y))) {
			slices.remove(getSliceKey(x, y));
			//world.client.notifyOfSliceUnload(x, y);
		}
	}
	
	/**
	 * Returns the integer key to use for referencing a slice.
	 * 
	 * @param x The slice's x-coordinate, in slice-lengths.
	 * @param y The slice's y-coordinate, in slice-lengths.
	 */
	protected int getSliceKey(int x, int y) {
		return ((x & 0xFFFF) << 16) + (y & 0xFFFF);
	}
	
	//--------------------==========--------------------
	//-----------------=====Getters=====----------------
	//--------------------==========--------------------
	
	// TODO: Using ints and doubles to distinguish between tile-lengths and
	// slice-lengths is very more than likely a horrible practice.
	// CHANGE SOMETIME IN THE FUTURE.
	
	/**
	 * Returns the slice at the given coordinates.
	 * @param x The slice's x-coordinate, in slice lengths.
	 * @param y The slice's y-coordinate, in slice lengths.
	 */
	public Slice getSliceAt(int x, int y) {
		return slices.get(getSliceKey(x, y));
	}
	
	/**
	 * Returns the slice at the given coordinates.
	 * @param x The slice's x-coordinate, in tile lengths.
	 * @param y The slice's y-coordinate, in tile lengths.
	 */
	public Slice getSliceAt(double x, double y) {
		// TODO: Efficiencyify to minimise casting required
		return getSliceAt((int)Math.floor((x / SLICE_SIZE)), (int)Math.floor((y / SLICE_SIZE)));
	}
	
	/*
	public Tile getTileAt(int x, int y) {
		return getSliceAt(x, y).getTileAt(
				MathUtil.calcWrappedRemainder(x, SLICE_SIZE),
				MathUtil.calcWrappedRemainder(y, SLICE_SIZE)
		);
	}
	*/
	
	/*
	public Tile getTileAt(double x, double y) {
		return getSliceAt(x, y).getTileAt(
				(int)Math.floor(MathUtil.calcWrappedRemainder(x, SLICE_SIZE)),
				(int)Math.floor(MathUtil.calcWrappedRemainder(y, SLICE_SIZE))
		);
	}
	*/

}
