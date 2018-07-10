package com.stabilise.entity.component;

import com.stabilise.entity.Entity;
import com.stabilise.entity.event.EntityEvent;
import com.stabilise.util.Checks;
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
     * The default implementation does nothing.
     */
    @Override
    public void init(Entity e) {
        
    }
    
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
     * <p>The default implementation returns {@link Component#WEIGHT_NORMAL}.
     */
    @Override
    public int getWeight() {
        return Component.WEIGHT_NORMAL;
    }
    
    // We don't override resolve(Component) as to force implementors to think
    // about how to resolve duplicates.
    
    
    /**
     * {@inheritDoc}
     * 
     * <p>The default implementation returns {@code false}.
     */
    @Override
    public boolean isTransient() {
        return false;
    }
    
    
    @Override
    public int hashCode() {
        throw Checks.badAssert("Hash code not designed");
    }
    
    /**
     * This implementation returns {@code true} if the given object is of the
     * same class as this one. This is a reasonable default for component
     * equality.
     */
    @Override
    public boolean equals(Object o) {
        // Don't check for if o is null; WeightingArrayList ensures we don't
        // need to worry about this.
        return getClass().equals(o.getClass());
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" +
                Integer.toHexString(System.identityHashCode(this));
    }
    
}
