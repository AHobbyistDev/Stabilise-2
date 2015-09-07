package com.stabilise.util;

/**
 * This class provides simple and concise ways for testing indices and throwing
 * appropriate IndexOutOfBoundsExceptions or IllegalArgumentExceptions.
 */
public class Checks {
    
    private Checks() {} // non-instantiable
    
    /**
     * Tests a value.
     * 
     * @param index The index being tested.
     * @param min The minimum legal value (inclusive).
     * @param max The maximum legal value (inclusive).
     * 
     * @return index
     * @throws IndexOutOfBoundsException if {@code index < min || index > max}.
     */
    public static int testIndex(int index, int min, int max) {
        if(index < min || index > max)
            throw new IndexOutOfBoundsException("Illegal index " + index +
                    "; it should be in the range [" + min + ", " + max + "]");
        return index;
    }
    
    /**
     * Tests a value.
     * 
     * @param index The index being tested.
     * @param min The minimum legal value (inclusive).
     * 
     * @return index
     * @throws IndexOutOfBoundsException if {@code index < min}.
     */
    public static int testMinIndex(int index, int min) {
        if(index < min)
            throw new IndexOutOfBoundsException("Illegal index " + index +
                    "; it should not be less than " + min);
        return index;
    }
    
    /**
     * Tests a value.
     * 
     * @param index The index being tested.
     * @param max The maximum legal value (inclusive).
     * 
     * @return index
     * @throws IndexOutOfBoundsException if {@code index > max}.
     */
    public static int testMaxIndex(int index, int max) {
        if(index > max)
            throw new IndexOutOfBoundsException("Illegal index " + index +
                    "; it should not be greater than " + max);
        return index;
    }
    
    /**
     * Tests a value.
     * 
     * @param val The value being tested.
     * @param min The minimum legal value (inclusive).
     * @param max The maximum legal value (inclusive).
     * 
     * @return val
     * @throws IllegalArgumentException if {@code val < min || val > max}.
     */
    public static int test(int val, int min, int max) {
        if(val < min || val > max)
            throw new IllegalArgumentException("Illegal value " + val +
                    "; it should be in the range [" + min + ", " + max + "]");
        return val;
    }
    
    /**
     * Tests a value.
     * 
     * @param val The value being tested.
     * @param min The minimum legal value (inclusive).
     * 
     * @return val
     * @throws IllegalArgumentException if {@code val < min}.
     */
    public static int testMin(int val, int min) {
        if(val < min)
            throw new IllegalArgumentException("Illegal value " + val +
                    "; it should not be less than " + min);
        return val;
    }
    
    /**
     * Tests a value.
     * 
     * @param val The value being tested.
     * @param max The maximum legal value (inclusive).
     * 
     * @return val
     * @throws IllegalArgumentException if {@code val > max}.
     */
    public static int testMax(int val, int max) {
        if(val > max)
            throw new IllegalArgumentException("Illegal value " + val +
                    "; it should not be greater than " + max);
        return val;
    }
    
}
