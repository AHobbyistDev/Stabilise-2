package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.MathUtil;
import com.stabilise.util.annotation.Incomplete;

/**
 * For the uninitiated: <i>A circle is a simple shape of Euclidean geometry
 * that is the set of all points in a plane that are at a given distance from a
 * given point, the centre. The distance between any of the points and the
 * centre is called the radius. It can also be defined as the locus of a point
 * equidistant from a fixed point.</i>
 */
public class Circle extends Shape {
	
	/** The point that is the centre of the circle. */
	public final Vector2f centre;
	/** The circle's radius. */
	public final float radius;

	
	/**
	 * Creates a new Circle, with a default radius of 0.
	 * 
	 * @param centre The centre of the circle.
	 */
	public Circle(Vector2f centre) {
		this(centre, 0);
	}
	
	/**
	 * Creates a new Circle object.
	 * 
	 * @param x The x-coordinate of the circle's centre.
	 * @param y The y-coordinate of the circle's centre.
	 * @param radius The circle's radius.
	 */
	public Circle(float x, float y, float radius) {
		this(new Vector2f(x, y), radius);
	}
	
	/**
	 * Creates a new Circle.
	 * 
	 * @param centre The circle's centre.
	 * @param radius The circle's radius.
	 */
	public Circle(Vector2f centre, float radius) {
		this.centre = centre;
		this.radius = radius;
	}
	
	/**
	 * Returns this circle.
	 */
	@Override
	public Circle rotate(float rotation) {
		return this;
	}
	
	/**
	 * This currently returns this circle, as fancier shapes have yet to be
	 * implemented.
	 */
	@Override
	@Incomplete
	public Circle transform(Matrix2f matrix) {
		// TODO: Scale/skew/etc matrices are a no-go for now
		return this;
	}
	
	@Override
	public Shape translate(float x, float y) {
		return new Circle(new Vector2f(centre.x + x, centre.y + y), radius);
	}
	
	@Override
	protected Vector2f[] getVertices() {
		return new Vector2f[] {centre};
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>The current implementation redirects the test, as if by:
	 * <pre>s.intersects(this)</pre>
	 */
	@Override
	public boolean intersects(Shape s) {
		return s.intersects(this);
	}
	
	@Override
	public boolean containsPoint(Vector2f p) {
		float dx = centre.x - p.x;
		float dy = centre.y - p.y;
		return dx*dx + dy*dy <= radius*radius;
	}
	
	@Override
	protected ShapeProjection getProjection(Vector2f axis) {
		// A circle, being a uniform shape, is of constant width for all axes
		float mid = Vector2f.dot(centre, axis);
		return new ShapeProjection(mid - radius, mid + radius);
	}
	
	@Override
	protected ShapeProjection getHorizontalProjection() {
		return getProjection(MathUtil.UNIT_VECTOR_X);
	}
	
	@Override
	protected ShapeProjection getVerticalProjection() {
		return getProjection(MathUtil.UNIT_VECTOR_Y);
	}
	
	/**
	 * Returns this circle.
	 */
	@Override
	public Circle reflect() {
		return this;
	}
	
}
