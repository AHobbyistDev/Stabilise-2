package com.stabilise.world;

import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;

import com.stabilise.entity.GameObject;

/**
 * A SliceMap is tasked with mapping the slices which should be loaded about
 * a targeted GameObject (typically the player) and ensuring they are loaded
 * and unloaded appropriately by the world.
 * 
 * <p>A SliceMap should be used by invoking {@link #update()} every tick to
 * ensure the world is properly loaded about the target and unloaded when
 * necessary. When a SliceMap is no longer needed, invoke {@link #unload()} and
 * ensure that {@link #update()} is no longer invoked thereafter.
 * 
 * <p>
 */
public class SliceMap {
	
	/** A reference to the world object. */
	private GameWorld world;
	/** The slice map's central target. */
	private GameObject target;
	
	/** The target's most recent slice positions (i.e. the centre slices of the
	 * map). */
	private int centreX, centreY;
	/** Coordinates determining which slices are to be loaded. */
	protected int minSliceXLoaded, maxSliceXLoaded, minSliceYLoaded, maxSliceYLoaded;
	
	
	/**
	 * Creates a new SliceMap instance.
	 * 
	 * @param world The world to base the slice map on.
	 * @param target The entity around which to load slices.
	 */
	public SliceMap(GameWorld world, GameObject target) {
		if(world == null)
			throw new IllegalArgumentException("world is null!");
		if(target == null)
			throw new IllegalArgumentException("target is null!");
		
		this.world = world;
		this.target = target;
		
		reload();
	}
	
	/**
	 * Updates the SliceMap; the map will determine which slices need to be
	 * loaded, and which need to be unloaded.
	 */
	public void update() {
		int sliceX = target.getSliceX();
		int sliceY = target.getSliceY();
		
		// If the target hasn't moved slices, nothing needs to be changed
		if(centreX == sliceX && centreY == sliceY)
			return;
		
		centreX = sliceX;
		centreY = sliceY;
		
		int oldMinX = minSliceXLoaded;
		int oldMaxX = maxSliceXLoaded;
		int oldMinY = minSliceYLoaded;
		int oldMaxY = maxSliceYLoaded;
		
		minSliceXLoaded = centreX - LOADED_SLICE_RADIUS;
		maxSliceXLoaded = centreX + LOADED_SLICE_RADIUS;
		minSliceYLoaded = centreY - LOADED_SLICE_RADIUS;
		maxSliceYLoaded = centreY + LOADED_SLICE_RADIUS;
		
		// This way of loading and unloading slices has the following
		// characteristics:
		// > It isn't as efficient as using 'if' conditionals for when the
		//   target moves only 1 slice in a tick (which should be the case 99%
		//   of the time).
		// > It capably loads and unloads slices no matter how fast the target
		//   moves.
		// > However, it becomes extremely inefficient when the target
		//   approaches very high speeds (or moves very far due to, say,
		//   teleporting) as it loads and unloads as necessary everything
		//   between point A and point B, no matter how far apart they are. For
		//   purposes such as these, it may be wiser to invoke refresh().
		
		for(int x = minSliceXLoaded; x < oldMinX; x++) loadCol(x);
		for(int x = maxSliceXLoaded; x > oldMaxX; x--) loadCol(x);
		for(int y = minSliceYLoaded; y < oldMinY; y++) loadRow(y);
		for(int y = maxSliceYLoaded; y > oldMaxY; y--) loadRow(y);
		
		for(int x = oldMinX; x < minSliceXLoaded; x++) unloadCol(x, oldMinY, oldMaxY);
		for(int x = maxSliceXLoaded; x < oldMaxX; x++) unloadCol(x, oldMinY, oldMaxY);
		for(int y = oldMinY; y < minSliceYLoaded; y++) unloadRow(y, oldMinX, oldMaxX);
		for(int y = maxSliceYLoaded; y < oldMaxY; y++) unloadRow(y, oldMinX, oldMaxX);
		
		// The following legacy code doesn't work if the player moves >1 slice
		// in any direction in a tick.
		/*
		if(minSliceXLoaded < oldMinX) loadCol(minSliceXLoaded);
		else if(minSliceXLoaded > oldMinX) unloadCol(oldMinX, oldMinY, oldMaxY);
		
		if(maxSliceXLoaded < oldMaxX) unloadCol(oldMaxX, oldMinY, oldMaxY);
		else if(maxSliceXLoaded > oldMaxX) loadCol(maxSliceXLoaded);
		
		if(minSliceYLoaded < oldMinY) loadRow(minSliceYLoaded);
		else if(minSliceYLoaded > oldMinY) unloadRow(oldMinY, oldMinX, oldMaxX);
		
		if(maxSliceYLoaded < oldMaxY) unloadRow(oldMaxY, oldMinX, oldMaxX);
		else if(maxSliceYLoaded > oldMaxY) loadRow(maxSliceYLoaded);
		*/
	}
	
	/**
	 * Loads a column of slices.
	 * 
	 * @param x The x-coordinate of the column, in slice-lengths.
	 */
	protected void loadCol(int x) {
		for(int y = minSliceYLoaded; y <= maxSliceYLoaded; y++)
			world.loadSlice(x, y);
	}
	
	/**
	 * Loads a row of slices.
	 * 
	 * @param y The y-coordinate of the row, in slice-lengths.
	 */
	protected void loadRow(int y) {
		for(int x = minSliceXLoaded; x <= maxSliceXLoaded; x++)
			world.loadSlice(x, y);
	}
	
	/**
	 * Unloads a column of slices.
	 * 
	 * @param x The x-coordinate of the column, in slice-lengths.
	 * @param minY The minimum y-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 * @param maxY The maximum y-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 */
	protected void unloadCol(int x, int minY, int maxY) {
		for(int y = minY; y <= maxY; y++)
			world.unloadSlice(x, y);
	}
	
	/**
	 * Unloads a row of slices.
	 * 
	 * @param y The y-coordinate of the column, in slice-lengths.
	 * @param minX The minimum x-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 * @param maxX The maximum x-coordinate of the slices to be removed in
	 * slice-lengths, inclusive.
	 */
	protected void unloadRow(int y, int minX, int maxX) {
		for(int x = minX; x <= maxX; x++)
			world.unloadSlice(x, y);
	}
	
	/**
	 * Refreshes the map of loaded slices. This is useful to call if, say, the
	 * target is teleported and an entirely new batch of slices will need to be
	 * loaded.
	 */
	public void refresh() {
		int oldMinX = minSliceXLoaded;
		int oldMaxX = maxSliceXLoaded;
		int oldMinY = minSliceYLoaded;
		int oldMaxY = maxSliceYLoaded;
		
		// Reload before unloading to prevent slices from being unloaded
		// unnecessarily if they are to remain loaded anyway. The temporary
		// double-anchorage of slices should not cause any issues.
		reload();
		
		for(int x = oldMinX; x <= oldMaxX; x++)
			unloadCol(x, oldMinY, oldMaxY);
	}
	
	/**
	 * Loads the slices which should be loaded about the target.
	 */
	private void reload() {
		centreX = target.getSliceX();
		centreY = target.getSliceY();
		minSliceXLoaded = centreX - LOADED_SLICE_RADIUS;
		maxSliceXLoaded = centreX + LOADED_SLICE_RADIUS;
		minSliceYLoaded = centreY - LOADED_SLICE_RADIUS;
		maxSliceYLoaded = centreY + LOADED_SLICE_RADIUS;
		
		for(int x = minSliceXLoaded; x <= maxSliceXLoaded; x++)
			for(int y = minSliceYLoaded; y <= maxSliceYLoaded; y++)
				world.loadSlice(x, y);
	}
	
	/**
	 * Unloads all slices maintained by this SliceMap. Usage of this map should
	 * be discontinued after invoking this.
	 */
	public void unload() {
		for(int x = minSliceXLoaded; x <= maxSliceXLoaded; x++)
			unloadCol(x, minSliceYLoaded, maxSliceYLoaded);
	}
	
}
