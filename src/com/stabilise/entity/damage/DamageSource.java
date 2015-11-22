package com.stabilise.entity.damage;

import com.stabilise.entity.Entity;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;

/**
 * Represents a source of damage.
 */
public class DamageSource {
    
    public int damage;
    /** The ID of the entity dealing the damage. -1 if not sourced from an
     * entity. */
    public long srcID;
    /** Force to apply upon impact. */
    public Vec2 force;
    
    
    public DamageSource() {
        this(0);
    }
    
    public DamageSource(int damage) {
        this.damage = damage;
        this.force = Maths.VEC_ZERO;
        this.srcID = -1;
    }
    
    public DamageSource(int damage, long srcID, Vec2 force) {
        this.damage = damage;
        this.srcID = srcID;
        this.force = force;
    }
    
    public DamageSource(int damage, long srcID, float fx, float fy) {
        this(damage, srcID, Vec2.immutable(fx, fy));
    }
    
    /**
     * Applies any effects to the entity.
     * 
     * <p>The default implementation does nothing.
     */
    public void applyEffects(Entity e) {
        // do nothing
    }
    
    public float forceX() {
        return force.x();
    }
    
    public float forceY() {
        return force.y();
    }
    
}
