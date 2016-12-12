package com.stabilise.util.maths;

/**
 * This interface defines a generator of noise in both 1D and 2D space.
 */
public interface INoise {
    
    /**
     * Gets the noise value at x, between 0 and 1.
     */
    float noise(double x);
    
    /**
     * Gets the noise value at (x, y), between 0 and 1.
     */
    float noise(double x, double y);
    
}
