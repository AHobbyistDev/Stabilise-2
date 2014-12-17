package com.stabilise.util.shape;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.badlogic.gdx.math.Vector2;

/**
 * A precomputable shape is a shape wherein the projection axes and their
 * projections may be pre-calculated independent of collisions, as to allow for
 * faster collision computation. This provides a form of optimisation which
 * trades memory and initial computation for reduced computation at the time of
 * collisions.
 * 
 * <p>Since for now I'm too lazy to write comprehensive usage documentation,
 * refer to Quadrilateral or any of its subclasses to see how the functionality
 * of a precomputable shape should be implemented.
 */
public abstract class PrecomputableShape extends Shape {
	
	/**
	 * Gets the precomputed variant of this shape. If this shape is the
	 * precomputed variant, it will return itself.
	 * 
	 * @return The precomputed variant of this shape.
	 */
	public PrecomputableShape precomputed() {
		return this;
	}
	
	/**
	 * Gets the ordinary variant of this shape. If this shape is the ordinary
	 * variant, it will return itself.
	 * 
	 * @return The ordinary variant of this shape.
	 */
	public PrecomputableShape notPrecomputed() {
		return this;
	}
	
	/**
	 * Gets the axes upon which to project the shape for collision detection.
	 * These take the form of vectors perpendicular to each of the shape's
	 * edges.
	 * 
	 * <p>This method differs from {@link #generateAxes()} in that this returns
	 * the cached axes of this shape, which will have been precomputed on shape
	 * construction.
	 * 
	 * @return The shape's projection axes.
	 */
	@ForPrecomputedVariant
	protected Vector2[] getAxes() {
		// The proper return value:
		//return axes;
		
		// The placeholder return value:
		return generateAxes();
	}
	
	/**
	 * Gets the shape's projection on its i<font size="-1"><sup>th</sup></font>
	 * axis.
	 * 
	 * <p>The default implementation returns a new ShapeProjection with min and
	 * max of 0, and should be properly implemented by the precomputed variant
	 * of a shape.
	 * 
	 * @param i The axis number.
	 * 
	 * @return The shape's projection.
	 * @throws ArrayIndexOutOfBoundsException Thrown if {@code i} is negative
	 * or greater than {@code n-1}, where {@code n} is the shape's number of
	 * projection axes as returned by {@link #getAxes()} (that is, {@code n ==
	 * getAxes().length}).
	 */
	@ForPrecomputedVariant
	protected ShapeProjection getProjection(int i) {
		// The proper return value:
		//return projections[i];
		
		// The placeholder return value:
		return new ShapeProjection(0f, 0f);
	}
	
	/**
	 * Calculates whether or not a point is within the bounds of the shape.
	 * 
	 * <p>This method is optimised for the precomputed variant of a shape.
	 * 
	 * @param p The point.
	 * 
	 * @return {@code true} if the shape contains the point; {@code false}
	 * otherwise.
	 */
	@ForPrecomputedVariant
	protected boolean containsPointPrecomputed(Vector2 p) {
		for(int i = 0; i < getAxes().length; i++) {
			if(!getProjection(i).containsPoint(p.dot(getAxes()[i])))
				return false;
		}
		return true;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Indicates that the target method should be used by the precomputed
	 * variant of a shape. This also typically means said method will need to
	 * be properly implemented by the precomputed variant.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.METHOD})
	protected @interface ForPrecomputedVariant {
		// nothing to see here, move along
	}
	
}
