package com.stabilise.input;

import com.badlogic.gdx.InputMultiplexer;

/**
 * Defines the methods required by any component capable of receiving
 * configurable input from a Controller.
 * 
 * @see Controller
 */
public interface Controllable {
    
    /**
     * Called when a key corresponding to the specified control is pressed.
     * 
     * @param control The control pressed.
     * 
     * @return {@code true} if the control was processed. See {@link
     * InputMultiplexer}.
     */
    boolean handleControlPress(Controller.Control control);
    
    /**
     * Called when a key corresponding to the specified control is released.
     * 
     * @param control The control released.
     * 
     * @return {@code true} if the control was processed. See {@link
     * InputMultiplexer}.
     */
    boolean handleControlRelease(Controller.Control control);
    
}
