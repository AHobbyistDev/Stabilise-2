package com.stabilise.entity.event;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.Component;
import com.stabilise.util.concurrent.event.Event;
import com.stabilise.world.World;


/**
 * An EntityEvent is a special type of event that is posted to an {@link
 * Entity} to notify its components of, allow them to modify, and perform
 * reactions in response to, actions effected upon it. Unlike the parent {@link
 * Event} class, entity events are not posted to an EventDispatcher, but are
 * rather bubbled through an entity's list of components.
 * 
 * @see Entity#post(World, EntityEvent)
 * @see Component#handle(World, Entity, EntityEvent)
 */
public class EntityEvent extends Event {
    
    /**
     * An identifying type for an event; enums can be 'switched' for efficient
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
        /** When an entity goes through a portal, either to the same dimension
         * or another dimension. */
        THROUGH_PORTAL,
        /** When an entity comes "in range" of a portal. */
        PORTAL_IN_RANGE,
        /** When an entity moves "out of range" of a portal. */
        PORTAL_OUT_OF_RANGE,
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
