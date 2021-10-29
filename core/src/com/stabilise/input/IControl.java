package com.stabilise.input;

/**
 * Common interface to be implemented by all control enums to be utilised
 * through a {@link Controller}.
 */
public interface IControl {
    
    /**
     * Returns the identifying name of the control. This should be unique!
     */
    // Since all controls extend Enum, better call it this rather than getName()
    // to avoid confusion with name() defined for all enums.
    String identifier();
    
    /**
     * Gets the default mapping for this control.
     */
    KeyMapping defaultMapping();
    
    /**
     * Returns {@code true} if this control is enabled. An enabled control is
     * just a regular control; a disabled control is hidden, cannot be bound,
     * and cannot be triggered, as if it does not exist. This is used to
     * enable/disable debug controls for development versions of the game.
     */
    boolean isEnabled();
    

    
}
