package com.stabilise.util.shape;


import java.util.Arrays;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.maths.Matrix2;

/**
 * A shape is a 2D object usually consisting of a number of vertices, which may
 * be used to represent such things as collision areas.
 * 
 * <p>Classes in the {@code Shape} hierarchy are immutable, but may be {@link
 * #transform(VertexFunction) transformed} to generate a new shape.
 * 
 * <p>These classes use the <a
 * href=http://en.wikipedia.org/wiki/Hyperplane_separation_theorem> Separating
 * Axis Theorem/Hyperplane Separation Theorem</a> for collision detection.
 * 
 * <p>A shape which does not yet have its collision data generated is not safe
 * for multithreaded use. If you wish to use a shape across multiple threads,
 * make sure you invoke {@link #genCollisionData()} before sharing it to ensure
 * correct behaviour.
 */
public abstract class Shape {
    
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
    
    /*
    public Shape transformSelf(VertexFunction f) {
        float[] verts = getVertices();
        transformVerts(verts, verts, f);
        return this;
    }
    */
    
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
        return transformVerts(getVertices(), f);
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
        final float cos = MathUtils.cos(rads);
        final float sin = MathUtils.sin(rads);
        return transform((dest,o,x,y) -> {
            dest[o]   = x*cos - y*sin;
            dest[o+1] = x*sin + y*cos;
        });
    }
    
    /**
     * Gets a new shape identical to this one, but translated.
     * 
     * @param x The translation along the x-axis.
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
    public final boolean intersects(Shape s) {
        return Collider.intersects(this, s);
    }
    
    /**
     * Calculates whether or not this shape, translated by <tt>(dx,dy)</tt>,
     * intersects with another.
     * 
     * @return {@code true} if this shape intersects with {@code s}; {@code
     * false} otherwise.
     * @throws NullPointerException if {@code s} is {@code null}.
     */
    public final boolean intersects(Shape s, float dx, float dy) {
        return Collider.intersects(this, s, dx, dy);
    }
    
    /**
     * Calculates whether or this shape contains the specified shape.
     * 
     * @return {@code true} if this shape contains {@code s}; {@code false}
     * otherwise.
     * @throws NullPointerException if {@code s} is {@code null}.
     */
    //public abstract boolean contains(Shape s);
    
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
    public boolean containsPoint(float x, float y) { return false; }
    
    /**
     * Gets this shape's vertices.
     * 
     * <p>The vertices are returned in the order they physically connect - that
     * is, consecutive vertices in the array (first and last are considered
     * consecutive) are joined by edges.
     */
    protected abstract float[] getVertices();
    
    /**
     * Returns a copy of this shape's vertices.
     */
    public final float[] cpyVertices() {
        float[] v = getVertices();
        return Arrays.copyOf(v, v.length);
    }
    
    /**
     * Forces this shape to generate its collision data, if it has not been
     * generated already.
     * 
     * <p>A shape which does not yet have its collision data generated is not
     * safe for multithreaded use. If you wish to use a shape across multiple
     * threads, make sure you invoke this method before sharing it to ensure
     * correct behaviour.
     */
    @NotThreadSafe
    public void genCollisionData() {
       // nothing to see here in the default implementation; move along 
    }
    
    /** For use by {@link Collider}. */
    abstract int getKey();
    
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
     * Transforms the given array of vertices with {@code f} and returns the
     * result. {@code verts} will not be modified.
     * 
     * @throws NullPointerException if either argument is null.
     */
    static float[] transformVerts(float[] verts, VertexFunction f) {
        float[] dest = new float[verts.length];
        transformVerts(verts, dest, f);
        return dest;
    }
    
    /**
     * Transforms the given array of vertices with {@code f} and stores the
     * result in {@code dest}. Unless {@code verts == dest}, verts will not be
     * modified.
     * 
     * @throws NullPointerException if any argument is null.
     * @throws ArrayIndexOutOfBoundsException if {@code dest.length <
     * verts.length}.
     */
    static void transformVerts(float[] verts, float[] dest, VertexFunction f) {
        for(int i = 0; i < verts.length; i += 2)
            f.apply(dest, i, verts[i], verts[i+1]);
    }
    
    /**
     * Calculates the projection axes of {@code verts} and stores them in
     * {@code dest}.
     * 
     * @throws NullPointerException if either argument is {@code null}.
     * @throws ArrayIndexOutOfBoundsException if {@code dest.length <
     * verts.length}
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
     * dest.length - 1}.
     */
    private static void getAxis(float[] dest, int offset, float x1, float y1,
            float x2, float y2) {
        // This is what we're really doing:
        // v1.sub(v2).rotate90Degrees();
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
        /*
         * It is worth noting that for projecting a shape onto one of its own
         * axes, the two vertices which generated said axis will have identical
         * projections, and thus we could potentially save computation time by
         * only projecting one of them. However, such an "optimisation" will
         * likely only produce unnecessary overhead and complexity, and so it's
         * not worth bothering with. Besides, projection of a single vertex is
         * pretty cheap already.
         */
        
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
        /*
         * Note: This function is identical to (and thus duplicated from)
         * getProjection() asides from the final part, where we test for
         * overlap with another projection rather than dump the resultant data
         * into an array.
         */
        
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
    
    static boolean projectionsOverlapAABB(float[] verts, float x, float y,
            float projMin, float projMax) {
        float p1 = x*verts[AABB.XMIN] + y*verts[AABB.YMIN];
        float p2 = x*verts[AABB.XMAX] + y*verts[AABB.YMIN];
        float p3 = x*verts[AABB.XMAX] + y*verts[AABB.YMAX];
        float p4 = x*verts[AABB.XMIN] + y*verts[AABB.YMAX];
        
        return Math.min(Math.min(p1, p2), Math.min(p3, p4)) <= projMax
            && Math.max(Math.max(p1, p2), Math.max(p3, p4)) >= projMin;
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * A VertexFunction is a special function 
     */
    @FunctionalInterface
    public static interface VertexFunction {
        
        /**
         * Transforms a vertex. The resultant {@code x} should be placed in
         * {@code dest[offset]}, and the resultant {@code y} should be placed
         * in {@code dest[offset+1]}. Alternatively, you can use {@link
         * #set(float[], int, float, float)} to set the resultant x and y, if
         * you wish to avoid interacting with the destination array directly.
         * 
         * @param dest The destination array in which to store the result.
         * @param offset The destination array offset.
         * @param x The x component of the input vertex.
         * @param y The y component of the input vertex.
         */
        public void apply(float[] dest, int offset, float x, float y);
        
        /**
         * Sets the resultant x and y in the destination array. This should
         * only be invoked from within {@link #apply(float[],int,float,float)
         * apply}, with the {@code dest} and {@code offset} args forwarded to
         * this methd.
         * 
         * @param dest The destination array in which to store the result.
         * @param offset The destination array offset.
         * @param x The x component of the output vertex.
         * @param y The y component of the output vertex.
         */
        default void set(float[] dest, int offset, float x, float y) {
            dest[offset]   = x;
            dest[offset+1] = y;
        }
        
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
