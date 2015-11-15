package com.stabilise.entity.damage;

import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;


public class DamageSource {
    
    public int damage;
    public long srcID;
    public Vec2 force;
    
    
    public DamageSource() {}
    
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
    
}
