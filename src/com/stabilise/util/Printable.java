package com.stabilise.util;

/**
 * Defines a convenience method, {@link #print()}, with a default
 * implementation, all in the name of making life easier.
 */
public interface Printable {
    
    /**
     * Prints this object to standard out.
     */
    default void print() {
        System.out.println(toString());
    }
    
}
