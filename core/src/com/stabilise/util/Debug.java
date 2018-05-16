package com.stabilise.util;

/**
 * Contains a big fat global DEBUG variable for when I'm in the mood to do some
 * really crude debugging that I want to be able to enable/disable while the
 * program is running.
 */
public class Debug {
    
    
    public static volatile boolean DEBUG = false;
    
    
    
    private Debug() {} // non-instantiable
    
}
