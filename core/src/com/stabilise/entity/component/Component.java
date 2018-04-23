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
 * An entity component. Components provide essentially all functionality for
 * entities, including behaviour, effects, buffs, background technical
 * conveniences, etc. Any number of components may be added to or removed from
 * an entity on the fly, which is extremely useful.
 * 
 * <p>In addition to the assortment of ad hoc components, each entity is
 * equipped with three privileged components: a {@link CCore core} component, a
 * {@link CController controller} component, and a {@link CPhysics physics}
 * component.
 */
public interface Component extends IWeightProvider, IDuplicateResolver<Component> {
    
    /**
     * Initialises this component. Invoked when {@link
     * Entity#addComponent(Component) added to an entity}, immediately before
     * being added to the entity's list of components. If this is one of the
     * entity's priviliged three components, this is invoked by the entity's
     * constructor.
     */
    void init(Entity e);
    
    /**
     * Updates this component.
     * 
     * @param w The world.
     * @param e The entity this component is being updated as part of.
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
    
    /**
     * Returns the "weight" of this component. The list of ad hoc components on
     * an entity is ordered from lowest weight to highest weight. It isn't
     * usually necessary to override this unless there's a particular need for
     * a component to come before/after some other components when iterating or
     * {@link #handle(World, Entity, EntityEvent) handling events}.
     * 
     * <p>The default implementation returns 0.
     */
    @Override
    default int getWeight() {
        return 0;
    }
    
    /**
     * This is invoked on a component in an entity's list of ad hoc components
     * when a new component that is {@link Object#equals(Object) equal} to it
     * is added. See the {@link IDuplicateResolver#resolve(Object) parent
     * documentation} for more information.
     * 
     * <p>The chosen resolution policy obviously depends on what is deemed to
     * be most appropriate for a given component. Note that side-effects for
     * this method are permitted, and in fact encouraged. For example, consider
     * a component implementing an "on-fire" effect for an entity. If we
     * attempt to add another such "on-fire" component, the component already
     * present may wish to increase its duration correspondingly, and then
     * reject the new component.
     */
    @Override
    Action resolve(Component c);
    
}
