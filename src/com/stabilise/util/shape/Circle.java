package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.Matrix2;
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
	public final Vector2 centre;
	/** The circle's radius. */
	public final float radius;

	
	/**
	 * Creates a new Circle, with a default radius of 0.
	 * 
	 * @param centre The centre of the circle.
	 */
	public Circle(Vector2 centre) {
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
		this(new Vector2(x, y), radius);
	}
	
	/**
	 * Creates a new Circle.
	 * 
	 * @param centre The circle's centre.
	 * @param radius The circle's radius.
	 */
	public Circle(Vector2 centre, float radius) {
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
	public Circle transform(Matrix2 matrix) {
		// TODO: Scale/skew/etc matrices are a no-go for now
		return this;
	}
	
	@Override
	public Shape translate(float x, float y) {
		return new Circle(new Vector2(centre.x + x, centre.y + y), radius);
	}
	
	@Override
	protected Vector2[] getVertices() {
		return new Vector2[] {centre};
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
	public boolean containsPoint(Vector2 p) {
		float dx = centre.x - p.x;
		float dy = centre.y - p.y;
		return dx*dx + dy*dy <= radius*radius;
	}
	
	@Override
	protected ShapeProjection getProjection(Vector2 axis) {
		// A circle, being a uniform shape, is of constant width for all axes
		float mid = centre.dot(axis);
		return new ShapeProjection(mid - radius, mid + radius);
	}
	
	@Override
	protected ShapeProjection getHorizontalProjection() {
		return getProjection(Vector2.X);
	}
	
	@Override
	protected ShapeProjection getVerticalProjection() {
		return getProjection(Vector2.Y);
	}
	
	/**
	 * Returns this circle.
	 */
	@Override
	public Circle reflect() {
		return this;
	}
	
}
