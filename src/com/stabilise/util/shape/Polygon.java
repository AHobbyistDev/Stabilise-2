package com.stabilise.util.shape;

import java.util.Arrays;

import javax.annotation.concurrent.NotThreadSafe;

import com.badlogic.gdx.math.MathUtils;
import com.stabilise.util.Checks;
import com.stabilise.util.maths.Maths;

/**
 * A polygon is a shape with any number of vertices.
 * 
 * <p>The worst-case expensiveness of a polygon to compute scales linearly
 * with its number of vertices.
 */
@NotThreadSafe
public class Polygon extends Shape {
    
    /** The polygon's vertices. */
    float[] verts;
    
    /** The projection axes - lazily initialised by genCollisionData(). */
    float[] axes = null;
    /** The projections corresponding to each axis - lazily initialised by
     * genCollisionData(). */
    float[] projs = null;
    
    
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
    public Polygon(float... vertices) {
        Checks.testMin(vertices.length, 6);
        if((vertices.length & 1) != 0)
            throw new IllegalArgumentException("vertices must be an"
                    + "even-numbered array");
        
        this.verts = vertices;
    }
    
    /**
     * Constructor to be used when checking the vertex array would be pointless
     * and wasteful.
     */
    protected Polygon() {}
    
    @Override
    public Shape transform(VertexFunction f) {
        return newInstance(transformVertices(f));
    }
    
    @Override
    public Shape rotate(float rads) {
        Polygon p = (Polygon)super.rotate(rads);
        if(projs == null) {
            projs = new float[verts.length];
            projs[0] = Float.NaN; // flag value
        }
        p.projs = projs;
        return p;
    }
    
    @Override
    public float[] getVertices() {
        return verts;
    }
    
    @Override
    public void genCollisionData() {
        if(axes == null) {
            Shape.generateAxes(verts, axes = new float[verts.length]);
            if(projs == null)
                Shape.genProjections(verts, axes, projs = new float[verts.length]);
            else if(Float.isNaN(projs[0]))
                Shape.genProjections(verts, axes, projs);
        }
    }
    
    @Override
    int getKey() {
        return Collider.K_POLY;
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
    protected Polygon newInstance(float[] vertices) {
        Polygon p = newInstance();
        p.verts = vertices;
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
        if(verts.length != p.verts.length) return false;
        return Arrays.equals(verts, p.verts);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Polygon: [");
        for(int i = 0; i < verts.length; i += 2) {
            sb.append("\n     (");
            sb.append(String.format("%.2f", verts[i]));
            sb.append(", ");
            sb.append(String.format("%.2f", verts[i+1]));
            sb.append(")");
        }
        sb.append("\n]");
        return sb.toString();
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
    public static Polygon newPolygon(float[] vertices) {
        Polygon p = new Polygon();
        p.verts = vertices;
        return p;
    }
    
    public static Polygon rectangle(float x, float y, float width, float height) {
        Polygon p = new Polygon();
        p.verts = new float[] {
                x, y,
                x + width, y,
                x + width, y + height,
                x, y + height
        };
        return p;
    }
    
    /**
     * Constructs a polygon approximation of a circle with centre (x,y) and
     * with the specified number of points/vertices.
     */
    public static Polygon circle(float x, float y, float radius, int points) {
        Checks.testMin(points, 3);
        float[] verts = new float[2*points];
        float increment = Maths.TAUf / points;
        float angle = increment;
        for(int i = 0; i < 2*points; i += 2) {
            verts[i]   = x + radius * MathUtils.cos(angle);
            verts[i+1] = y + radius * MathUtils.sin(angle);
            angle += increment;
        }
        return newPolygon(verts);
    }
    
}
