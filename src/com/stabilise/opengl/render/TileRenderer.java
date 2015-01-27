package com.stabilise.opengl.render;

import com.stabilise.core.Resources;
import com.stabilise.opengl.TextureSheet;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.Slice;

/**
 * The TileRenderer class manages the rendering of the tiles that constitute a
 * world.
 */
public class TileRenderer implements Renderer {
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** A reference to the world renderer. */
	public final WorldRenderer worldRenderer;
	/** A reference to the world. */
	public final ClientWorld<?> world;
	
	public TextureSheet tiles;
	
	
	/**
	 * Creates a new TileRenderer.
	 * 
	 * @param worldRenderer The world renderer.
	 */
	public TileRenderer(WorldRenderer worldRenderer) {
		this.worldRenderer = worldRenderer;
		world = worldRenderer.world;
		
		loadResources();
	}
	
	@Override
	public void loadResources() {
		tiles = TextureSheet.sequentiallyOptimised(Resources.texture("sheets/tiles"), 8, 8);
	}
	
	@Override
	public void unloadResources() {
		tiles.dispose();
	}
	
	@Override
	public void resize(int width, int height) {
		// le nothing
	}
	
	@Override
	public void update() {
		// le nothing
	}

	@Override
	public void render() {
		for(int c = world.camera.sliceX - worldRenderer.slicesHorizontal; c <= world.camera.sliceX + worldRenderer.slicesHorizontal; c++)
			for(int r = world.camera.sliceY - worldRenderer.slicesVertical; r <= world.camera.sliceY + worldRenderer.slicesVertical; r++)
				renderSlice(c, r);
	}
	
	/**
	 * Renders a slice.
	 * 
	 * @param x The x-coordinate of the slice, in slice-lengths.
	 * @param y The y-coordinate of the slice, in slice-lengths.
	 */
	private void renderSlice(int x, int y) {
		Slice slice = world.getSliceAt(x, y);
		
		if(slice == null)
			return;
		
		final float tileXInit = worldRenderer.offsetX + (x * Slice.SLICE_SIZE);
		float tileX;
		float tileY = worldRenderer.offsetY + (y * Slice.SLICE_SIZE);
		
		for(int r = 0; r < Slice.SLICE_SIZE; r++) {
			tileX = tileXInit;
			for(int c = 0; c < Slice.SLICE_SIZE; c++) {
				// Offset of +8 due to tile breaking animations; offset of -1
				// because air has no texture: sums to +7
				int id = slice.getTileAt(c, r).getID() + 7;
				
				if(id != 7) // not air
					worldRenderer.batch.draw(tiles.getRegion(id), tileX, tileY, 1f, 1f);
				
				tileX++; // formerly + worldRenderer.pixelsPerTile
			}
			
			tileY++; // formerly + worldRenderer.pixelsPerTile
		}
	}
	
}