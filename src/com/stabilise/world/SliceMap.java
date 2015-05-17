package com.stabilise.world;

import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;

import java.util.Objects;

import com.stabilise.core.Constants;
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
 */
public class SliceMap {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of slices which must be travelled along any axis in a tick
	 * to trigger a complete refresh. */
	private static final int REFRESH_BOUNDARY = Constants.LOADED_SLICE_RADIUS;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** A reference to the world object. */
	private HostWorld world;
	/** The slice map's central target. */
	private final GameObject target;
	
	/** The target's most recent slice positions (i.e. the centre slices of the
	 * map). */
	private int centreX, centreY;
	/** Coordinates determining which slices are to be loaded. */
	protected int minSliceX, maxSliceX, minSliceY, maxSliceY;
	
	
	/**
	 * Creates a new SliceMap instance.
	 * 
	 * @param world The world to base the slice map on.
	 * @param target The entity around which to load slices.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public SliceMap(HostWorld world, GameObject target) {
		this.world = Objects.requireNonNull(world);
		this.target = Objects.requireNonNull(target);
		
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
		
		// If the target has moved very far, opt to refresh instead.
		if(Math.abs(sliceX - centreX) >= REFRESH_BOUNDARY ||
				Math.abs(sliceY - centreY) >= REFRESH_BOUNDARY) {
			refresh();
			return;
		}
		
		centreX = sliceX;
		centreY = sliceY;
		
		int oldMinX = minSliceX;
		int oldMaxX = maxSliceX;
		int oldMinY = minSliceY;
		int oldMaxY = maxSliceY;
		
		minSliceX = centreX - LOADED_SLICE_RADIUS;
		maxSliceX = centreX + LOADED_SLICE_RADIUS;
		minSliceY = centreY - LOADED_SLICE_RADIUS;
		maxSliceY = centreY + LOADED_SLICE_RADIUS;
		
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
		//   purposes such as these, refresh() is invoked instead.
		
		// Implementation note: if both centreX and centreY were changed, some
		// slices may be double-counted. As such, we blanket-sweep columns with
		// x, but tread carefully with y to make sure we don't double-count.
		
		for(int x = minSliceX; x < oldMinX; x++) loadCol(x, minSliceY, maxSliceY);
		for(int x = maxSliceX; x > oldMaxX; x--) loadCol(x, minSliceY, maxSliceY);
		for(int y = minSliceY; y < oldMinY; y++)
			loadRow(y, Math.max(oldMinX, minSliceX), Math.min(oldMaxX, maxSliceX));
		for(int y = maxSliceY; y > oldMaxY; y--)
			loadRow(y, Math.max(oldMinX, minSliceX), Math.min(oldMaxX, maxSliceX));
		
		for(int x = oldMinX; x < minSliceX; x++) unloadCol(x, oldMinY, oldMaxY);
		for(int x = oldMaxX; x > maxSliceX; x--) unloadCol(x, oldMinY, oldMaxY);
		for(int y = oldMinY; y < minSliceY; y++)
			unloadRow(y, Math.max(oldMinX, minSliceX), Math.min(oldMaxX, maxSliceX));
		for(int y = oldMaxY; y > maxSliceY; y--)
			unloadRow(y, Math.max(oldMinX, minSliceX), Math.min(oldMaxX, maxSliceX));
	}
	
	private void loadCol(int x, int minY, int maxY) {
		for(int y = minY; y <= maxY; y++) world.loadSlice(x, y);
	}
	
	private void loadRow(int y, int minX, int maxX) {
		for(int x = minX; x <= maxX; x++) world.loadSlice(x, y);
	}
	
	/**
	 * Loads a column of slices.
	 * 
	 * @param x The x-coordinate of the column, in slice-lengths.
	 */
	protected void loadCol(int x) {
		for(int y = minSliceY; y <= maxSliceY; y++)
			world.loadSlice(x, y);
	}
	
	/**
	 * Loads a row of slices.
	 * 
	 * @param y The y-coordinate of the row, in slice-lengths.
	 */
	protected void loadRow(int y) {
		for(int x = minSliceX; x <= maxSliceX; x++)
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
		int oldMinX = minSliceX;
		int oldMaxX = maxSliceX;
		int oldMinY = minSliceY;
		int oldMaxY = maxSliceY;
		
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
		minSliceX = centreX - LOADED_SLICE_RADIUS;
		maxSliceX = centreX + LOADED_SLICE_RADIUS;
		minSliceY = centreY - LOADED_SLICE_RADIUS;
		maxSliceY = centreY + LOADED_SLICE_RADIUS;
		
		for(int x = minSliceX; x <= maxSliceX; x++)
			loadCol(x);
	}
	
	/**
	 * Unloads all slices maintained by this SliceMap. Usage of this map should
	 * be discontinued after invoking this.
	 */
	public void unload() {
		for(int x = minSliceX; x <= maxSliceX; x++)
			unloadCol(x, minSliceY, maxSliceY);
	}
	
}
