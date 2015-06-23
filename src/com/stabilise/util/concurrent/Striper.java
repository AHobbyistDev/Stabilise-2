package com.stabilise.util.concurrent;

import java.util.Objects;
import java.util.function.IntFunction;

import com.stabilise.util.annotation.Immutable;
import com.stabilise.util.maths.Maths;

/**
 * This class facilitates the implementation of an object striping strategy by
 * providing a set of indexed objects. This class may be used, for example, for
 * concurrent resource sharing and lock striping.
 */
@Immutable
public class Striper<T> {
	
	private final T[] objects;
	private final int mask;
	
	
	/**
	 * Creates a new object striper.
	 * 
	 * @param numObjects The number of objects to split this striper into.
	 * @param supplier A function to use to generate the objects. The integer
	 * parameter is each object's index.
	 * 
	 * @throws IllegalArgumentException if {@code numObjects} is negative, or
	 * is not a power of two.
	 * @throws NullPointerException if {@code supplier} is {@code null}, or it
	 * supplies any null objects.
	 */
	public Striper(int numObjects, IntFunction<T> supplier) {
		if(numObjects < 0)
			throw new IllegalArgumentException("numLocks < 0");
		if(!Maths.isPowerOfTwo(numObjects))
			throw new IllegalArgumentException("numLocks not a power of 2");
		
		Objects.requireNonNull(supplier);
		
		@SuppressWarnings("unchecked")
		T[] objs = (T[]) new Object[numObjects];
		for(int i = 0; i < numObjects; i++)
			objs[i] = Objects.requireNonNull(supplier.apply(i));
		
		objects = objs;
		mask = numObjects - 1; // only works if numObjects is a power of 2
	}
	
	/**
	 * Gets an object. The object is retrieved from index {@code
	 * (i % numObjects)} of this striper, where {@code numObjects} is specified
	 * in the constructor.
	 */
	public T get(int i) {
		return objects[i & mask];
	}
	
	
	/**
	 * Creates and returns a striper for generic objects.
	 * 
	 * @param numObjects The number of objects to split the striper into.
	 * 
	 * @throws IllegalArgumentException if {@code numObjects} is negative, or
	 * is not a power of two.
	 */
	public static Striper<Object> generic(int numObjects) {
		return new Striper<>(numObjects, i -> new Object());
	}
	
}
