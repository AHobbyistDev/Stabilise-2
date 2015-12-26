package com.stabilise.entity;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.collect.SimpleList;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.world.World;
import com.stabilise.world.WorldCamera;

/**
 * The GameCamera controls the player's perspective, and hence which parts of
 * the world are visible to them.
 */
public class GameCamera extends FreeGameObject implements WorldCamera {
    
    /** The entity upon which to focus the camera. */
    private Entity focus;
    
    /** Real x, y values (i.e. ignoring shake). */
    private double rx, ry;
    
    /** The number of tiles to view horizontally/vertically. */
    public int width, height;
    
    /** The coordinates of the slice in which the camera is located, in
     * slice-lengths. These are cached values recalculated every tick. */
    public int sliceX, sliceY;
    
    /** The strength with which the camera follows the focus. */
    private float followStrength = 0.25f;
    
    private final SimpleList<Shake> shakes = new UnorderedArrayList<>();
    
    
    /**
     * Creates a new GameCamera.
     */
    public GameCamera() {
        setFocus(null);
        rx = x;
        ry = y;
    }
    
    @Override
    public void update(World w) {
        if(focus != null) {
            rx += (focus.x - rx) * followStrength;
            ry += (focus.y + focus.aabb.centreY() - ry) * followStrength;
        }
        
        x = rx;
        y = ry;
        
        shakes.iterate(s -> {
            float mod = (float) s.duration / s.maxDuration;
            x += s.strength * (2 * w.getRnd().nextFloat() - 1) * mod;
            y += s.strength * (2 * w.getRnd().nextFloat() - 1) * mod;
            return --s.duration == 0;
        });
        
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
        focus = null; // help the gc
    }
    
    @Override
    public void setFocus(Entity e) {
        focus = e;
        
        if(e != null) {
            rx = e.x;
            ry = e.y + e.aabb.centreY();
            sliceX = getSliceX();
            sliceY = getSliceY();
        }
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
        this.followStrength = Checks.testExclIncl(followStrength, 0f, 1);
    }
    
    /**
     * Moves the camera to the same coordinates as its focus.
     */
    public void snapToFocus() {
        rx = focus.x;
        ry = focus.y + focus.aabb.centreY();
    }
    
    @Override
    public void shake(float strength, int duration) {
        shakes.append(new Shake(strength, duration));
    }
    
    private static class Shake {
        
        public float strength;
        public int duration;
        public int maxDuration;
        
        public Shake(float strength, int duration) {
            this.strength = strength;
            this.duration = duration;
            maxDuration = duration;
        }
        
    }
    
}
