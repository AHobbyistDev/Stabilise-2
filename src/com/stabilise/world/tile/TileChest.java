package com.stabilise.world.tile;

import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.util.Log;
import com.stabilise.world.IWorld;
import com.stabilise.world.tile.tileentity.TileEntityChest;

/**
 * A chest tile.
 */
public class TileChest extends Tile {
	
	/**
	 * Creates a chest tile.
	 */
	TileChest() {
		super();
		
		setHardness(Tile.HARDNESS_WOOD);
	}
	
	@Override
	public void handleStep(IWorld world, int x, int y, Entity e) {
		super.handleStep(world, x, y, e);
		
		/*
		TileEntityChest c = (TileEntityChest)world.getTileEntityAt(x, y);
		// TODO: temporary
		if(c == null)
			return;
		System.out.println("Contents of the chest at (" + x + "," + y + "):\n" + c.items.toString());
		*/
	}
	
	@Override
	public void handlePlace(IWorld world, int x, int y) {
		super.handlePlace(world, x, y);
		TileEntityChest t = createTileEntity(x, y);
		t.world = world;
		world.setTileEntityAt(x, y, t);
	}
	
	/**
	 * Creates the tile entity associated with a chest for the given
	 * coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The chest tile entity object.
	 */
	public TileEntityChest createTileEntity(int x, int y) {
		return new TileEntityChest(x, y);
	}
	
	@Override
	public void handleRemove(IWorld world, int x, int y) {
		super.handleRemove(world, x, y);
		world.removeTileEntityAt(x, y);
	}
	
	@Override
	public void handleInteract(IWorld world, int x, int y, EntityMob mob) {
		super.handleInteract(world, x, y, mob);
		
		TileEntityChest c = (TileEntityChest)world.getTileEntityAt(x, y);
		// TODO: temporary
		if(c == null)
			Log.get().postWarning("The chest tile entity is missing!");
		else
			Log.get().postDebug(c.toString());
	}
	
}
