package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.world.World;


/**
 * Skeletal implementation of {@link Component}. Importantly, this provides an
 * override for {@link Object#equals(Object)} to be for better duplicate
 * resolution.
 */
public abstract class AbstractComponent implements Component {
    
    /**
     * {@inheritDoc}
     * 
     * <p>The default implementation returns {@code false}.
     */
    @Override
    public boolean shouldRemove() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>The default implementation returns {@code false}.
     */
    public boolean handle(World w, Entity e, EntityEvent ev) {
        return false;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>The default implementation returns {@code 0}.
     */
    @Override
    public int getWeight() {
        return 0;
    }
    
    // We don't override resolve(Component) as to force implementors to think
    // about how to resolve duplicates.
    
    
    /**
     * This implementation returns {@code true} if the given object is of the
     * same class as this one.
     */
    @Override
    public boolean equals(Object o) {
        // Don't check for if o is null; WeightingArrayList ensures we don't
        // need to worry about this.
        return getClass().equals(o.getClass());
    }
    
}
