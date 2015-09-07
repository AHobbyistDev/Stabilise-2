package com.stabilise.entity.effect;

import com.stabilise.entity.EntityMob;
import com.stabilise.world.World;

/**
 * An Effect is a lingering condition which can affect a mob.
 */
public abstract class Effect {
    
    /** The duration of the effect, in ticks. */
    public final int duration;
    /** The age of the effect, in ticks. */
    public int age = 0;
    
    /** Whether or not the effect is considered 'destroyed', or has worn off. */
    public boolean destroyed = false;
    
    
    /**
     * Creates a new Effect.
     * 
     * @param duration The duration of the effect.
     */
    protected Effect(int duration) {
        if(duration < 1)
            throw new IllegalArgumentException("The duration of an effect must be a positive number!");
        
        this.duration = duration;
    }
    
    /**
     * Updates the Effect.
     * 
     * @param world The world.
     * @param target The target of the Effect.
     */
    public void update(World world, EntityMob target) {
        if(++age == duration)
            destroyed = true;
    }
    
    /**
     * Clones the Effect.
     * 
     * @return The clone.
     */
    public abstract Effect clone();
    
}
