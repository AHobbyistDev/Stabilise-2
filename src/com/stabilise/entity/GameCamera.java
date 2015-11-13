package com.stabilise.entity;

import java.util.Objects;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * The GameCamera controls the player's perspective, and hence which parts of
 * the world are visible to them.
 */
public class GameCamera extends FreeGameObject {
    
    /** The entity upon which to focus the camera. */
    private Entity focus;
    
    /** The number of tiles to view horizontally/vertically. */
    public int width, height;
    
    /** The coordinates of the slice in which the camera is located, in
     * slice-lengths. These are cached values recalculated every tick. */
    public int sliceX, sliceY;
    
    /** The strength with which the camera follows the focus. */
    private float followStrength = 0.25f;
    
    
    /**
     * Creates a new GameCamera.
     * 
     * @param provider The game world.
     * @param focus The entity upon which to focus the camera.
     */
    public GameCamera(Entity focus) {
        setFocus(focus);
    }
    
    @Override
    public void update(World world) {
        x += (focus.x - x) * followStrength;
        y += (focus.y + 1 - y) * followStrength;
        
        sliceX = getSliceX();
        sliceY = getSliceY();
        
        // Unimportant TODOs:
        // Focus on multiple entities
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
     * @throws NullPointerException if {@code e} is {@code null}.
     */
    public void setFocus(Entity e) {
        focus = Objects.requireNonNull(e);
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
            throw new IllegalArgumentException("The follow strength must be within (0,1]!");
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
