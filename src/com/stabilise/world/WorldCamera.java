package com.stabilise.world;

import com.stabilise.entity.Entity;


public interface WorldCamera {
    
    /**
     * Sets the entity upon which to focus the camera. If {@code e} is null,
     * the camera will freeze.
     */
    void setFocus(Entity e);
    
    /**
     * Adds a shake effect to the camera.
     */
    void shake(float strength, int duration);
    
}
