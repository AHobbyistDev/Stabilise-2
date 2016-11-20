package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.collect.IDuplicateResolver;
import com.stabilise.util.collect.IWeightProvider;
import com.stabilise.world.World;


/**
 * An entity component. There are three primary component categories:
 * 
 * <ul>
 * <li>{@link CPhysics} - physics component.
 * <li>{@link CController} - entity controller.
 * <li>{@link CCore} - entity core.
 * </ul>
 * 
 * <p>Additionally, any number of miscellaneous components may be added to an
 * entity.
 */
public interface Component extends IWeightProvider, IDuplicateResolver<Component> {
    
    /**
     * Initialises this component. Invoked when added to the entity.
     */
    void init(Entity e);
    
    /**
     * Updates this component.
     */
    void update(World w, Entity e);
    
    /**
     * Checks for whether or not this component should be removed.
     * 
     * <p>The default implementation returns {@code false}.
     */
    default boolean shouldRemove() {
        return false;
    }
    
    /**
     * Handles an entity-local event broadcast.
     * 
     * <p>The default implementation returns {@code false}.
     * 
     * @return {@code true} to consume the event and prevent it from being
     * passed on to more components; {@code false} to not treat the event as
     * consumed.
     */
    default boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
    }
    
    @Override
    default int getWeight() {
        return 0;
    }
    
    @Override
    default Action resolve(Component c) {
        return Action.KEEP_BOTH;
    }
    
}
