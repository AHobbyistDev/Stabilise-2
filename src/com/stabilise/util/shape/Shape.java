package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.maths.Matrix2;

/**
 * A shape is a 2D object usually consisting of a number of vertices, which may
 * be used to represent such things as collision areas.
 * 
 * <p>Most classes in the {@code Shape} hierarchy are designed to be treated as
 * immutable, even though they expose their mutable vertices. As such, client
 * code is strongly encouraged to <b>never modify</b> said vertices, and
 * instead create new Shapes if a modified shape is desired.
 * 
 * <p>Classes in the {@code Shape} hierarchy tend to use the
 * <a href=http://en.wikipedia.org/wiki/Hyperplane_separation_theorem>
 * Separating Axis Theorem/Hyperplane Separation Theorem</a> for collision
 * detection.
 */
public abstract class Shape {
	
	/** A Shape which should be used as a placeholder to indicate the lack of a
	 * shape, in preference to a null pointer. */
	public static final Shape NO_SHAPE = new NoShape();
	
	
	
	/**
	 * Transforms this shape by applying the given transformation matrix to
	 * each of its vertices, where applicable, and returns the transformed
	 * shape. Each vertex is transformed by
	 * <a href=http://en.wikipedia.org/wiki/Matrix_multiplication> multiplying
	 * </a> the the given transformation matrix by said vertex's representative
	 * 2D vector.
	 * 
	 * <p>For example, for a rotation transformation, the matrix takes the
	 * form:
	 * 
	 * <pre>
	 * | cosθ  -sinθ |
	 * | sinθ  cosθ  |</pre>
	 * where θ is the angle by which to rotate the shape, in radians.
	 * 
	 * @param matrix The transformation matrix.
	 * 
	 * @return The transformed shape.
	 */
	public abstract Shape transform(Matrix2 matrix);
	
	/**
	 * Gets the vertices of this shape if it were transformed using the given
	 * matrix. The returned array is the same length as, and ordered the same
	 * as, the vertices returned by {@link #getVertices()}.
	 * 
	 * <p>Each vertex is transformed by
	 * <a href=http://en.wikipedia.org/wiki/Matrix_multiplication> multiplying
	 * </a> the the given transformation matrix by said vertex's representative
	 * 2D vector.
	 * 
	 * @param matrix The transformation matrix.
	 * 
	 * @return The transformed vertices. 
	 */
	protected Vector2[] getTransformedVertices(Matrix2 matrix) {
		Vector2[] vertices = getVertices();
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = matrix.transform(vertices[i]);
		return vertices;
	}
	
	/**
	 * Rotates a vertex anticlockwise about (0,0) by the specified angle. The
	 * supplied vertex will not be modified.
	 * 
	 * @param vertex The vertex.
	 * @param rotation The angle, in radians.
	 * 
	 * @return The rotated vertex.
	 */
	protected final Vector2 rotateVertex(Vector2 vertex, float rotation) {
		return rotateVertex(vertex, (float)Math.cos(rotation), (float)Math.sin(rotation));
	}
	
	/**
	 * Rotates a vertex anticlockwise about (0,0) using the sine and cosine of
	 * an angle. The supplied vertex will not be modified.
	 * 
	 * <p>This method is faster than {@link #rotateVertex(Vector2, float)}.
	 * 
	 * @param vertex The vertex.
	 * @param cos The cosine of the angle.
	 * @param sin The sine of the angle.
	 * 
	 * @return The rotated vertex.
	 */
	protected final Vector2 rotateVertex(Vector2 vertex, float cos, float sin) {
		return new Vector2(
				vertex.x * cos - vertex.y * sin,
				vertex.x * sin + vertex.y * cos
		);
	}
	
	/**
	 * Rotates this shape about the point (0,0) and returns the rotated shape.
	 * The shape's vertices, where applicable, will be rotated about the point
	 * (0,0) appropriately.
	 * 
	 * @param rotation The angle by which to rotate the shape anticlockwise, in
	 * radians.
	 * 
	 * @return The rotated shape.
	 */
	public Shape rotate(float rotation) {
		return transform(new Matrix2().setToRotation(rotation));
	}
	
	/**
	 * Gets a new shape object identical to this one, but translated.
	 * 
	 * <p>Implementation note: subclasses must ensure that the returned shape
	 * is of the same class as itself.
	 * 
	 * @param x The amount by which to translate the new shape, along the x-axis.
	 * @param y The amount by which to translate the new shape, along the y-axis.
	 * 
	 * @return The new translated shape.
	 */
	public abstract Shape translate(float x, float y);
	
	/**
	 * Clones the shape and reflects the clone about the y-axis.
	 * 
	 * <p>Implementation note: subclasses must ensure that the returned shape
	 * is of the same class as itself.
	 * 
	 * @return The reflected clone of the shape.
	 */
	public abstract Shape reflect();
	
	/**
	 * Gets an array containing all of the shape's vertices.
	 * 
	 * <p>Implementation note: it is imperative that any subclass of Shape
	 * returns the vertices <i>in the order they connect</i> (i.e. consecutive
	 * vertices in the returned array are joined by edges).
	 * 
	 * @return The shape's vertices.
	 */
	protected abstract Vector2[] getVertices();
	
	/**
	 * Calculates whether or not two shapes intersect.
	 * 
	 * @param s The shape with which to test intersection.
	 * 
	 * @return {@code true} if the two shapes intersect; {@code false}
	 * otherwise.
	 */
	public abstract boolean intersects(Shape s);
	
	/**
	 * Calculates whether or not the given point is within the bounds of the
	 * shape.
	 * 
	 * @param x The x-coordinate of the point.
	 * @param y The y-coordinate of the point.
	 * 
	 * @return {@code true} if the shape contains the point; {@code false}
	 * otherwise.
	 */
	public boolean containsPoint(float x, float y) {
		// Basic implementation for polygons
		Vector2[] axes = generateAxes();
		for(Vector2 axis : axes) {
			if(!getProjection(axis).containsPoint(axis.dot(x, y)))
				return false;
		}
		return true;
	}
	
	/**
	 * Calculates whether or not a point is within the bounds of the shape.
	 * 
	 * <p>This method redirects to {@link #containsPoint(float, float)
	 * containsPoint(p.x, p.y)}.
	 * 
	 * @param p The point.
	 * 
	 * @return {@code true} if the shape contains the point; {@code false}
	 * otherwise.
	 */
	public boolean containsPoint(Vector2 p) {
		return containsPoint(p.x, p.y);
	}
	
	/**
	 * Gets the axes upon which to project the shape for collision detection.
	 * These take the form of vectors perpendicular to each of the shape's
	 * edges.
	 * 
	 * @return The shape's projection axes.
	 */
	protected Vector2[] generateAxes() {
		Vector2[] vertices = getVertices();
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = getAxis(vertices[i], vertices[(i+1) == vertices.length ? 0 : i+1]);
		return vertices;
	}
	
	/**
	 * Gets the projection axis for two adjacent vertices. This takes the form
	 * of a vector perpendicular to the edge joining the vertices.
	 * 
	 * @param v1 The first vertex.
	 * @param v2 The second vertex.
	 * 
	 * @return The projection axis.
	 */
	protected final Vector2 getAxis(Vector2 v1, Vector2 v2) {
		// N.B. Normalising the vectors appears to be unnecessary
		
		// The following is how it's typically done:
		//return MathsUtil.rotate90Degrees(MathUtil.sub(v1, v2));
		// Faster, however, is:
		return new Vector2(v2.y - v1.y, v1.x - v2.x);
	}
	
	/**
	 * Gets the shape's projection for a given axis.
	 * 
	 * @param axis The axis upon which to project the shape.
	 * 
	 * @return The shape's projection.
	 */
	protected ShapeProjection getProjection(Vector2 axis) {
		Vector2[] vertices = getVertices();
		
		float min = axis.dot(vertices[0]);
		float max = min;
		
		for(int i = 1; i < vertices.length; i++) {
			float p = axis.dot(vertices[i]);
			if(p < min)
				min = p;
			else if(p > max)
				max = p;
		}
		
		return new ShapeProjection(min, max);
	}
	
	/**
	 * Gets the horizontal projection for this shape.
	 * 
	 * <p>The returned ShapeProjection is equivalent to the one returned as if
	 * by:
	 * 
	 * <pre>
	 * {@link #getProjection(Vector2) getProjection(new Vector2(1f, 0f))}
	 * </pre>
	 * 
	 * @return The horizontal projection.
	 * @throws ArrayIndexOutOfBoundsException if this shape's vertices as
	 * return by {@link #getVertices()} is of length 0.
	 */
	ShapeProjection getHorizontalProjection() {
		Vector2[] vertices = getVertices();
		
		float min = vertices[0].x;
		float max = min;
		
		for(int i = 1; i < vertices.length; i++) {
			if(vertices[i].x < min)
				min = vertices[i].x;
			else if(vertices[i].x > max)
				max = vertices[i].x;
		}
		
		return new ShapeProjection(min, max);
	}
	
	/**
	 * Gets the vertical projection for this shape.
	 * 
	 * <p>The returned ShapeProjection is equivalent to the one returned as if
	 * by:
	 * 
	 * <pre>
	 * {@link #getProjection(Vector2) getProjection(new Vector2(0f, 1f))}
	 * </pre>
	 * 
	 * @return The vertical projection.
	 * @throws ArrayIndexOutOfBoundsException if this shape's vertices as
	 * return by {@link #getVertices()} is of length 0.
	 */
	ShapeProjection getVerticalProjection() {
		Vector2[] vertices = getVertices();
		
		float min = vertices[0].y;
		float max = min;
		
		for(int i = 1; i < vertices.length; i++) {
			if(vertices[i].y < min)
				min = vertices[i].y;
			else if(vertices[i].y > max)
				max = vertices[i].y;
		}
		
		return new ShapeProjection(min, max);
	}
	
	/**
	 * Checks whether or not this Shape is an AABB.
	 * 
	 * @return {@code true} if this shape is an AABB; {@code false} otherwise.
	 */
	protected boolean isAABB() {
		//return this instanceof AABB;
		return false;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Rotates a Shape about the point (0,0) by a given angle, in radians, and
	 * then returns it. This automatically casts the returned shape to the same
	 * class as the given shape.
	 * 
	 * @param shape The shape.
	 * @param rotation The angle by which to rotate the shape, in radians.
	 * 
	 * @return The rotated shape.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Shape> T rotate(T shape, float rotation) {
		return (T)shape.rotate(rotation);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A blank implementation of Shape used by {@link #NO_SHAPE}.
	 */
	private static final class NoShape extends Shape {
		@Override public Shape transform(Matrix2 matrix) { return this; }
		@Override public Shape translate(float x, float y) { return this; }
		@Override protected Vector2[] getVertices() { return new Vector2[0]; }
		@Override public boolean intersects(Shape s) { return false; }
		@Override public boolean containsPoint(Vector2 p) { return false; }
		@Override public Shape reflect() { return this; }
	}
	
}
