package com.stabilise.entity.component.state;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.Component;
import com.stabilise.entity.damage.DamageSource;
import com.stabilise.entity.effect.Effect;
import com.stabilise.util.shape.AABB;
import com.stabilise.world.World;


public interface CState extends Component {
    
    /**
     * Returns the entity's AABB.
     */
    AABB getAABB();
    
    /**
     * Applies an effect to the entity.
     * 
     * <p>The default implementation does nothing.
     */
    default void applyEffect(Effect effect) {
        // do nothing
    }
    
    /**
     * Attempts to damage the entity.
     * 
     * <p>The default implementation does nothing and returns {@code false}.
     * 
     * @return true if the entity was damaged; {@code false} if not.
     */
    default boolean damage(World w, Entity e, DamageSource src) { 
        return false; 
    }
    
    /**
     * Kills the entity.
     * 
     * <p>The default implementation invokes {@code e.destroy()}.
     */
    default void kill(World w, Entity e, DamageSource src) {
        e.destroy();
    }
    
}
