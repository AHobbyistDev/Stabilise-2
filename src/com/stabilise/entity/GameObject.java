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
    
    /** If {@code true}, this GameObject should be removed from the world ASAP. */
    protected boolean destroyed = false;
    
    
    /**
     * Updates this GameObject.
     * 
     * @param world The world.
     */
    public abstract void update(World world);
    
    /**
     * Updates this GameObject, and then returns {@link #isDestroyed()}.
     * 
     * <p>This method performs as if by:
     * 
     * <pre>
     * update(world);
     * return isDestroyed();</pre>
     * 
     * @param world The world.
     * 
     * @return {@code true} if this GameObject is considered destroyed and
     * should be removed from the world ASAP; {@code false} otherwise.
     */
    public boolean updateAndCheck(World world) {
        if(isDestroyed())
            return true;
        update(world);
        return isDestroyed();
    }
    
    /**
     * @param renderer The renderer with which to render the GameObject. Never
     * null.
     */
    public abstract void render(WorldRenderer renderer);
    
    /**
     * Destroys this GameObject.
     * 
     * <p>Invoking this method guarantees that this GameObject will be removed
     * from the world ASAP (either during the current update tick, or during
     * the next one).
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
     * @return The x-coordinate of this GameObject, in tile-lengths.
     */
    public abstract double getX();
    
    /**
     * @return The y-coordinate of this GameObject, in tile-lengths.
     */
    public abstract double getY();
    
    /**
     * @return The x-coordinate of the tile this GameObject is in, in
     * tile-lengths.
     */
    public abstract int getTileX();
    
    /**
     * @return The y-coordinate of the tile this GameObject is in, in
     * tile-lengths.
     */
    public abstract int getTileY();
    
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
