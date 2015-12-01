package com.stabilise.util.concurrent;

import java.util.Objects;
import java.util.function.IntFunction;

import javax.annotation.concurrent.Immutable;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.Checks;

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
     * @param numObjects The number of objects to split this striper into. This
     * is rounded to the next power of two.
     * @param supplier A function to use to generate the objects. The integer
     * parameter is each object's index.
     * 
     * @throws IllegalArgumentException if {@code numObjects < 1}.
     * @throws NullPointerException if {@code supplier} is {@code null}, or it
     * supplies any null objects.
     */
    public Striper(int numObjects, IntFunction<T> supplier) {
        Checks.testMin(numObjects, 1);
        Objects.requireNonNull(supplier);
        
        numObjects = MathUtils.nextPowerOfTwo(numObjects);
        
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
        return objects[i & mask]; // i & mask == i % numObjects
    }
    
    /**
     * Gets an object, as if by {@link #get(int) get(o.hashCode())}. If {@code
     * o} is {@code null}, {@code get(0)} is invoked instead.
     */
    public T get(Object o) {
        return get(Objects.hashCode(o));
    }
    
    
    /**
     * Creates and returns a striper for generic objects.
     * 
     * @param numObjects The number of objects to split this striper into. This
     * is rounded to the next power of two.
     * 
     * @throws IllegalArgumentException if {@code numObjects < 1}.
     */
    public static Striper<Object> generic(int numObjects) {
        return new Striper<>(numObjects, i -> new Object());
    }
    
}
