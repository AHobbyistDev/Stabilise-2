package com.stabilise.entity.component;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.PositionFree;
import com.stabilise.entity.event.EThroughPortalIntra;
import com.stabilise.util.Checks;
import com.stabilise.util.collect.SimpleList;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * The GameCamera controls the player's perspective, and hence which parts of
 * the world are visible to them. Also keeps track of the player and the world
 * the player is in for rendering and control purposes.
 */
public class CCamera extends CEntityTracker {
    
    /** Position of the camera. */
    public final PositionFree pos = Position.create();
    /** Real position (i.e. ignoring shake). */
    public final PositionFree realPos = Position.create();
    
    /** The strength with which the camera follows the focus. */
    private float followStrength = 1.0f; //0.25f;
    
    private final SimpleList<Shake> shakes = new UnorderedArrayList<>();
    
    
    // TODO: TEMPORARY!!
    public Vector2 upVector;
    
    
    
    @Override
    public void init(Entity e) {
        super.init(e);
        
        realPos.set(e.pos);
        realPos.ly += e.aabb.centreY();
        realPos.align();
        
        // TODO: TEMPORARY
        upVector = e.upDirection;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        super.update(w, e, dt);
        
        realPos.lx += realPos.diffX(e.pos) * followStrength;
        realPos.ly += (realPos.diffY(e.pos) + e.aabb.centreY()) * followStrength;
        realPos.align();
        
        pos.set(realPos);
        
        shakes.iterate(s -> {
            float mod = (float) s.duration / s.maxDuration;
            pos.addX(s.strength * (2 * w.rnd().nextFloat() - 1) * mod);
            pos.addY(s.strength * (2 * w.rnd().nextFloat() - 1) * mod);
            return --s.duration == 0;
        });
        
        pos.align();
    }
    
    @Override
    protected void handleThroughPortal(World w, Entity e, EThroughPortalIntra ev) {
        super.handleThroughPortal(w, e, ev);
        
        pos.add(ev.portalCore.offset).align();
        realPos.add(ev.portalCore.offset).align();
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
     * Moves the camera to the same coordinates as the entity.
     */
    /*
    public void snapToFocus(Entity e) {
        realPos.set(e.pos);
        realPos.ly += e.aabb.centreY();
        realPos.align();
    }
    */
    
    /**
     * Adds a shake effect to the camera.
     */
    public void shake(float strength, int duration) {
        shakes.append(new Shake(strength, duration));
    }
    
    @Override
    public int getWeight() {
        return Component.WEIGHT_CAMERA;
    }
    
    @Override
    public Action resolve(Component c) {
        return Action.REJECT; // only one camera!
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        // nothing to do
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        // nothing to do
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
