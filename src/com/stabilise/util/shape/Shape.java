package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.MathUtil;
import com.stabilise.util.MatrixUtil;

/**
 * A shape is a 2D object consisting of a number of vertices, which may be used
 * to represent such things as collision volumes.
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
		return transform(MatrixUtil.rotationMatrix2f(rotation));
	}
	
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
	// Note: Subclasses must ensure the returned shape must be of the same
	// class as this shape.
	public abstract Shape transform(Matrix2f matrix);
	
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
	protected Vector2f[] getTransformedVertices(Matrix2f matrix) {
		Vector2f[] vertices = getVertices();
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = transformVertex(matrix, vertices[i]);
		return vertices;
	}
	
	/**
	 * Transforms a vertex by applying the given transformation matrix to it.
	 * 
	 * @param matrix The 2x2 transformation matrix.
	 * @param vertex The vertex to rotate.
	 * 
	 * @return The transformed vertex.
	 */
	protected final Vector2f transformVertex(Matrix2f matrix, Vector2f vertex) {
		return Matrix2f.transform(matrix, vertex, null);
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
	protected abstract Vector2f[] getVertices();
	
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
		return containsPoint(new Vector2f(x, y));
	}
	
	/**
	 * Calculates whether or not a point is within the bounds of the shape.
	 * 
	 * @param p The point.
	 * 
	 * @return {@code true} if the shape contains the point; {@code false}
	 * otherwise.
	 */
	public boolean containsPoint(Vector2f p) {
		// Basic implementation for polygons
		Vector2f[] axes = generateAxes();
		for(Vector2f axis : axes) {
			if(!getProjection(axis).containsPoint(Vector2f.dot(axis, p)))
				return false;
		}
		return true;
	}
	
	/**
	 * Gets the axes upon which to project the shape for collision detection.
	 * These take the form of vectors perpendicular to each of the shape's
	 * edges.
	 * 
	 * @return The shape's projection axes.
	 */
	protected Vector2f[] generateAxes() {
		Vector2f[] vertices = getVertices();
		
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = getAxis(vertices[i], vertices[(i+1) % vertices.length]);
		
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
	protected final Vector2f getAxis(Vector2f v1, Vector2f v2) {
		// Normalising the vectors appears to be unnecessary
		return MathUtil.getPerpendicularVector(Vector2f.sub(v1, v2, null));//.normalise()
	}
	
	/**
	 * Gets the shape's projection for a given axis.
	 * 
	 * @param axis The axis upon which to project the shape.
	 * 
	 * @return The shape's projection.
	 */
	protected ShapeProjection getProjection(Vector2f axis) {
		Vector2f[] vertices = getVertices();
		
		float min = Vector2f.dot(axis, vertices[0]);
		float max = min;
		
		for(int i = 1; i < vertices.length; i++) {
			float p = Vector2f.dot(axis, vertices[i]);
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
	 * {@link #getProjection(Vector2f) getProjection(new Vector2f(1f, 0f))}
	 * </pre>
	 * 
	 * @return The horizontal projection.
	 * @throws ArrayIndexOutOfBoundsException if this shape's vertices as
	 * return by {@link #getVertices()} is of length 0.
	 */
	protected ShapeProjection getHorizontalProjection() {
		Vector2f[] vertices = getVertices();
		
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
	 * {@link #getProjection(Vector2f) getProjection(new Vector2f(0f, 1f))}
	 * </pre>
	 * 
	 * @return The vertical projection.
	 * @throws ArrayIndexOutOfBoundsException if this shape's vertices as
	 * return by {@link #getVertices()} is of length 0.
	 */
	protected ShapeProjection getVerticalProjection() {
		Vector2f[] vertices = getVertices();
		
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
		@Override public Shape transform(Matrix2f matrix) { return this; }
		@Override public Shape translate(float x, float y) { return this; }
		@Override protected Vector2f[] getVertices() { return new Vector2f[0]; }
		@Override public boolean intersects(Shape s) { return false; }
		@Override public boolean containsPoint(Vector2f p) { return false; }
		@Override public Shape reflect() { return this; }
	}
	
}
