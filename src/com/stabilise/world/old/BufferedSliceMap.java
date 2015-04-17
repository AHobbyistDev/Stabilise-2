package com.stabilise.world.old;

import static com.stabilise.core.Constants.LOADED_SLICE_BUFFER;
import static com.stabilise.core.Constants.LOADED_SLICE_RADIUS;

import com.stabilise.world.HostWorld;
import com.stabilise.world.Slice;

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
	public BufferedSliceMap(HostWorld world) {
		super(world);
	}
	
	@Override
	public void update() {
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
		else if(minSliceXLoaded > oldMinX) unloadCol(oldMinX - LOADED_SLICE_BUFFER, oldMinY - LOADED_SLICE_BUFFER, oldMaxY + LOADED_SLICE_BUFFER);
		
		if(maxSliceXLoaded < oldMaxX) unloadCol(oldMaxX + LOADED_SLICE_BUFFER, oldMinY - LOADED_SLICE_BUFFER, oldMaxY + LOADED_SLICE_BUFFER);
		else if(maxSliceXLoaded > oldMaxX) loadCol(maxSliceXLoaded);
		
		if(minSliceYLoaded < oldMinY) loadRow(minSliceYLoaded);
		else if(minSliceYLoaded > oldMinY) unloadRow(oldMinY - LOADED_SLICE_BUFFER, oldMinX - LOADED_SLICE_BUFFER, oldMaxX + LOADED_SLICE_BUFFER);
		
		if(maxSliceYLoaded < oldMaxY) unloadRow(oldMaxY + LOADED_SLICE_BUFFER, oldMinX - LOADED_SLICE_BUFFER, oldMaxX + LOADED_SLICE_BUFFER);
		else if(maxSliceYLoaded > oldMaxY) loadRow(maxSliceYLoaded);
	}
	
	@Override
	public void addSlice(Slice slice) {
		if(slice.x < minSliceXLoaded - LOADED_SLICE_BUFFER ||
				slice.x > maxSliceXLoaded + LOADED_SLICE_BUFFER ||
				slice.y < minSliceYLoaded - LOADED_SLICE_BUFFER ||
				slice.y > maxSliceYLoaded + LOADED_SLICE_BUFFER)
			;//----world.client.notifyOfSliceUnload(slice.x, slice.y);
		else
			slices.put(getSliceKey(slice.x, slice.y), slice);
	}

}
