package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
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
     * Initialises this component.
     */
    void init(World w, Entity e);
    
    /**
     * Updates this component.
     */
    void update(World w, Entity e);
    
    /**
     * Handles an entity-local event broadcast.
     */
    void handle(World w, Entity e, ComponentEvent ev);
    
}
