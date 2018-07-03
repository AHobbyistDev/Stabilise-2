package com.stabilise.entity.component.effect;

import com.stabilise.entity.Entity;
import com.stabilise.entity.component.AbstractComponent;
import com.stabilise.world.World;

/**
 * An Effect is a lingering condition which can affect a mob.
 */
public abstract class CEffect extends AbstractComponent {
    
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
    protected CEffect(int duration) {
        if(duration < 1)
            throw new IllegalArgumentException("The duration of an effect must be a positive number!");
        
        this.duration = duration;
    }
    
    @Override
    public void update(World w, Entity e, float dt) {
        age++;
    }
    
    @Override
    public boolean shouldRemove() {
        return age >= duration;
    }
    
}
