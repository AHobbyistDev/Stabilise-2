package com.stabilise.entity.event;

import com.stabilise.entity.hitbox.Hitbox;


public class ELinkedHitboxCollision extends EntityEvent {
    
    /** Number of {@link Hitbox#hits hits} left on the hitbox. In particular, a
     * value of 0 means the hitbox is to be destroyed. */
    public int hitsRemaining;
    
    
    public ELinkedHitboxCollision(int hitsRemaining) {
        super(Type.HITBOX_COLLISION);
        this.hitsRemaining = hitsRemaining;
    }
    
}
