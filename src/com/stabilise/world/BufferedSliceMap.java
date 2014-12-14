package com.stabilise.world;

import static com.stabilise.core.Constants.BUFFER_LENGTH;
import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;
import static com.stabilise.world.Slice.SLICE_SIZE;

/**
 * A BufferedSliceMap is a SliceMap whose slices aren't immediately unloaded when
 * the player moves out of range of them - this is to prevent performance loss
 * when the player constantly crosses the boundary between slices.
 * 
 * @deprecated
 */
public class BufferedSliceMap extends SliceMapOld {
	
	/**
	 * Creates a new SliceMap instance.
	 * @param world The world to base the slice map on.
	 */
	public BufferedSliceMap(GameWorld world) {
		super(world);
	}
	
	@Override
	public void update() {
		playerSliceX = (int)Math.floor(world.player.x / SLICE_SIZE);
		playerSliceY = (int)Math.floor(world.player.y / SLICE_SIZE);
		
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
		else if(minSliceXLoaded > oldMinX) unloadCol(oldMinX - BUFFER_LENGTH, oldMinY - BUFFER_LENGTH, oldMaxY + BUFFER_LENGTH);
		
		if(maxSliceXLoaded < oldMaxX) unloadCol(oldMaxX + BUFFER_LENGTH, oldMinY - BUFFER_LENGTH, oldMaxY + BUFFER_LENGTH);
		else if(maxSliceXLoaded > oldMaxX) loadCol(maxSliceXLoaded);
		
		if(minSliceYLoaded < oldMinY) loadRow(minSliceYLoaded);
		else if(minSliceYLoaded > oldMinY) unloadRow(oldMinY - BUFFER_LENGTH, oldMinX - BUFFER_LENGTH, oldMaxX + BUFFER_LENGTH);
		
		if(maxSliceYLoaded < oldMaxY) unloadRow(oldMaxY + BUFFER_LENGTH, oldMinX - BUFFER_LENGTH, oldMaxX + BUFFER_LENGTH);
		else if(maxSliceYLoaded > oldMaxY) loadRow(maxSliceYLoaded);
	}
	
	@Override
	public void addSlice(Slice slice) {
		if(slice.x < minSliceXLoaded - BUFFER_LENGTH ||
				slice.x > maxSliceXLoaded + BUFFER_LENGTH ||
				slice.y < minSliceYLoaded - BUFFER_LENGTH ||
				slice.y > maxSliceYLoaded + BUFFER_LENGTH)
			;//----world.client.notifyOfSliceUnload(slice.x, slice.y);
		else
			slices.put(getSliceKey(slice.x, slice.y), slice);
	}

}
