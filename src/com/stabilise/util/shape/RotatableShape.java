package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

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
 * 
 * @param <T> The type of shape.
 */
public class RotatableShape<T extends Shape> extends Shape {
	
	/** The original shape. */
	private final T baseShape;
	/** The rotated shape. */
	private T rotatedShape;
	/** The rotation. */
	private float rotation;
	
	
	/**
	 * Creates a new RotatableShape.
	 * 
	 * @param shape The base shape.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code shape} is an
	 * {@code AxisAlignedBoundingBox}.
	 */
	public RotatableShape(T shape) {
		if(shape instanceof AxisAlignedBoundingBox)
			throw new IllegalArgumentException("Cannot wrap an AxisAlignedBoundingBox " +
					"in a RotatableShape; an AABB may not be rotated!");
		
		this.baseShape = shape;
		rotatedShape = shape;
		rotation = 0f;
	}
	
	/**
	 * Gets the rotated shape represented by this RotatableShape.
	 * 
	 * @return The shape.
	 */
	public T get() {
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
	public T rotate(float rotation) {
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
	@SuppressWarnings("unchecked")
	public T setRotation(float rotation) {
		if(rotation == 0)
			return rotatedShape = baseShape;
		if(this.rotation == rotation)
			return rotatedShape;
		this.rotation = rotation;
		return rotatedShape = (T)baseShape.rotate(rotation);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Invoking this method is equivalent to invoking
	 * <pre>get().transform(matrix)</pre>
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T transform(Matrix2f matrix) {
		return (T)rotatedShape.transform(matrix);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Invoking this method is equivalent to invoking
	 * <pre>get().translate(x, y)</pre>
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T translate(float x, float y) {
		return (T)rotatedShape.translate(x, y);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method is equivalent to invoking
	 * <pre>get().getVertices()</pre>
	 */
	@Override
	protected Vector2f[] getVertices() {
		return rotatedShape.getVertices();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method is equivalent to invoking
	 * <pre>get().intersects(s)</pre>
	 */
	@Override
	public boolean intersects(Shape s) {
		return rotatedShape.intersects(s);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method is equivalent to invoking
	 * <pre>get().containsPoint(p)</pre>
	 */
	@Override
	public boolean containsPoint(Vector2f p) {
		return rotatedShape.containsPoint(p);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method is equivalent to invoking
	 * <pre>get().reflect()</pre>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T reflect() {
		return (T)rotatedShape.reflect();
	}
	
}
