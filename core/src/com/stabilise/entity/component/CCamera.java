package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.entity.PositionFree;
import com.stabilise.entity.event.EThroughPortal;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.entity.event.EntityEvent.Type;
import com.stabilise.util.Checks;
import com.stabilise.util.collect.SimpleList;
import com.stabilise.util.collect.UnorderedArrayList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.World;

/**
 * The GameCamera controls the player's perspective, and hence which parts of
 * the world are visible to them.
 */
public class CCamera extends AbstractComponent {
    
    /** Position of the camera. */
    public final PositionFree pos = Position.create();
    /** Real position (i.e. ignoring shake). */
    public final PositionFree realPos = Position.create();
    
    /** The strength with which the camera follows the focus. */
    private float followStrength = 0.25f;
    
    private final SimpleList<Shake> shakes = new UnorderedArrayList<>();
    
    /** The world that the entity -- and also this camera -- is in. Updated
     * when the entity moves dimensions via a portal. */
    public World world = null;
    
    
    
    @Override
    public void init(Entity e) {
        realPos.set(e.pos);
        realPos.ly += e.aabb.centreY();
        realPos.align();
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        if(world == null)
            world = w;
        
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
        
        // Unimportant TODOs:
        // Focus on multiple entities
        // Way of capping off-centre-ness
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
    public void snapToFocus(Entity e) {
        realPos.set(e.pos);
        realPos.ly += e.aabb.centreY();
        realPos.align();
    }
    
    /**
     * Adds a shake effect to the camera.
     */
    public void shake(float strength, int duration) {
        shakes.append(new Shake(strength, duration));
    }
    
    @Override
    public boolean handle(World w, Entity e, EntityEvent ev) {
        if(ev.type().equals(Type.THROUGH_PORTAL)) {
            EThroughPortal ev0 = (EThroughPortal) ev;
            pos.add(ev0.portalCore.offset).align();
            realPos.add(ev0.portalCore.offset).align();
            world = ev0.portalCore.pairedWorld(w);
        }
        
        return false;
    }
    
    @Override
    public int getWeight() {
        // not *that* important, but update the camera after everything else
        return Integer.MAX_VALUE;
    }
    
    @Override
    public Action resolve(Component c) {
        return Action.REJECT;
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        Checks.TODO(); // TODO
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        Checks.TODO(); // TODO
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
