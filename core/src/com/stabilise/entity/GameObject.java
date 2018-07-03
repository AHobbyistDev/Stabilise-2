package com.stabilise.entity;

import com.stabilise.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A GameObject is an object which exists within the game world. GameObjects
 * may be updated every tick, rendered if appropriate, and destroyed.
 */
public abstract class GameObject {
    
    /** If {@code true}, this GameObject should be removed from the world ASAP. */
    protected boolean destroyed = false;
    
    /** The position of this GameObject. Initialised to (0,0). */
    public final Position pos;
    
    
    
    /**
     * Constructor.
     * 
     * @param free true to use a {@link PositionFree}; false to use a {@link
     * PositionFixed}.
     */
    protected GameObject(boolean free) {
        pos = free ? Position.create() : Position.createFixed();
    }
    
    /**
     * Updates this GameObject. This is only ever called through {@link
     * #updateAndCheck(World)}.
     * 
     * @param world The world in which this GameObject is present. Never null.
     * @param dt The number of seconds since the last update tick. Typically
     * equal to 1/ticksPerSecond.
     */
    protected void update(World world, float dt) {
        // do nothing
    }
    
    /**
     * Updates this GameObject, and then returns {@link #isDestroyed()}. If
     * this method returns true, this GameObject will be removed from the world
     * immediately afterwards.
     * 
     * <p>This method performs as if by:
     * 
     * <pre>
     * if(isDestroyed())
     *      return true;
     * update(world);
     * return isDestroyed();</pre>
     * 
     * @param world The world in which this GameObject is present. Never null.
     * @param dt The number of seconds since the last update tick. Typically
     * equal to 1/ticksPerSecond.
     * 
     * @return {@code true} if this GameObject is considered destroyed and
     * should be removed from the world ASAP; {@code false} otherwise.
     */
    public boolean updateAndCheck(World world, float dt) {
        if(!destroyed) // don't update if already destroyed!
            update(world, dt);
        return destroyed;
    }
    
    /**
     * @param renderer The renderer with which to render this GameObject. Never
     * null.
     */
    public void render(WorldRenderer renderer) {
        // do nothing
    }
    
    /**
     * Destroys this GameObject.
     * 
     * <p>Invoking this method guarantees that this GameObject will be removed
     * from the world ASAP (either during the current update tick, or during
     * the next one).
     * 
     * <p>The default implementation sets the {@link #destroyed} flag to {@code
     * true}; and thus {@link #isDestroyed()} will return {@code true}
     * henceforth.
     */
    public void destroy() {
        destroyed = true;
    }
    
    /**
     * Checks for whether or not this GameObject is considered destroyed. If
     * {@code true} is returned, this GameObject should be removed from the
     * world ASAP.
     */
    public final boolean isDestroyed() {
        return destroyed;
    }
    
    
}
