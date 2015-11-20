package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.world.World;


/**
 * An entity component. There are three primary component categories:
 * 
 * <ul>
 * <li>{@link CPhysics} - physics component.
 * <li>{@link CController} - entity controller.
 * <li>{@link CCore} - entity core.
 * </ul>
 */
public interface Component {
    
    /**
     * Initialises this component. Invoked when added to the entity.
     */
    void init(Entity e);
    
    /**
     * Updates this component.
     */
    void update(World w, Entity e);
    
    /**
     * Handles an entity-local event broadcast.
     * 
     * @return {@code true} to consume the event and prevent it from being
     * passed on to more components; {@code false} to not treat the event as
     * consumed.
     */
    boolean handle(World w, Entity e, EntityEvent ev);
    
}
