package com.stabilise.util.maths;


public interface INoise2D {
    
    /**
     * Gets the noise value at (x, y), between 0 and 1.
     */
    double noise(double x, double y);
    
}
