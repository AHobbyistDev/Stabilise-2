package com.stabilise.world.tile;

import com.stabilise.world.IWorld;
import com.stabilise.world.tile.tileentity.TileEntityMobSpawner;

/**
 * Mob spawner tile. Has an associated tile entity.
 * 
 * @see TileEntityMobSpawner
 */
public class TileMobSpawner extends Tile {
	
	public TileMobSpawner() {
		super();
		setHardness(Tile.HARDNESS_STONE);
	}
	
	@Override
	public void handlePlace(IWorld world, int x, int y) {
		super.handlePlace(world, x, y);
		TileEntityMobSpawner t = createTileEntity(x, y);
		t.world = world;
		world.setTileEntityAt(x, y, t);
	}
	
	/**
	 * Creates the tile entity associated with a mob spawner for the given
	 * coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The mob spawner tile entity object.
	 */
	public TileEntityMobSpawner createTileEntity(int x, int y) {
		return new TileEntityMobSpawner(x, y);
	}
	
	@Override
	public void handleRemove(IWorld world, int x, int y) {
		super.handleRemove(world, x, y);
		world.removeTileEntityAt(x, y);
	}
	
}
