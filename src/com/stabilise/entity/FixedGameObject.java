package com.stabilise.entity;

import com.stabilise.world.AbstractWorld;

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
		return AbstractWorld.sliceCoordFromTileCoord(x);
	}
	
	@Override
	public final int getSliceY() {
		return AbstractWorld.sliceCoordFromTileCoord(y);
	}
	
}
