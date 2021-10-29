package com.stabilise.input;

import com.badlogic.gdx.InputMultiplexer;

/**
 * Defines the methods required by any component capable of receiving
 * configurable input from a Controller.
 * 
 * @see Controller
 */
public interface Controllable<T extends Enum<T> & IControl> {
    
    /**
     * Called when a key or button corresponding to the specified control is
     * pressed, or, if the control is bound to the scroll wheel, when the wheel
     * is scrolled. The {@code screenX} and {@code screenY} parameters give the
     * position of the mouse cursor at the time of the click if the control was
     * triggered by a mouse button press. The {@code amount} parameter gives the
     * scroll amount if the event was triggered by the scroll wheel.
     *
     * @param control The triggered control.
     * @param screenX The x-coordinate of the mouse on the screen if the control
     * was triggered by a mouse button click; 0 otherwise.
     * @param screenY The y-coordinate of the mouse on the screen if the control
     * was triggered by a mouse button click; 0 otherwise.
     * @param amount The scroll amount if the control was triggered by the
     * scroll wheel; 0 otherwise.
     * 
     * @return {@code true} if the control was processed. See {@link
     * InputMultiplexer}.
     */
    boolean handleControlPress(T control, int screenX, int screenY, float amount);
    
    /**
     * Called when a key or button corresponding to the specified control is
     * released. The {@code screenX} and {@code screenY} parameters give the
     * position of the mouse cursor at the time of the release if the control
     * was triggered by a mouse button press.
     *
     * @param control The triggered control.
     * @param screenX The x-coordinate of the mouse on the screen if the control
     * was triggered by a mouse button click; 0 otherwise.
     * @param screenY The y-coordinate of the mouse on the screen if the control
     * was triggered by a mouse button click; 0 otherwise.
     *
     * @return {@code true} if the control was processed. See {@link
     * InputMultiplexer}.
     */
    boolean handleControlRelease(T control, int screenX, int screenY);
    
}
