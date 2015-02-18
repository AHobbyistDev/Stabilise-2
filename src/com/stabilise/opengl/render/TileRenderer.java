package com.stabilise.opengl.render;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.stabilise.core.Resources;
import com.stabilise.opengl.TextureSheet;
import com.stabilise.world.Slice;
import com.stabilise.world.old.ClientWorld;

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
	
	/** Number of slices rendered on each render step. */
	int slicesRendered = 0;
	
	
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
		tiles.texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
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
		slicesRendered = 0;
		for(int c = worldRenderer.playerCamera.sliceX - worldRenderer.slicesHorizontal;
				c <= worldRenderer.playerCamera.sliceX + worldRenderer.slicesHorizontal;
				c++)
			for(int r = worldRenderer.playerCamera.sliceY - worldRenderer.slicesVertical;
					r <= worldRenderer.playerCamera.sliceY + worldRenderer.slicesVertical;
					r++)
				renderSlice(c, r);
		//System.out.println(slicesRendered + " slices rendered");
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
		
		slicesRendered++;
		
		final int tileXInit = x * Slice.SLICE_SIZE;
		int tileX;
		int tileY = y * Slice.SLICE_SIZE;
		
		for(int r = 0; r < Slice.SLICE_SIZE; r++) {
			tileX = tileXInit;
			for(int c = 0; c < Slice.SLICE_SIZE; c++) {
				// Offset of +8 due to tile breaking animations; offset of -1
				// because air has no texture: sums to +7
				int id = slice.getTileIDAt(c, r) + 7;
				
				if(id != 7) // i.e. not air
					worldRenderer.batch.draw(tiles.getRegion(id), tileX, tileY, 1f, 1f);
				
				tileX++;
			}
			
			tileY++;
		}
	}
	
}