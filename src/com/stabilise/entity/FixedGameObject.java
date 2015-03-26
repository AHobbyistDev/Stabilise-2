package com.stabilise.entity;

import static com.stabilise.world.World.*;

/**
 * A FixedGameObject is a game object whose coordinates are constrained to the
 * coordinate grid of the world, and typically do not move.
 */
public abstract class FixedGameObject extends GameObject {
	
	/** The GameObject's coordinates, in tile-lengths */
	public int x, y;
	
	
	/**
	 * Creates a new FixedGameObject.
	 */
	public FixedGameObject() {
		// nothing to see here, move along
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
