package com.stabilise.util;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Array-related utility functions.
 */
public class ArrayUtil {
	
	// non-instantiable
	private ArrayUtil() {}
	
	/**
	 * Flips a two-dimensional array with non-primitive elements, such that:
	 * <pre>{ {foo1}, {foo2}, {foo3} }</pre> becomes
	 * <pre>{ {foo3}, {foo2}, {foo1} }</pre>
	 * 
	 * @param arr The array to flip.
	 * 
	 * @return {@code arr}
	 */
	public static <T> T[][] reverse2DArray(T[][] arr) {
		if(arr == null || arr.length < 2)
			return arr;
		
		T[] temp;
		for(int i = 0; i < arr.length / 2; i++) {
			temp = arr[arr.length - i - 1];
			arr[arr.length - i - 1] = arr[i];
			arr[i] = temp;
		}
		
		return arr;
	}
	
	/**
	 * Flips a two-dimensional integer array, such that:
	 * <pre>{ {foo1}, {foo2}, {foo3} }</pre> becomes
	 * <pre>{ {foo3}, {foo2}, {foo1} }</pre>
	 * 
	 * @param arr The array to flip.
	 * 
	 * @return {@code arr}
	 */
	public static int[][] reverse2DIntArray(int[][] arr) {
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
	public static <T> T[] setMinArrayLength(T[] array, int minLength) {
		if(array == null)
			return null;
		
		if(array.length >= minLength)
			return array;
		
		return (T[]) Arrays.copyOf(array, minLength);
	}
	
	/**
	 * Gets the index of an object in an array.
	 * 
	 * @param array The array.
	 * @param object The object.
	 * 
	 * @return The object's index, or -1 if the object is not in the array, or
	 * the array is {@code null}.
	 */
	public static <T> int indexOf(T[] array, Object object) {
		return Arrays.asList(array).indexOf(object);
	}
	
	/**
	 * Removes all instances of {@code null} from an array.
	 * 
	 * @param array The array.
	 * 
	 * @return The array, with no {@code null} elements.
	 */
	public static <T> T[] removeNulls(T[] array) {
		// TODO: Inefficient as there may be multiple adjacent null elements,
		// but it'll do for now.
		for(int i = 0; i < array.length;) {
			if(array[i] == null)
				array = ArrayUtils.remove(array, i);
			else
				i++;
		}
		return array;
	}
	
}
