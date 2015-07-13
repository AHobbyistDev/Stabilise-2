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
		
		for(int tx = x-1; tx <= x+1; tx++) {
			Tile t1;
			Tile t2 = world.getTileAt(tx, y-2);
			for(int ty = y-1; ty <= y+1; ty ++) {
				// If a tile is dirt and it has an air tile above it, spread the grass to that tile
				t1 = t2;
				t2 = world.getTileAt(tx, ty);
				if(t1.getID() == Tiles.DIRT.getID() && t2.getID() == Tiles.AIR.getID())
					world.setTileAt(tx, ty-1, getID());
			}
		}
	}
	
}
