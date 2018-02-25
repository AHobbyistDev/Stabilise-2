package com.stabilise.entity.event;

import com.stabilise.entity.damage.IDamageSource;


public class EDamaged extends EntityEvent {
    
    public final IDamageSource src;
    
    private EDamaged(Type type, IDamageSource src) {
        super(type);
        this.src = src;
    }
    
    public static EDamaged damaged(IDamageSource src) {
        return new EDamaged(Type.DAMAGED, src);
    }
    
    public static EDamaged killed(IDamageSource src) {
        return new EDamaged(Type.KILLED, src);
    }
    
}
