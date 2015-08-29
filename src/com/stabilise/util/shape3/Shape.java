package com.stabilise.util.shape3;


import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.maths.Matrix2;

/**
 * A shape is a 2D object usually consisting of a number of vertices, which may
 * be used to represent such things as collision areas.
 * 
 * <p>Classes in the {@code Shape} hierarchy are immutable, but may be {@link
 * #transform(Matrix2) transformed} to generate a new shape.
 * 
 * <p>These classes use the <a
 * href=http://en.wikipedia.org/wiki/Hyperplane_separation_theorem> Separating
 * Axis Theorem/Hyperplane Separation Theorem</a> for collision detection.
 */
public abstract class Shape {
	
	/** A Shape which should be used as a placeholder to indicate the lack of a
	 * shape, in preference to a null pointer. */
	public static final Shape NO_SHAPE = new NoShape();
	
	
	/**
	 * Transforms this shape by applying the given transformation function to
	 * each of its vertices, and returns the transformed shape. This shape is
	 * unmodified.
	 * 
	 * @param f The transformation function.
	 * 
	 * @return The transformed shape.
	 * @throws NullPointerException if {@code f} is {@code null}.
	 */
	public abstract Shape transform(VertexFunction f);
	
	/**
	 * Gets the vertices of this shape, transformed using the given function.
	 * The returned array is the same length as (and ordered the same as) the
	 * vertices returned by {@link #getVertices()}.
	 * 
	 * @param f The transformation function.
	 * 
	 * @return The transformed vertices.
	 */
	protected float[] transformVertices(VertexFunction f) {
		float[] verts = getVertices();
		float[] newVerts = new float[verts.length];
		for(int i = 0; i < verts.length; i += 2)
			f.apply(newVerts, i, verts[i], verts[i+1]);
		return newVerts;
	}
	
	/**
	 * Transforms this shape by applying the given transformation matrix to
	 * each of its vertices, where applicable, and returns the transformed
	 * shape. Each vertex is transformed by
	 * <a href=http://en.wikipedia.org/wiki/Matrix_multiplication> multiplying
	 * </a> the the given transformation matrix by said vertex's representative
	 * 2D vector. This shape is unmodified.
	 * 
	 * @param m The transformation matrix.
	 * 
	 * @return The transformed shape.
	 * @throws NullPointerException if {@code m} is {@code null}.
	 */
	public Shape transform(Matrix2 m) {
		return transform((dest,o,x,y) -> {
			dest[o]   = m.m00*x + m.m01*y;
			dest[o+1] = m.m10*x + m.m10*y;
		});
	}
	
	/**
	 * Rotates this shape about the point (0,0) and returns the rotated shape.
	 * The shape's vertices, where applicable, will be rotated about the point
	 * (0,0) appropriately.
	 * 
	 * @param rads The angle by which to rotate the shape anticlockwise, in
	 * radians.
	 * 
	 * @return The rotated shape.
	 * @throws UnsupportedOperationException if this shape is an {@code AABB}.
	 */
	public Shape rotate(float rads) {
		//return transform(new Matrix2().setToRotation(rotation));
		final float cos = MathUtils.cos(rads);
		final float sin = MathUtils.sin(rads);
		return transform((dest,o,x,y) -> {
			dest[o]   = x*cos - y*sin;
			dest[o+1] = x*sin + y*cos;
		});
	}
	
	/**
	 * Gets a new shape object identical to this one, but translated.
	 * 
	 * @param x The translation along x-axis.
	 * @param y The translation along the y-axis.
	 * 
	 * @return The new translated shape.
	 */
	public Shape translate(float x, float y) {
		return transform((dest,o,x0,y0) -> {
			dest[o]   = x0 + x;
			dest[o+1] = y0 + y;
		});
	}
	
	/**
	 * Clones this shape and reflects the clone about the y-axis.
	 * 
	 * @return The reflected clone of this shape.
	 */
	public Shape reflect() {
		return transform((dest,o,x,y) -> {
			dest[o]   = -x;
			dest[o+1] = y;
		});
	}
	
	/**
	 * Calculates whether or not this shape intersects with another.
	 * 
	 * @return {@code true} if this shape intersects with {@code s}; {@code
	 * false} otherwise.
	 * @throws NullPointerException if {@code s} is {@code null}.
	 */
	public abstract boolean intersects(Shape s);
	
	/**
	 * Calculates whether or this shape contains the specified shape.
	 * 
	 * @return {@code true} if this shape contains {@code s}; {@code false}
	 * otherwise.
	 * @throws NullPointerException if {@code s} is {@code null}.
	 */
	public abstract boolean contains(Shape s);
	
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
	/*
	public final boolean containsPoint(Vec2 p) {
		return containsPoint(p.x, p.y);
	}
	*/
	
	/**
	 * Calculates whether or not the given point is within the bounds of this
	 * shape.
	 * 
	 * @param x The x-coordinate of the point.
	 * @param y The y-coordinate of the point.
	 * 
	 * @return {@code true} if this shape contains the point; {@code false}
	 * otherwise.
	 */
	public abstract boolean containsPoint(float x, float y);
	
	/**
	 * Gets this shape's vertices.
	 * 
	 * <p>The vertices are returned in the order they physically connect - that
	 * is, consecutive vertices in the array (first and last are considered
	 * consecutive) are joined by edges.
	 */
	protected abstract float[] getVertices();
	
	/**
	 * Gets the axes upon which to project the shape for collision detection.
	 * 
	 * <p>A shape's projection axes are a set of vectors orthogonal to each of
	 * its edges. This such such that projection of object (via dot product)
	 * onto said axis allows for easy testing as to whether or not that object
	 * appears to lie (however partially) within the shape from the viewpoint
	 * of that axis.
	 * 
	 * @return The shape's projection axes.
	 */
	protected float[] getAxes() {
		return genAxes();
	}
	
	/**
	 * Generates and returns this shape's projection axes.
	 */
	protected float[] genAxes() {
		float[] verts = getVertices();
		float[] axes = new float[verts.length];
		generateAxes(getVertices(), axes);
		return axes;
	}
	
	/**
	 * Gets this shape's projection for a given axis. This method sets:
	 * 
	 * <pre>
	 * dest[offset]   = min;
	 * dest[offset+1] = max;
	 * </pre>
	 * 
	 * @param axis The axis upon which to project the shape.
	 * @param dest The destination array in which to store the projection.
	 * @param offset The array offset.
	 * @param x The x-coordinate of the axis.
	 * @param y The y-coordinate of the axis.
	 * 
	 * @throws NullPointerException if {@code dest} is {@code null}.
	 */
	protected void getProjection(float[] dest, int offset, float x, float y) {
		getProjection(getVertices(), x, y, dest, offset);
	}
	
	/**
	 * Gets the horizontal projection for this shape.
	 * 
	 * <p>The returned ShapeProjection is equivalent to the one returned as if
	 * by:
	 * 
	 * <pre>
	 * {@link #getProjection(float[], int, float, float)
	 * getProjection(dest, offset, 1, 0)}
	 * </pre>
	 * 
	 * @return The horizontal projection.
	 */
	void getHorizontalProjection(float[] dest, int offset) {
		float[] verts = getVertices();
		
		float min = verts[0];
		float max = min;
		
		for(int i = 2; i < verts.length; i += 2) {
			if(verts[i] < min)
				min = verts[i];
			else if(verts[i] > max)
				max = verts[i];
		}
		
		dest[offset]   = min;
		dest[offset+1] = max;
	}
	
	/**
	 * Gets the vertical projection for this shape.
	 * 
	 * <p>The returned ShapeProjection is equivalent to the one returned as if
	 * by:
	 * 
	 * <pre>
	 * {@link #getProjection(float[], int, float, float)
	 * getProjection(dest, offset, 0, 1)}
	 * </pre>
	 * 
	 * @return The vertical projection.
	 */
	void getVerticalProjection(float[] dest, int offset) {
		float[] verts = getVertices();
		
		float min = verts[1];
		float max = min;
		
		for(int i = 3; i < verts.length; i += 2) {
			if(verts[i] < min)
				min = verts[i];
			else if(verts[i] > max)
				max = verts[i];
		}
		
		dest[offset]   = min;
		dest[offset+1] = max;
	}
	
	/**
	 * Gets this shape's projection on its own i<font size="-1"><sup>th</sup>
	 * </font> axis.
	 * 
	 * @param i The axis number.
	 * 
	 * @return The shape's projection.
	 * @throws ArrayIndexOutOfBoundsException if {@code i} is negative or
	 * greater than {@code n-1}, where {@code n} is the shape's number of
	 * projection axes as returned by {@link #getAxes()} (that is, {@code n ==
	 * getAxes().length}).
	 */
	protected boolean projectionIntersects(int i, float min, float max) {
		// Note: This shouldn't be used without precomputation!!
		// This implementation is merely offered for convenience.
		
		// Much nicer implementation with objects... RIP
		//return getProjection(getAxes()[i]).intersects(p);
		
		float[] proj = new float[2];
		float[] axes = getAxes();
		getProjection(proj, 0, axes[i], axes[i + 1]);
		return projsIntersect(proj[0], proj[1], min, max);
	}
	
	/**
	 * Gets this shape's projection on its own i<font size="-1"><sup>th</sup>
	 * </font> axis.
	 * 
	 * @param i The axis number.
	 * 
	 * @return The shape's projection.
	 * @throws ArrayIndexOutOfBoundsException if {@code i} is negative or
	 * greater than {@code n-1}, where {@code n} is the shape's number of
	 * projection axes as returned by {@link #getAxes()} (that is, {@code n ==
	 * getAxes().length}).
	 */
	protected boolean projectionContains(int i, float min, float max) {
		// Note: This shouldn't be used without precomputation!!
		// This implementation is merely offered for convenience.
		
		// Much nicer implementation with objects... RIP
		//return getProjection(getAxes()[i]).contains(p);
		
		float[] proj = new float[2];
		float[] axes = getAxes();
		getProjection(proj, 0, axes[i], axes[i + 1]);
		return projContains(proj[0], proj[1], min, max);
	}
	
	protected boolean projectionContainsPoint(int i, float p) {
		// Note: This shouldn't be used without precomputation!!
		// This implementation is merely offered for convenience.
		
		float[] proj = new float[2];
		float[] axes = getAxes();
		getProjection(proj, 0, axes[i], axes[i + 1]);
		return projContainsPoint(proj[0], proj[1], p);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append('{');
		float[] verts = getVertices();
		if(verts != null) {
			for(int i = 0; i < verts.length; i += 2) {
				sb.append('[');
				sb.append(verts[i]);
				sb.append(',');
				sb.append(verts[i+1]);
				sb.append(']');
				if(i < verts.length - 1)
					sb.append(',');
			}
		}
		sb.append('}');
		return sb.toString();
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
	
	/**
	 * Deposits the projection axes of verts into dest. This is O(n).
	 * 
	 * @param verts
	 * @param dest
	 * 
	 * @throws ArrayIndexOutOfBoundsException if dest.length < verts.length
	 */
	static void generateAxes(float[] verts, float[] dest) {
		int n = verts.length - 2;
		for(int i = 2; i < n; i += 2)
			getAxis(dest, i, verts[i-2], verts[i-1], verts[i], verts[i+1]);
		// Finally we add in the axis connecting the first and last vertices.
		getAxis(dest, 0, verts[0], verts[1], verts[n], verts[n+1]);
	}
	
	/**
	 * Gets the projection axis for two adjacent vertices. This takes the form
	 * of a vector perpendicular to the edge joining the vertices.
	 * 
	 * @param dest The destination array.
	 * @param offset The array index offset for this axis.
	 * @param x1 The x-coordinate of the first vertex.
	 * @param y1 The y-coordinate of the first vertex.
	 * @param x2 The x-coordinate of the second vertex.
	 * @param y2 The y-coordinate of the second vertex.
	 * 
	 * @throws NullPointerException if {@code dest} is {@code null}.
	 * @throws ArrayIndexOutOfBoundsException if {@code offset >=
	 * dest.length -1 }.
	 */
	static void getAxis(float[] dest, int offset, float x1, float y1,
			float x2, float y2) {
		// This is what we're really doing:
		//return v1.sub(v2).rotate90Degrees();
		// However, if we simplify that algebraically, we get:
		dest[offset]   = y2 - y1;
		dest[offset+1] = x1 - x2;
	}
	
	/**
	 * This is O(n<font size=-1><sup>2</sup></font>).
	 */
	static void genProjections(float[] verts, float[] axes, float[] dest) {
		for(int i = 0; i < axes.length; i += 2)
			getProjection(verts, axes[i], axes[i+1], dest, i);
	}
	
	static void getProjection(float[] verts, float x, float y, float[] dest, int offset) {
		float min = x*verts[0] + y*verts[1]; // dot product <=> projection
		float max = min;
		float p;
		
		for(int i = 2; i < verts.length; i += 2) {
			p = x*verts[i] + y*verts[i+1]; // dot product <=> projection
			if(p < min)
				min = p;
			else if(p > max)
				max = p;
		}
		
		dest[offset]   = min;
		dest[offset+1] = max;
	}
	
	static boolean projectionsOverlap(float[] verts, float x, float y,
			float projMin, float projMax) {
		float min = x*verts[0] + y*verts[1]; // dot product <=> projection
		float max = min;
		float p;
		
		for(int i = 2; i < verts.length; i += 2) {
			p = x*verts[i] + y*verts[i+1]; // dot product <=> projection
			if(p < min)
				min = p;
			else if(p > max)
				max = p;
		}
		
		return min <= projMax && max >= projMin;
	}
	
	/**
	 * Checks for whether or not two projections intersect.
	 * 
	 * @param min1 proj1.min
	 * @param max1 proj1.max
	 * @param min2 proj2.min
	 * @param max2 proj2.max
	 * 
	 * @return {@code true} if the two projections intersect.
	 */
	static boolean projsIntersect(float min1, float max1,
			float min2, float max2) {
		return min1 <= max2 && max2 >= min2;
	}
	
	/**
	 * Checks for whether or not one projection contains another.
	 * 
	 * @param min1 proj1.min
	 * @param max1 proj1.max
	 * @param min2 proj2.min
	 * @param max2 proj2.max
	 * 
	 * @return {@code true} if proj1 contains proj2.
	 */
	static boolean projContains(float min1, float max1,
			float min2, float max2) {
		return min1 <= min2 && max1 >= max2;
	}
	
	/**
	 * Checks for whether or not a projected point lies within a projection.
	 * 
	 * @param min proj.min
	 * @param max proj.max
	 * @param p The projection of a point.
	 * 
	 * @return {@code true} if the projection contains the point.
	 */
	static boolean projContainsPoint(float min, float max, float p) {
		return min <= p && max >= p;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A blank implementation of Shape used by {@link #NO_SHAPE}.
	 */
	private static final class NoShape extends Shape {
		@Override public Shape transform(Matrix2 matrix) { return this; }
		@Override public Shape transform(VertexFunction f) { return this; }
		@Override public Shape translate(float x, float y) { return this; }
		@Override protected float[] getVertices() { return new float[0]; }
		@Override protected float[] getAxes() { return new float[0]; }
		@Override public boolean intersects(Shape s) { return false; }
		@Override public boolean contains(Shape s) { return false; }
		@Override public boolean containsPoint(float x, float y) { return false; }
		@Override public Shape reflect() { return this; }
	}
	
	@FunctionalInterface
	public static interface VertexFunction {
		
		/**
		 * Transforms a vertex. The resultant {@code x} should be placed in
		 * {@code dest[offset]}, and the resultant {@code y} should be placed
		 * in {@code dest[offset+1]}. Alternatively, you can use {@link
		 * #setX(float[], int, float)} to set the resultant x and {@link
		 * #setY(float[], int, float)} to set the resultant y, if you wish to
		 * avoid interacting with the destination array directly.
		 * 
		 * @param dest The destination array in which to store the result.
		 * @param offset The destination array offset.
		 * @param x The x component of the input vertex.
		 * @param y The y component of the input vertex.
		 */
		public void apply(float[] dest, int offset, float x, float y);
		
		/**
		 * Sets the resultant x in the destination array. This should only be
		 * invoked from within {@link #apply(float[], int, float, float)
		 * apply}, with the {@code dest} and {@code offset} args forwarded to
		 * this methd.
		 * 
		 * @param dest The destination array in which to store the result.
		 * @param offset The destination array offset.
		 * @param x The x component of the output vertex.
		 */
		default public void setX(float[] dest, int offset, float x) {
			dest[offset] = x;
		}
		
		/**
		 * Sets the resultant y in the destination array. This should only be
		 * invoked from within {@link #apply(float[], int, float, float)
		 * apply}, with the {@code dest} and {@code offset} args forwarded to
		 * this methd.
		 * 
		 * @param dest The destination array in which to store the result.
		 * @param offset The destination array offset.
		 * @param x The y component of the output vertex.
		 */
		default public void setY(float[] dest, int offset, float y) {
			dest[offset+1] = y;
		}
		
	}
	
}
