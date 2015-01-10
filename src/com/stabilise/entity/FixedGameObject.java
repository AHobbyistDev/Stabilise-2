package com.stabilise.entity;

import com.stabilise.world.BaseWorld;
import com.stabilise.world.IWorld;

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
	
	/**
	 * Creates a new GameObject.
	 * 
	 * @param world The world.
	 */
	public FixedGameObject(IWorld world) {
		super(world);
	}
	
	@Override
	public final int getSliceX() {
		return BaseWorld.sliceCoordFromTileCoord(x);
	}
	
	@Override
	public final int getSliceY() {
		return BaseWorld.sliceCoordFromTileCoord(y);
	}
	
}
