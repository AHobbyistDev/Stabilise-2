package com.stabilise.entity.damage;

import com.stabilise.entity.Entity;

/**
 * Represents a source of damage.
 */
public interface IDamageSource {
    
    /**
     * Returns the amount of damage to deal.
     */
    int damage();
    
    /**
     * Sets the damage of this source. This method is provided for handlers
     * to intercept this source and modify the damage before applying it.
     * 
     * <p>The default implementation does not update the damage.
     * 
     * @return This source.
     */
    default IDamageSource setDamage(int damage) {
        return this;
    }
    
    /**
     * Returns the damage type.
     */
    DamageType type();
    
    /**
     * Returns the ID of the entity which is inflicting the damage, or -1 if
     * the damage is from a non-mob source.
     * 
     * <p>The default implementation returns -1.
     */
    default long sourceID() {
        return -1;
    }
    
    /**
     * Applies any desired effects to the given entity.
     * 
     * <p>The default implementation does nothing.
     */
    default void applyEffects(Entity e) {}
    
    /**
     * Returns true if this damage source grants invincibility frames; false
     * if not.
     */
    boolean invincibilityFrames();
    
    /**
     * Returns the impulse along the x-direction to apply upon dealing damage.
     * 
     * <p>The default implementation returns 0.
     */
    default float impulseX() {
        return 0f;
    }
    
    /**
     * Returns the impulse along the y-direction to apply upon dealing damage.
     * 
     * <p>The default implementation returns 0.
     */
    default float impulseY() {
        return 0f;
    }
    
    /**
     * Clones this source if this is the original; returns this source if it is
     * already a clone.
     */
    IDamageSource clone();
    
}
