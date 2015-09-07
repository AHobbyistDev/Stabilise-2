package com.stabilise.util.shape2;

import java.util.Arrays;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A polygon is a shape with any number of vertices.
 * 
 * <p>The worst-case expensiveness of a polygon to compute scales linearly
 * with its number of vertices.
 */
@NotThreadSafe
public class Polygon2 extends AbstractPolygon {
    
    /** The polygon's vertices. */
    protected float[] vertices;
    
    /** The projection axes - lazily initialised by getAxes(). */
    protected float[] axes;
    /** The projections corresponding to each axis - lazily initialised by
     * getAxes(). */
    protected float[] projections;
    
    
    /**
     * Creates a new Polygon.
     * 
     * @param vertices The polygon's vertices. These should be indexed such
     * that adjacent vertices are adjacent in the array, with the first vertex
     * also adjacent to the last vertex.
     * 
     * @throws NullPointerException if {@code vertices} is {@code null}.
     * @throws IllegalArgumentException if {@code vertices.length < 6}, or is
     * an odd number.
     */
    public Polygon2(float... vertices) {
        if((vertices.length & 1) != 0)
            throw new IllegalArgumentException("vertices must be an"
                    + "even-numbered array");
        if(vertices.length < 6)
            throw new IllegalArgumentException("vertices.length < 6");
        
        this.vertices = vertices;
    }
    
    /**
     * Constructor to be used when checking the vertex array would be pointless
     * and wasteful.
     */
    protected Polygon2() {}
    
    @Override
    public Shape transform(VertexFunction f) {
        return newInstance(transformVertices(f));
    }
    
    @Override
    public float[] getVertices() {
        return vertices;
    }
    
    // Precomputification -----------------------------------------------------
    
    protected boolean projectionIntersects(int i, float min, float max) {
        return projsIntersect(projections[i], projections[i+1], min, max);
    }
    
    protected boolean projectionContains(int i, float min, float max) {
        return projContains(projections[i], projections[i+1], min, max);
    }
    
    protected boolean projectionContainsPoint(int i, float p) {
        return projContainsPoint(projections[i], projections[i+1], p);
    }
    
    @Override
    protected boolean intersectsOnOwnAxes(Shape s) {
        return intersectsOnOwnAxesPrecomputed(s);
    }
    
    @Override
    public boolean contains(Shape s) {
        return containsPrecomputed(s);
    }
    
    @Override
    public boolean containsPoint(float x, float y) {
        return containsPointPrecomputed(x,y);
    }
    
    /** Gens the axes. */
    protected float[] genAxes() {
        axes = super.genAxes();
        genProjections();
        return axes;
    }
    
    /** Gens the projections. */
    protected void genProjections() {
        projections = new float[axes.length];
        for(int i = 0; i < axes.length; i += 2)
            getProjection(projections, i, axes[i], axes[i+1]);
    }
    
    @Override
    protected float[] getAxes() {
        return axes == null ? genAxes() : axes;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Returns a new shape instance of the same class as this one, for
     * duplication in transformation purposes.
     */
    protected Polygon2 newInstance() {
        return new Polygon2();
    }
    
    /**
     * As with {@link #newInstance()}, but sets its vertices without performing
     * any safety checks.
     * 
     * <p>This method hooks on to {@link #newInstance()}, so override it
     * instead.
     */
    protected Polygon2 newInstance(float[] vertices) {
        Polygon2 p = newInstance();
        p.vertices = vertices;
        return p;
    }
    
    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return -1;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Polygon2)) return false;
        Polygon2 p = (Polygon2)o;
        if(vertices.length != p.vertices.length) return false;
        return Arrays.equals(vertices, p.vertices);
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Constructs and returns a new Polygon with the specified vertices. The
     * returned polygon is equivalent to one constructed as if by
     * {@link #Polygon(Vec2[]) new Polygon(vertices)}, however, this method
     * provides faster performance as the supplied vertices are not checked for
     * validity. As such, constructing a Polygon using this method is suitable
     * only when it is known that the supplied vertices are valid.
     * 
     * @param vertices The polygon's vertices.
     * 
     * @return The new polygon.
     */
    public static Polygon2 newPolygon(float[] vertices) {
        Polygon2 p = new Polygon2();
        p.vertices = vertices;
        return p;
    }
    
}
