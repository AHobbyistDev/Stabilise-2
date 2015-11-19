package com.stabilise.entity.event;

import com.stabilise.util.concurrent.event.Event;


public class EntityEvent extends Event {
    
    public static enum Type {
        ADDED_TO_WORLD,
        TILE_COLLISION,
        TILE_COLLISION_H,
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
