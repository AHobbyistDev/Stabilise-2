package com.stabilise.world.tile;

import com.stabilise.world.World;

/**
 * A grass tile.
 */
public class TileGrass extends Tile {
	
	/**
	 * Creates a new grass tile.
	 */
	TileGrass(TileBuilder b) {
		super(b);
	}
	
	@Override
	public void update(World world, int x, int y) {
		if(world.getTileAt(x, y+1).isSolid()) {
			world.setTileAt(x, y, Tiles.DIRT);
			return;
		}
		
		Tile t1, t2;
		
		for(int tx = x-1; tx <= x+1; tx++) {
			t2 = world.getTileAt(tx, y-2);
			for(int ty = y-2; ty <= y+1; ty ++) {
				// If a tile is dirt and it has an air tile above it, spread
				// grass to that tile
				t1 = t2;
				t2 = world.getTileAt(tx, ty+1);
				if(t1 == Tiles.DIRT && t2 == Tiles.AIR)
					world.setTileAt(tx, ty, getID());
			}
		}
	}
	
}
