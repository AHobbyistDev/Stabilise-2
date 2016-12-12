package com.stabilise.util.maths;

import java.util.Objects;
import java.util.function.IntBinaryOperator;

import javax.annotation.concurrent.Immutable;

import com.stabilise.util.maths.Point.ImmutablePoint;
import com.stabilise.util.maths.Point.MutablePoint;

/**
 * A {@code PointFactory} produces {@code Points} which use a specified hash
 * function to generate their hash codes instead of the default hash function.
 * 
 * <p>Note that points created by a PointFactory may not necessarily produce
 * the same hash codes as points instantiated normally, or those produced by a
 * different PointFactory, so it is a very bad idea to intermix these different
 * 'breeds' of points.
 */
@Immutable
public class PointFactory {
    
    private final IntBinaryOperator hasher;
    
    
    /**
     * Creates a new point factory.
     * 
     * @param hasher The function with which to produce a point's hash code.
     * 
     * @throws NullPointerException if {@code hasher} is {@code null}.
     */
    public PointFactory(IntBinaryOperator hasher) {
        this.hasher = Objects.requireNonNull(hasher);
    }
    
    /**
     * Creates a new point factory with a hasher returned by {@link
     * Maths#genHashFunction(int, boolean)}.
     */
    public PointFactory(int maxElements, boolean negateHashMapShift) {
        hasher = Maths.genHashFunction(maxElements, negateHashMapShift);
    }
    
    /**
     * Creates a new immutable Point with the specified components.
     */
    public Point newImmutablePoint(int x, int y) {
        return new OwnedImmutablePoint(x, y);
    }
    
    /**
     * Creates a new mutable Point with the specified components.
     */
    public MutablePoint newMutablePoint(int x, int y) {
        return new OwnedMutablePoint(x, y);
    }
    
    /**
     * Creates a new mutable Point with components (0,0).
     */
    public MutablePoint newMutablePoint() {
        return newMutablePoint(0, 0);
    }
    
    // Nested classes ---------------------------------------------------------
    
    private class OwnedImmutablePoint extends ImmutablePoint {
        private OwnedImmutablePoint(int x, int y) { super(x,y); }
        @Override protected int genHash() { return hasher.applyAsInt(x, y); }
    }
    
    private class OwnedMutablePoint extends MutablePoint {
        private OwnedMutablePoint(int x, int y) { super(x,y); }
        @Override protected int genHash() { return hasher.applyAsInt(x, y); }
    }
    
}
