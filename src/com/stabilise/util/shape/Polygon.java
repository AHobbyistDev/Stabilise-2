package com.stabilise.util.shape;

import java.util.function.UnaryOperator;

import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.maths.Vec2;

/**
 * A polygon is a shape with any number of vertices.
 * 
 * <p>The worst-case expensiveness of a polygon to compute scales linearly
 * with its number of vertices.
 */
@NotThreadSafe
public class Polygon extends AbstractPolygon {
    
    /** The polygon's vertices. */
    protected Vec2[] vertices;
    
    /** The projection axes - lazily initialised by getAxes(). */
    protected Vec2[] axes;
    /** The projections corresponding to each axis - lazily initialised by
     * getAxes(). */
    protected ShapeProjection[] projections;
    
    
    /**
     * Creates a new Polygon.
     * 
     * @param vertices The polygon's vertices. These should be indexed such
     * that adjacent vertices are adjacent in the array, with the first vertex
     * also adjacent to the last vertex.
     * 
     * @throws NullPointerException if {@code vertices} or any of its elements
     * are {@code null}.
     * @throws IllegalArgumentException if {@code vertices.length < 3}.
     */
    public Polygon(Vec2... vertices) {
        if(vertices.length < 3)
            throw new IllegalArgumentException("vertices.length < 3");
        for(int i = 0; i < vertices.length; i++)
            if(vertices[i] == null)
                throw new NullPointerException("A vertex is null");
        
        this.vertices = vertices;
    }
    
    /**
     * Constructor to be used when checking the vertex array would be pointless
     * and wasteful.
     */
    protected Polygon() {}
    
    @Override
    public Shape transform(UnaryOperator<Vec2> f) {
        return newInstance(transformVertices(f));
    }
    
    @Override
    public Vec2[] getVertices() {
        return vertices;
    }
    
    // Precomputification -----------------------------------------------------
    
    @Override
    protected ShapeProjection getProjection(int i) {
        return projections[i];
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
    protected Vec2[] genAxes() {
        axes = super.getAxes();
        genProjections();
        return axes;
    }
    
    /** Gens the projections. */
    protected void genProjections() {
        projections = new ShapeProjection[axes.length];
        for(int i = 0; i < axes.length; i++)
            projections[i] = getProjection(axes[i]);
    }
    
    @Override
    protected Vec2[] getAxes() {
        return axes == null ? genAxes() : axes;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Returns a new shape instance of the same class as this one, for
     * duplication in transformation purposes.
     */
    protected Polygon newInstance() {
        return new Polygon();
    }
    
    /**
     * As with {@link #newInstance()}, but sets its vertices without performing
     * any safety checks.
     * 
     * <p>This method hooks on to {@link #newInstance()}, so override it
     * instead.
     */
    protected Polygon newInstance(Vec2[] vertices) {
        Polygon p = newInstance();
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
        if(!(o instanceof Polygon)) return false;
        Polygon p = (Polygon)o;
        if(vertices.length != p.vertices.length) return false;
        for(int i = 0; i < vertices.length; i++)
            if(!vertices[i].equals(p.vertices[i]))
                return false;
        return true;
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
    public static Polygon newPolygon(Vec2[] vertices) {
        Polygon p = new Polygon();
        p.vertices = vertices;
        return p;
    }
    
}
