package com.stabilise.entity.event;

import com.stabilise.util.concurrent.event.Event;


public class EntityEvent extends Event {
    
    /**
     * An identifying type for an event; enums can be switched for efficient
     * handler code.
     */
    public static enum Type {
        ADDED_TO_WORLD,
        /** Horizontal tile collision. */
        TILE_COLLISION_H,
        /** Vertical tile collision. */
        TILE_COLLISION_V,
        DAMAGED,
        KILLED,
        DESTROYED;
    }
    
    public static final EntityEvent
            ADDED_TO_WORLD = new EntityEvent(Type.ADDED_TO_WORLD),
            DESTROYED      = new EntityEvent(Type.DESTROYED);
    
    private final Type type;
    
    protected EntityEvent(Type type) {
        super(type.name());
        this.type = type;
    }
    
    public Type type() {
        return type;
    }
    
}
