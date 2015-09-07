package com.stabilise.util;

/**
 * A function which receives an object and an integer as an input, and outputs
 * another object.
 *
 * @param <I> The input object type.
 * @param <O> The output object type.
 */
@FunctionalInterface
public interface ObjectIntFunction<I, O> {
    
    /**
     * Applies the function.
     * 
     * @param i1 The object input.
     * @param i2 The integer input.
     * 
     * @return The output.
     */
    O apply(I i1, int i2);
    
}
