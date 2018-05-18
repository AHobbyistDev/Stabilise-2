package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.controller.CController;
import com.stabilise.entity.component.core.CCore;
import com.stabilise.entity.component.physics.CPhysics;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.collect.IDuplicateResolver;
import com.stabilise.util.collect.IWeightProvider;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.Exportable;
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
 * 
 * <p>Every implementor of this interface needs to have a parameterless
 * constructor so that it may be instantiated dynamically e.g. when an entity
 * is loaded from a save file.
 */
public interface Component extends IWeightProvider,
                                    IDuplicateResolver<Component>,
                                    Exportable {
    
    /**
     * Initialises this component. Invoked when {@link
     * Entity#addComponent(Component) added to an entity}, immediately
     * <em>after</em> being added to the entity's list of components (that is,
     * only if this component was successfully added, and was not rejected).
     * If this is one of the entity's priviliged three components, this is
     * invoked by the entity's constructor.
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
     */
    boolean shouldRemove();
    
    /**
     * Handles an entity-local event broadcast.
     * 
     * @return {@code true} to consume the event and prevent it from being
     * passed on to more components; {@code false} to not treat the event as
     * consumed.
     */
    boolean handle(World w, Entity e, EntityEvent ev);
    
    /**
     * Returns the "weight" of this component. The list of ad hoc components on
     * an entity is ordered from lowest weight to highest weight. It isn't
     * usually necessary to override this unless there's a particular need for
     * a component to come before/after some other components when iterating or
     * {@link #handle(World, Entity, EntityEvent) handling events}.
     */
    @Override
    int getWeight();
    
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
    
    
    /**
     * {@inheritDoc}
     * 
     * <p>In general it is not a good idea to use this directly -- use {@link
     * Component#fromCompound(DataCompound)} instead.
     */
    @Override
    void importFromCompound(DataCompound c);
    
    /**
     * {@inheritDoc}
     * 
     * <p>In general it is not a good idea to use this directly since this
     * method does not export this component's id -- use {@link
     * Component#toCompound(DataCompound, Component)} instead.
     */
    void exportToCompound(DataCompound c);
    
    
    
    /**
     * Reads a Component from the given DataCompound. The given DataCompound
     * should contain an integer tag "id".
     * 
     * @return the Component read from the compound, or {@code null} if the id
     * contained in the compoud was invalid.
     * 
     * @see #toCompound(DataCompound, Component)
     */
    public static Component fromCompound(DataCompound dc) {
        int id = dc.getI32("id");
        Component c = Components.COMPONENT_TYPES.create(id);
        if(c == null)
            return null;
        c.importFromCompound(dc);
        return c;
    }
    
    /**
     * Exports a Component to a DataCompound.
     * 
     * @param dc the compound to export to
     * @param c the component to export
     * 
     * @throws NullPointerException if either argument is null
     * @see #fromCompound(DataCompound)
     */
    public static void toCompound(DataCompound dc, Component c) {
        int id = Components.COMPONENT_TYPES.getID(c.getClass());
        dc.put("id", id);
        dc.put(c);
    }
    
}
