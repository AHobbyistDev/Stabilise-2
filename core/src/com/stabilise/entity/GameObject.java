package com.stabilise.entity;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A GameObject is an object which exists within the game world. GameObjects
 * may be updated every tick, rendered if appropriate, and destroyed.
 */
public abstract class GameObject {
    
    /** If {@code true}, this GameObject should be removed from the world ASAP. */
    protected boolean destroyed = false;
    
    /** The position of this GameObject. Initialises to (0,0). */
    public final Position pos = Position.create();
    
    
    
    /**
     * Updates this GameObject.
     * 
     * @param world The world in which this GameObject is present. Never null.
     */
    public void update(World world) {
        // do nothing
    }
    
    /**
     * Updates this GameObject, and then returns {@link #isDestroyed()}.
     * 
     * <p>This method performs as if by:
     * 
     * <pre>
     * update(world);
     * return isDestroyed();</pre>
     * 
     * @param world The world in which this GameObject is present. Never null.
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
    public boolean isDestroyed() {
        return destroyed;
    }
    
    
}