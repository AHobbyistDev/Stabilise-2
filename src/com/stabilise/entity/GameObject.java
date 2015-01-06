package com.stabilise.entity;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A GameObject is an object which exists within the game world. GameObjects
 * may be updated every tick, rendered if appropriate, and destroyed.
 * 
 * <p>A GameObject also possesses x and y coordinates; for these, see {@link
 * FixedGameObject} and {@link FreeGameObject}.
 */
public abstract class GameObject {
	
	/** A reference to the world the GameObject is in. */
	public World world;
	/** The GameObject's ID. Such an ID is not necessarily unique amongst all
	 * GameObjects; merely ones of the same type amongst which distinction is
	 * required (e.g. entities). */
	public int id;
	
	/** If {@code true}, this GameObject should be removed from the world ASAP. */
	protected boolean destroyed = false;
	
	
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
	 * Updates this GameObject.
	 */
	public abstract void update();
	
	/**
	 * Updates this GameObject, and then returns {@link #isDestroyed()}.
	 * 
	 * <p>This method performs as if by:
	 * 
	 * <pre>
	 * update();
	 * return isDestroyed();</pre>
	 */
	public final boolean updateAndCheck() {
		update();
		return isDestroyed();
	}
	
	/**
	 * @param renderer The renderer with which to render the GameObject.
	 */
	public abstract void render(WorldRenderer renderer);
	
	/**
	 * Destroys this GameObject.
	 * 
	 * <p>In the default implementation, this sets the {@link #destroyed} flag
	 * to {@code true}, and {@link #isDestroyed()} will return {@code true}
	 * henceforth.
	 */
	public void destroy() {
		destroyed = true;
	}
	
	/**
	 * If {@code true} is returned, this GameObject should be removed from the
	 * world ASAP.
	 * 
	 * @return {@code true} if this GameObject is considered destroyed; {@code
	 * false} otherwise.
	 */
	public boolean isDestroyed() {
		return destroyed;
	}
	
	/**
	 * Gets the x-coordinate of the slice the game object is within.
	 * 
	 * @return The x-coordinate of the slice, in slice-lengths.
	 */
	public abstract int getSliceX();
	
	/**
	 * Gets the y-coordinate of the slice the game object is within.
	 * 
	 * @return The y-coordinate of the slice, in slice-lengths.
	 */
	public abstract int getSliceY();
	
}
