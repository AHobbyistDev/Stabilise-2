package com.stabilise.entity;

import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Checks;
import com.stabilise.util.collect.SimpleList;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.world.World;

/**
 * The GameCamera controls the player's perspective, and hence which parts of
 * the world are visible to them.
 */
public class GameCamera extends GameObject {
    
    /** The entity upon which to focus the camera. */
    private Entity focus;
    
    /** Real position (i.e. ignoring shake). */
    public final PositionFree realPos = Position.create();
    
    /** The strength with which the camera follows the focus. */
    private float followStrength = 0.25f;
    
    private final SimpleList<Shake> shakes = new UnorderedArrayList<>();
    
    
    /**
     * Creates a new GameCamera.
     */
    public GameCamera() {
        super(true);
        setFocus(null);
    }
    
    @Override
    public void update(World w) {
        if(focus != null) {
            realPos.lx += realPos.diffX(focus.pos) * followStrength;
            realPos.ly += (realPos.diffY(focus.pos) + focus.aabb.centreY()) * followStrength;
        }
        
        realPos.align();
        pos.set(realPos);
        
        shakes.iterate(s -> {
            float mod = (float) s.duration / s.maxDuration;
            pos.addX(s.strength * (2 * w.rnd().nextFloat() - 1) * mod);
            pos.addY(s.strength * (2 * w.rnd().nextFloat() - 1) * mod);
            return --s.duration == 0;
        });
        
        pos.align();
        
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
    
    /**
     * Sets the entity upon which to focus the camera. If {@code e} is null,
     * the camera will freeze.
     */
    public void setFocus(Entity e) {
        focus = e;
        
        if(e != null) {
            realPos.set(e.pos);
            realPos.ly += e.aabb.centreY();
            realPos.align();
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
        this.followStrength = Checks.testExclIncl(followStrength, 0f, 1f);
    }
    
    /**
     * Moves the camera to the same coordinates as its focus.
     */
    public void snapToFocus() {
        realPos.set(focus.pos);
        realPos.ly += focus.aabb.centreY();
        realPos.align();
    }
    
    /**
     * Adds a shake effect to the camera.
     */
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
