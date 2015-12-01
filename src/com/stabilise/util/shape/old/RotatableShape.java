package com.stabilise.util.shape.old;

import java.util.function.UnaryOperator;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.util.maths.Vec2;

/**
 * A RotatableShape serves as a wrapper for a Shape which can be rotated,
 * allowing that shape's rotation to be stored and rotated variants of that
 * shape to be obtained. This class accomplishes this by storing that shape as
 * a template and obtaining all rotated shapes from it; this is done in
 * preference to obtaining a rotated shape from the last rotated shape, wherein
 * cumulative floating point errors derived from the rotation process could
 * lead to inaccuracies in calculations.
 * 
 * <p>This class extends {@code Shape} such that a RotatableShape instance may
 * be used in place of a standard Shape without necessitating much refactoring.
 */
@NotThreadSafe
@Deprecated
public class RotatableShape extends Shape {
    
    /** The original, or template, shape. */
    private final Shape baseShape;
    /** The rotated shape, which is treated as the actual shape. */
    private Shape rotatedShape;
    /** Rotation, in radians. */
    private float rotation;
    
    
    /**
     * Creates a new RotatableShape.
     * 
     * @param shape The base shape.
     * 
     * @throws IllegalArgumentException if {@code shape} is an AABB.
     */
    public RotatableShape(Shape shape) {
        if(shape instanceof AABBOld)
            throw new IllegalArgumentException("Cannot wrap an AABB in a "
                    + "RotatableShape since it may not be rotated!");
        
        this.baseShape = shape;
        rotatedShape = shape;
        rotation = 0f;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>
     * Invoking this method is equivalent to invoking
     * <pre>get().transform(f)</pre>
     * </p>
     */
    @Override
    public Shape transform(UnaryOperator<Vec2> f) {
        return rotatedShape.transform(f);
    }
    
    /**
     * Gets the rotated shape represented by this RotatableShape.
     * 
     * @return The shape.
     */
    public Shape get() {
        return rotatedShape;
    }
    
    /**
     * Gets the shape's rotation.
     * 
     * @return The shape's rotation, in radians.
     */
    public float getRotation() {
        return rotation;
    }
    
    /**
     * Rotates the shape about the point (0,0) and returns the rotated shape.
     * This rotation is cumulative and the shape will be rotated relative to
     * its current rotation.
     * 
     * @param rotation The angle by which to rotate the shape anticlockwise, in
     * radians.
     * 
     * @return The rotated shape.
     */
    @Override
    public Shape rotate(float rotation) {
        return setRotation(this.rotation + rotation);
    }
    
    /**
     * Sets the shape's rotation, and returns the rotated shape. The shape
     * will be rotated about (0,0) appropriately.
     * 
     * @param rotation The angle by which to rotate the shape anticlockwise
     * from its originally-defined position, in radians.
     * 
     * @return The rotated shape.
     */
    public Shape setRotation(float rotation) {
        if(rotation == 0f)
            return rotatedShape = baseShape;
        if(this.rotation == rotation)
            return rotatedShape;
        this.rotation = rotation;
        return rotatedShape = baseShape.rotate(rotation);
    }
    
    @Override
    public Shape translate(float x, float y) {
        return rotatedShape.translate(x, y);
    }
    
    @Override
    protected Vec2[] getVertices() {
        return rotatedShape.getVertices();
    }
    
    @Override
    public boolean intersects(Shape s) {
        return rotatedShape.intersects(s);
    }
    
    @Override
    public boolean contains(Shape s) {
        return rotatedShape.contains(s);
    }
    
    @Override
    public boolean containsPoint(float x, float y) {
        return rotatedShape.containsPoint(x, y);
    }
    
}
