package com.stabilise.util.shape;

import java.util.function.Function;

import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Vec2;

/**
 * For the uninitiated: <i>A circle is a simple shape of Euclidean geometry
 * that is the set of all points in a plane that are at a given distance from a
 * given point, the centre. The distance between any of the points and the
 * centre is called the radius. It can also be defined as the locus of a point
 * equidistant from a fixed point.</i>
 */
public class Circle extends Shape {
	
	/** The coordinates of the centre of the circle. */
	public final float x, y;
	/** The circle's radius. */
	public final float radius;

	
	/**
	 * Creates a new Circle, with a default radius of 0.
	 * 
	 * @param centre The centre of the circle.
	 */
	public Circle(Vec2 centre) {
		this(centre, 0);
	}
	
	/**
	 * Creates a new Circle.
	 * 
	 * @param centre The circle's centre.
	 * @param radius The circle's radius.
	 */
	public Circle(Vec2 centre, float radius) {
		this(centre.x(), centre.y(), radius);
	}
	
	/**
	 * Creates a new Circle object.
	 * 
	 * @param x The x-coordinate of the circle's centre.
	 * @param y The y-coordinate of the circle's centre.
	 * @param radius The circle's radius.
	 */
	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	
	/**
	 * Returns this circle.
	 */
	@Override
	public Circle rotate(float rotation) {
		return this;
	}
	
	@Override
	public Circle transform(Matrix2 matrix) {
		// TODO: Proper transformation functionality.
		return (Circle)super.transform(matrix);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Note that only the centrepoint of this circle is transformed as per
	 * the function.
	 */
	@Override
	@Incomplete
	public Circle transform(Function<Vec2, Vec2> f) {
		// TODO: Wasteful as this results in the instantiation of two Vec2s.
		return new Circle(f.apply(Vec2.immutable(x,y)), radius);
	}
	
	@Override
	public Shape translate(float x, float y) {
		return new Circle(this.x + x, this.y + y, radius);
	}
	
	/**
	 * Returns this circle.
	 */
	@Override
	public Circle reflect() {
		return this;
	}
	
	@Override
	protected Vec2[] getVertices() {
		return new Vec2[] {Vec2.immutable(x, y)};
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>The current implementation redirects the test, as if by:
	 * <pre>s.intersects(this)</pre>
	 */
	@Override
	public boolean intersects(Shape s) {
		if(radius == 0) return false;
		if(s instanceof Circle)
			return intersects((Circle)s);
		return s.intersects(this);
	}
	
	/**
	 * Calculates whether or not this circle intersects with another.
	 * 
	 * @return {@code true} if this intersects with {@code c}; {@code false}
	 * otherwise.
	 */
	public boolean intersects(Circle c) {
		if(radius == 0) return false;
		float dx = this.x - c.x;
		float dy = this.y - c.y;
		float radii = radius + c.radius;
		return dx*dx + dy*dy <= radii*radii;
	}
	
	@Override
	public boolean contains(Shape s) {
		if(s instanceof Circle)
			return contains((Circle)s);
		return s.contains(this);
	}
	
	/**
	 * Calculates whether or not this circle contains another.
	 * 
	 * @return {@code true} if this contains {@code c}; {@code false}
	 * otherwise.
	 */
	public boolean contains(Circle c) {
		float dx = this.x - c.x;
		float dy = this.y - c.y;
		return radius >= dx*dx + dy*dy + c.radius;
	}
	
	@Override
	public boolean containsPoint(float x, float y) {
		float dx = this.x - x;
		float dy = this.y - y;
		return dx*dx + dy*dy <= radius*radius;
	}
	
	@Override
	protected ShapeProjection getProjection(Vec2 axis) {
		// A circle, being a uniform shape, is of constant width for all axes
		float mid = axis.dot(x, y);
		// TODO + or - radius doesn't work unless be divide by |axis|
		return new ShapeProjection(mid - radius, mid + radius);
	}
	
	@Override
	protected ShapeProjection getHorizontalProjection() {
		return new ShapeProjection(x - radius, x + radius);
	}
	
	@Override
	protected ShapeProjection getVerticalProjection() {
		return new ShapeProjection(y - radius, y + radius);
	}
	
}
