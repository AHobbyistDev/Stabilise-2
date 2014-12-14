package com.stabilise.entity;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A GameObject is an object which exists within the game.
 */
public abstract class GameObject {
	
	/** A reference to the world the GameObject is in. */
	public World world;
	/** The GameObject's ID. */
	public int id;
	
	/** The GameObject's x-coordinate. */
	public double x;
	/** The GameObject's y-coordinate. */
	public double y;
	
	
	/**
	 * Creates a new GameObject.
	 */
	protected GameObject() {
		// nothing to see here, move along
	}
	
	/**
	 * Creates a new GameObject.
	 * 
	 * @param world The world.
	 */
	public GameObject(World world) {
		this.world = world;
	}
	
	/**
	 * Updates the GameObject.
	 */
	public abstract void update();
	
	/**
	 * Renders the GameObject.
	 * 
	 * @param renderer The renderer with which to render the GameObject.
	 */
	public abstract void render(WorldRenderer renderer);
	
	/**
	 * Destroys the GameObject.
	 */
	public abstract void destroy();
	
	/**
	 * Gets the x-coordinate of the slice the game object is within.
	 * 
	 * @return The x-coordinate of the slice, in slice-lengths.
	 */
	public final int getSliceX() {
		return World.sliceCoordFromTileCoord(x);
	}
	
	/**
	 * Gets the y-coordinate of the slice the game object is within.
	 * 
	 * @return The y-coordinate of the slice, in slice-lengths.
	 */
	public final int getSliceY() {
		return World.sliceCoordFromTileCoord(y);
	}

}
