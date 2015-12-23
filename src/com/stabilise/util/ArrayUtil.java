package com.stabilise.util;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Array-related utility functions.
 */
public class ArrayUtil {
    
    private static final Object[] EMPTY_ARR = new Object[0];
    
    
    // non-instantiable
    private ArrayUtil() {}
    
    
    /**
     * Flips a two-dimensional integer array, such that e.g.:
     * <pre>{ {foo1}, {foo2}, {foo3} }</pre> becomes
     * <pre>{ {foo3}, {foo2}, {foo1} }</pre>
     * 
     * @param a The array to flip.
     * 
     * @return {@code a}
     */
    public static int[][] flip2DIntArray(int[][] a) {
        if(a == null || a.length < 2)
            return a;
        
        int[] temp;
        for(int i = 0; i < a.length / 2; i++) {
            temp = a[a.length - i - 1];
            a[a.length - i - 1] = a[i];
            a[i] = temp;
        }
        
        return a;
    }
    
    /**
     * Performs a deep copy of the given 2D array (because arr.clone() does
     * not work as one would expect >.<).
     * 
     * @throws NullPointerException if {@code arr} is {@code null}.
     */
    public static int[][] deepCopy(int[][] arr) {
        int[][] copy = new int[arr.length][];
        for(int i = 0; i < arr.length; i++) {
            copy[i] = arr[i].clone();
        }
        return copy;
    }
    
    /**
     * Converts a 1D array to a 2D array with the specified number of rows and
     * columns.
     * 
     * @throws NullPointerException if {@code a} is {@code null}.
     * @throws IllegalArgumentException if {@code a.length != rows * cols}, or
     * either {@code rows} or {@code cols} is negative.
     */
    public static int[][] to2D(int[] a, int rows, int cols) {
        if(a.length != rows * cols)
            throw new IllegalArgumentException("Input array length ");
        int[][] arr = new int[rows][cols];
        int i = 0;
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                arr[r][c] = a[i++];
            }
        }
        return arr;
    }
    
    /**
     * Converts a 2D array to a 1D array.
     */
    public static int[] to1D(int[][] a) {
        int rows = a.length;
        if(rows == 0)
            return new int[0];
        int cols = a[0].length;
        int[] arr = new int[rows*cols];
        int i = 0;
        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                arr[i++] = a[r][c];
            }
        }
        return arr;
    }
    
    /**
     * Returns a random element of the given array using ThreadLocalRandom.
     * 
     * @throws NullPointerException if {@code arr} is {@code null}.
     */
    @SafeVarargs
    public static <T> T random(T... arr) {
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }
    
    /**
     * Returns an empty array. Safe to use as there are no elements present.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArr() {
        return (T[]) EMPTY_ARR;
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
