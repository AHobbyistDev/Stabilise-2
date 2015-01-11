package com.stabilise.opengl.render.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A model is a rig or set of animations for a game object, such as a player.
 */
public abstract class Model {
	
	
	/**
	 * Creates a new Model.
	 */
	public Model() {
		// nothing to see here, move along
	}
	
	/**
	 * Scales the model.
	 * 
	 * @param pixelsPerTile The number of pixels per tile.
	 */
	public abstract void rescale(float pixelsPerTile);
	
	/**
	 * Renders the model.
	 * 
	 * @param batch The batch with which to render; already started.
	 * @param x The x-coordinate at which to render the model, in pixels.
	 * @param y The y-coordinate at which to render the model, in pixels.
	 */
	public abstract void render(SpriteBatch batch, int x, int y);
	
	/**
	 * Destroys the model and unloads any resources it is holding.
	 */
	public abstract void dispose();
	
}
