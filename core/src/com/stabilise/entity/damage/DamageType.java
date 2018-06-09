package com.stabilise.entity.damage;


public enum DamageType {
    
    ATTACK(false), INERTIA(true), FIRE(false), VOID(true);
    
    public final boolean bypassesInvulFrames;
    
    DamageType(boolean bypassesInvulFrames) {
        this.bypassesInvulFrames = bypassesInvulFrames;
    }
    
}
