package com.stabilise.render.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * A model is a rig or set of animations for a game object, such as a player.
 */
public abstract class Model implements Disposable {
    
    /**
     * Creates a new Model.
     */
    public Model() {
        // nothing to see here, move along
    }
    
    /**
     * Renders the model.
     * 
     * @param batch The batch with which to render; already started.
     * @param x The x-coordinate at which to render the model.
     * @param y The y-coordinate at which to render the model.
     */
    public abstract void render(SpriteBatch batch, float x, float y);
    
    public abstract void dispose();
    
}
