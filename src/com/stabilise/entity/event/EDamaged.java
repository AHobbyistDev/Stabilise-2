package com.stabilise.entity.event;

import com.stabilise.entity.damage.DamageSource;


public class EDamaged extends EntityEvent {
    
    public final DamageSource src;
    
    private EDamaged(Type type, DamageSource src) {
        super(type);
        this.src = src;
    }
    
    public static EDamaged damaged(DamageSource src) {
        return new EDamaged(Type.DAMAGED, src);
    }
    
    public static EDamaged killed(DamageSource src) {
        return new EDamaged(Type.KILLED, src);
    }
    
}
