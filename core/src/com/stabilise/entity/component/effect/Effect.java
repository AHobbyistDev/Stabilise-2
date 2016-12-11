package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.Component;
import com.stabilise.world.World;

/**
 * An Effect is a lingering condition which can affect a mob.
 */
public abstract class Effect implements Component {
    
    /** The duration of the effect, in ticks. */
    public int duration;
    /** The age of the effect, in ticks. When this reaches {@link #duration},
     * this effect will be removed. */
    public int age = 0;
    
    
    /**
     * Creates a new Effect.
     * 
     * @param duration The duration of the effect, in ticks.
     */
    protected Effect(int duration) {
        if(duration < 1)
            throw new IllegalArgumentException("The duration of an effect must be a positive number!");
        
        this.duration = duration;
    }
    
    @Override
    public void update(World w, Entity e) {
        age++;
    }
    
    @Override
    public boolean shouldRemove() {
        return age >= duration;
    }
    
    // Get rid of the default implementation; we want to force effects to
    // handle overlapping ones.
    @Override
    public abstract Action resolve(Component c);
    
    @Override
    public boolean equals(Object o) {
        // Effects are considered equal if they're the same class.
        return o != null && o.getClass().equals(getClass());
    }
    
}
