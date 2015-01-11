package com.stabilise.entity;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.ClientWorld;

/**
 * The GameCamera controls the player's perspective, and hence which parts of
 * the world are visible to them.
 */
public class GameCamera extends FreeGameObject {
	
	/** The entity upon which to focus the camera. */
	private Entity focus;
	
	/** The number of tiles to view horizontally. */
	public int width;
	/** The number of tiles to view vertically. */
	public int height;
	
	/** The x-coordinate of the slice in which the camera is located, in slice-lengths. */
	public int sliceX;
	/** The y-coordinate of the slice in which the camera is located, in slice-lengths. */
	public int sliceY;
	
	/** The strength with which the camera follows the focus. */
	private float followStrength = 0.25f;
	
	
	/**
	 * Creates a new GameCamera.
	 * 
	 * @param world The game world.
	 * @param focus The entity upon which to focus the camera.
	 */
	public GameCamera(ClientWorld<?> world, Entity focus) {
		super(world);
		setFocus(focus);
	}
	
	@Override
	public void update() {
		//x = focus.x;
		//y = focus.y;
		
		x += (focus.x - x) * followStrength;
		y += (focus.y + 1 - y) * followStrength;
		
		sliceX = getSliceX();
		sliceY = getSliceY();
		
		// Unimportant TODOs:
		// Focus on multiple entities
		// Non-jerky off-centre focusing
		// Function for capping off-centre-ness
	}
	
	@Override
	public void render(WorldRenderer renderer) {
		// nothing to see here, move along
	}
	
	@Override
	public void destroy() {
		// nothing to see here, move along
	}
	
	/**
	 * Sets the entity upon which to focus the camera.
	 * 
	 * @param e The entity.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code e} is {@code null}.
	 */
	public void setFocus(Entity e) {
		if(e == null)
			throw new IllegalArgumentException("The game camera's focus must be non-null!");
		
		focus = e;
		x = focus.x;
		y = focus.y + 1;
		sliceX = focus.getSliceX();
		sliceY = focus.getSliceY();
	}
	
	/**
	 * Sets the camera's follow strength. Every tick the camera will close the
	 * distance between it and its focus proportional to this amount.
	 * 
	 * @param followStrength The follow strength.
	 * 
	 * @throws IllegalArgumentException if {@code followStrength} is not within
	 * the range {@code 0 < followStrength <= 1}.
	 */
	public void setFollowStrength(float followStrength) {
		if(followStrength <= 0 || followStrength > 1)
			throw new IllegalArgumentException("The follow strength must be between 0 (exclusive) and 1 (inclusive)!");
		this.followStrength = followStrength;
	}
	
	/**
	 * Moves the camera to the same coordinates as its focus.
	 */
	public void snapToFocus() {
		x = focus.x;
		y = focus.y;
	}
	
}
