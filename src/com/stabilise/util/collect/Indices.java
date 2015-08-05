package com.stabilise.util.collect;

/**
 * This class provides simple and concise ways for testing indices and throwing
 * appropriate IndexOutOfBoundsExceptions.
 */
public class Indices {
	
	private Indices() {} // non-instantiable
	
	/**
	 * Tests a value.
	 * 
	 * @param index The index being tested.
	 * @param min The minimum legal value (inclusive).
	 * @param max The maximum legal value (inclusive).
	 * 
	 * @throws IndexOutOfBoundsException if {@code index < min || index > max}.
	 */
	public static void test(int index, int min, int max) {
		if(index < min || index > max)
			throw new IndexOutOfBoundsException("Illegal index " + index +
					"; it should be in the range [" + min + ", " + max + "]");
	}
	
	/**
	 * Tests a value.
	 * 
	 * @param index The index being tested.
	 * @param min The minimum legal value (inclusive).
	 * 
	 * @throws IndexOutOfBoundsException if {@code index < min}.
	 */
	public static void testMin(int index, int min) {
		if(index < min)
			throw new IndexOutOfBoundsException("Illegal index " + index +
					"; it should not be less than " + min);
	}
	
	/**
	 * Tests a value.
	 * 
	 * @param index The index being tested.
	 * @param max The maximum legal value (inclusive).
	 * 
	 * @throws IndexOutOfBoundsException if {@code index > max}.
	 */
	public static void testMax(int index, int max) {
		if(index > max)
			throw new IndexOutOfBoundsException("Illegal index " + index +
					"; it should not be greater than " + max);
	}
	
}
