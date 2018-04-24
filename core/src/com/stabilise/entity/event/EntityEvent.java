package com.stabilise.entity.event;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.Component;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.world.World;


public class EntityEvent extends Event {
    
    /**
     * An identifying type for an event; enums can be switched for efficient
     * handler code.
     */
    public static enum Type {
    	/** Posted when the entity is first added to the world. */
        ADDED_TO_WORLD,
        /** Posted immediately before an entity is removed from the world. This
         * happens after an entity is destroyed. */
        REMOVED_FROM_WORLD,
        /** Posted when {@link Entity#destroy() destroy()} is invoked on an
         * entity. Because destroy() doesn't accept any arguments, the World
         * object in {@link Component#handle(World, Entity, EntityEvent)
         * Component.handle()} will be {@code null}, so watch out. */
        DESTROYED,
        /** Horizontal tile collision. */
        TILE_COLLISION_H,
        /** Vertical tile collision. */
        TILE_COLLISION_V,
        DAMAGED,
        KILLED,
    }
    
    public static final EntityEvent
            ADDED_TO_WORLD     = new EntityEvent(Type.ADDED_TO_WORLD),
            REMOVED_FROM_WORLD = new EntityEvent(Type.REMOVED_FROM_WORLD),
            DESTROYED          = new EntityEvent(Type.DESTROYED);
    
    private final Type type;
    
    protected EntityEvent(Type type) {
        super(type.name());
        this.type = type;
    }
    
    public Type type() {
        return type;
    }
    
}
