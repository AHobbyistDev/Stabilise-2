package com.stabilise.util.shape.old;

import java.util.function.UnaryOperator;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.ArrayUtil.ImmutableArray;
import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Vec2;

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
@Deprecated
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
    public abstract Shape transform(UnaryOperator<Vec2> f);
    
    /**
     * Gets the vertices of this shape if it were transformed using the given
     * function. The returned array is the same length as, and ordered the same
     * as, the vertices returned by {@link #getVertices()}.
     * 
     * @param f The transformation function.
     * 
     * @return The transformed vertices. 
     */
    protected Vec2[] transformVertices(UnaryOperator<Vec2> f) {
        final Vec2[] verts = getVertices();
        Vec2[] newVerts = new Vec2[verts.length];
        for(int i = 0; i < verts.length; i++)
            newVerts[i] = f.apply(verts[i]);
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
     * @param matrix The transformation matrix.
     * 
     * @return The transformed shape.
     * @throws NullPointerException if {@code matrix} is {@code null}.
     */
    public Shape transform(Matrix2 matrix) {
        return transform(v -> matrix.transform(v));
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
        return transform(v -> v.rotate(cos, sin, v));
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
        return transform(v -> new Vec2(v.x + x, v.y + y));
    }
    
    /**
     * Clones this shape and reflects the clone about the y-axis.
     * 
     * @return The reflected clone of this shape.
     */
    public Shape reflect() {
        return transform(v -> new Vec2(-v.x, v.y));
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
    protected abstract Vec2[] getVertices();
    
    /**
     * Gets this shape's vertices.
     * 
     * <p>The vertices are returned in the order they physically connect - that
     * is, consecutive vertices in the array (first and last are considered
     * consecutive) are joined by edges.
     */
    public final ImmutableArray<Vec2> vertices() {
        return new ImmutableArray<>(getVertices());
    }
    
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
    protected Vec2[] getAxes() {
        Vec2[] verts = getVertices();
        Vec2[] axes = new Vec2[verts.length];
        for(int i = 0; i < verts.length; i++)
            axes[i] = getAxis(verts[i], verts[i+1 == verts.length ? 0 : i+1]);
        return axes;
    }
    
    /**
     * Gets the shape's projection for a given axis.
     * 
     * @param axis The axis upon which to project the shape.
     * 
     * @return The shape's projection.
     * @throws NullPointerException if {@code axis} is {@code null}.
     */
    protected ShapeProjection getProjection(Vec2 axis) {
        Vec2[] vertices = getVertices();
        
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
    protected ShapeProjection getProjection(int i) {
        //System.out.println("WARNING: Using getProjection(int) without precomputation!");
        return getProjection(getAxes()[i]);
    }
    
    /**
     * Gets the horizontal projection for this shape.
     * 
     * <p>The returned ShapeProjection is equivalent to the one returned as if
     * by:
     * 
     * <pre>
     * {@link #getProjection(Vec2) getProjection(new Vec2(1f, 0f))}
     * </pre>
     * 
     * @return The horizontal projection.
     */
    ShapeProjection getHorizontalProjection() {
        Vec2[] vertices = getVertices();
        
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
     * {@link #getProjection(Vec2) getProjection(new Vec2(0f, 1f))}
     * </pre>
     * 
     * @return The vertical projection.
     */
    ShapeProjection getVerticalProjection() {
        Vec2[] vertices = getVertices();
        
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append('{');
        Vec2[] verts = getVertices();
        if(verts != null) {
            for(int i = 0; i < verts.length; i++) {
                sb.append(verts[i].toString());
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
     * Gets the projection axis for two adjacent vertices. This takes the form
     * of a vector perpendicular to the edge joining the vertices.
     * 
     * @param v1 The first vertex.
     * @param v2 The second vertex.
     * 
     * @return The projection axis.
     * @throws NullPointerException if either argument is {@code null}.
     */
    protected static Vec2 getAxis(Vec2 v1, Vec2 v2) {
        // This is what we're really doing:
        //return v1.sub(v2).rotate90Degrees();
        // However, if we simplify that algebraically, we get:
        return new Vec2(v2.y - v1.y, v1.x - v2.x);
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A blank implementation of Shape used by {@link #NO_SHAPE}.
     */
    private static final class NoShape extends Shape {
        @Override public Shape transform(Matrix2 matrix) { return this; }
        @Override public Shape transform(UnaryOperator<Vec2> f) { return this; }
        @Override public Shape translate(float x, float y) { return this; }
        @Override protected Vec2[] getVertices() { return new Vec2[0]; }
        @Override protected Vec2[] getAxes() { return new Vec2[0]; }
        @Override public boolean intersects(Shape s) { return false; }
        @Override public boolean contains(Shape s) { return false; }
        @Override public boolean containsPoint(float x, float y) { return false; }
        @Override public Shape reflect() { return this; }
    }
    
}
