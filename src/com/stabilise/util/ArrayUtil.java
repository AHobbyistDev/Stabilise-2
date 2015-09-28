package com.stabilise.util;

import java.util.Objects;

/**
 * Array-related utility functions.
 */
public class ArrayUtil {
    
    // non-instantiable
    private ArrayUtil() {}
    
    /**
     * Flips a two-dimensional integer array, such that:
     * <pre>{ {foo1}, {foo2}, {foo3} }</pre> becomes
     * <pre>{ {foo3}, {foo2}, {foo1} }</pre>
     * 
     * @param arr The array to flip.
     * 
     * @return {@code arr}
     */
    public static int[][] flip2DIntArray(int[][] arr) {
        if(arr == null || arr.length < 2)
            return arr;
        
        int[] temp;
        for(int i = 0; i < arr.length / 2; i++) {
            temp = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = arr[i];
            arr[i] = temp;
        }
        
        return arr;
    }
    
    /**
     * Ensures an array's length is at least the specified value.
     * 
     * @param array The array.
     * @param minLength The minimum required array length.
     * 
     * @return A new array with the specified minimum size and the same
     * elements, if the given arrays length is less than that of the specified
     * minimum length, or the array, if its length is greater than or equal to
     * that of the specified minimum length, or {@code null} if the array
     * parameter is {@code null}.
     */
    /*
    public static <T> T[] setMinArrayLength(T[] array, int minLength) {
        if(array == null)
            return null;
        if(array.length >= minLength)
            return array;
        return Arrays.copyOf(array, minLength);
    }
    */
    
    /**
     * Gets the index of an object in an array.
     * 
     * @param array The array.
     * @param object The object.
     * 
     * @return The object's index, or -1 if the object is not in the array, or
     * the array is {@code null}.
     */
    /*
    public static <T> int indexOf(T[] array, Object object) {
        return Arrays.asList(array).indexOf(object);
    }
    */
    
    /**
     * Ensures that that size of the specified {@code ArrayList} is at least
     * that of the specified {@code size} parameter; expanding {@code list}
     * with {@code null} entries if necessary.
     */
    /*
    public static void ensureSize(ArrayList<?> list, int size) {
        list.ensureCapacity(size); // prevents excessive arraycopies
        while(list.size() < size)
            list.add(null);
    }
    */
    
    /**
     * A class which wraps an array as to not permit structural modification.
     */
    public static final class ImmutableArray<E> {
        
        private final E[] arr;
        
        /**
         * Creates a new immutable array.
         * 
         * @param arr The array to wrap.
         * 
         * @throws NullPointerException if {@code arr} is {@code null}.
         */
        public ImmutableArray(E[] arr) {
            this.arr = Objects.requireNonNull(arr);
        }
        
        /**
         * Gets the element in the array at the specified index.
         * 
         * @throws ArrayIndexOutOfBoundsException
         */
        public E get(int index) {
            return arr[index];
        }
        
        /**
         * @return The length of the array.
         */
        public int length() {
            return arr.length;
        }
        
    }
    
}
