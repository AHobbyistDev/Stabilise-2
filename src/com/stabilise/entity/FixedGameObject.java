package com.stabilise.entity;

import static com.stabilise.world.World.*;

/**
 * A FixedGameObject is a game object whose coordinates are constrained to the
 * coordinate grid of the world, and typically do not move.
 */
public abstract class FixedGameObject extends GameObject {
	
	/** The GameObject's coordinates, in tile-lengths. */
	public final int x, y;
	
	
	/**
	 * @param x The x-coordinate, in tile-lengths.
	 * @param y The y-coordinate, in tile lengths.
	 */
	public FixedGameObject(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public final int getSliceX() {
		return sliceCoordFromTileCoord(x);
	}
	
	@Override
	public final int getSliceY() {
		return sliceCoordFromTileCoord(y);
	}
	
}
