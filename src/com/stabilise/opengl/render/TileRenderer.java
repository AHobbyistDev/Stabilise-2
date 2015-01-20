package com.stabilise.opengl.render;

import com.stabilise.util.maths.Point;
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
	public WorldRenderer worldRenderer;
	
	/** The spritesheet of tiles. */
	public SpriteSheet tiles;
	
	/** A reference to the world. */
	public ClientWorld<?> world;
	
	/** The sprite coordinates for each tile. */
	private Point[] tileCoords;
	
	
	/**
	 * Creates a new TileRenderer.
	 * 
	 * @param worldRenderer The world renderer.
	 */
	public TileRenderer(WorldRenderer worldRenderer) {
		this.worldRenderer = worldRenderer;
		world = worldRenderer.world;
		
		tileCoords = new Point[64];
		for(int i = 0; i < tileCoords.length; i++)
			tileCoords[i] = new Point(i % 8, 1 + i/8);		// The tile spritesheet is 8x8
		
		loadResources();
	}
	
	@Override
	public void update() {
		// le nothing
	}

	@Override
	public void render() {
		for(int c = world.camera.sliceX - worldRenderer.slicesHorizontal; c <= world.camera.sliceX + worldRenderer.slicesHorizontal; c++) {
			for(int r = world.camera.sliceY - worldRenderer.slicesVertical; r <= world.camera.sliceY + worldRenderer.slicesVertical; r++) {
				renderSlice(c, r);
			}
		}
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
		
		final float tileXInit = worldRenderer.offsetX + (x * Slice.SLICE_SIZE * worldRenderer.scale);
		float tileX;
		float tileY = worldRenderer.offsetY + (y * Slice.SLICE_SIZE * worldRenderer.scale);
		
		for(int r = 0; r < Slice.SLICE_SIZE; r++) {
			tileX = tileXInit;
			for(int c = 0; c < Slice.SLICE_SIZE; c++) {
				//System.out.println("Rendering tile " + c + "," + r + " in slice " + x + "," + y);
				/*
				if(slice.tiles[r][c] != 0) {
					tiles.drawSprite(slice.getTileAt(c, r).getID() + 7,		//it was -1 before breaking animations were added to spritesheet
							(int)((c + Slice.SLICE_SIZE * x) * worldRenderer.scale) + worldRenderer.offsetX,
							(int)((r + Slice.SLICE_SIZE * y) * worldRenderer.scale) + worldRenderer.offsetY);
				}
				*/
				
				///*
				int id = slice.getTileAt(c, r).getID() - 1;		// Offset of -1 since air has no texture
				
				if(id != -1) {
					tiles.drawSprite(tileCoords[id].x, tileCoords[id].y,
							(int)tileX,
							(int)tileY);
				}
				
				tileX += worldRenderer.scale;
				//*/
			}
			
			tileY += worldRenderer.scale;
		}
	}
	
	@Override
	public void loadResources() {
		tiles = new SpriteSheet("sheets/tiles", 8, 8);
		tiles.setScale((float)worldRenderer.scale / tiles.getSpriteWidth());
		tiles.filter(GL_NEAREST);
	}

	@Override
	public void unloadResources() {
		tiles.destroy();
	}
	
}