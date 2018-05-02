package com.stabilise.util.shape;

import com.stabilise.util.Checks;
import com.stabilise.util.annotation.Incomplete;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.maths.Matrix2;

/**
 * For the uninitiated: <i>A circle is a simple shape of Euclidean geometry
 * that is the set of all points in a plane that are at a given distance from a
 * given point, the centre. The distance between any of the points and the
 * centre is called the radius. It can also be defined as the locus of a point
 * equidistant from a fixed point.</i>
 */
@Incomplete
class Circle extends Shape {
    
    /** The coordinates of the centre of the circle. */
    public final float[] c;
    /** The circle's radius. */
    public final float radius;

    
    /**
     * Creates a new Circle, with a default radius of 0.
     * 
     * @param centre The centre of the circle.
     */
    public Circle(float[] centre) {
        this(centre, 0);
    }
    
    /**
     * Creates a new Circle.
     * 
     * @param centre The circle's centre.
     * @param radius The circle's radius.
     */
    public Circle(float[] centre, float radius) {
        c = centre;
        this.radius = radius;
    }
    
    /**
     * Creates a new Circle object.
     * 
     * @param x The x-coordinate of the circle's centre.
     * @param y The y-coordinate of the circle's centre.
     * @param radius The circle's radius.
     */
    public Circle(float x, float y, float radius) {
        this(new float[] {x, y}, radius);
    }
    
    public float x() {
        return c[0];
    }
    
    public float y() {
        return c[1];
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
    public Circle transform(VertexFunction f) {
        float[] newC = new float[2];
        f.apply(newC, 0, c[0], c[1]);
        return new Circle(newC, radius);
    }
    
    /**
     * Returns this circle.
     */
    @Override
    public Circle reflect() {
        return this;
    }
    
    @Override
    protected float[] getVertices() {
        return c;
    }
    
    /**
     * Calculates whether or not this circle intersects with another.
     * 
     * @return {@code true} if this intersects with {@code c}; {@code false}
     * otherwise.
     */
    public boolean intersects(Circle c) {
        if(radius == 0) return false;
        float dx = x() - c.x();
        float dy = y() - c.y();
        float radii = radius + c.radius;
        return dx*dx + dy*dy <= radii*radii;
    }
    
    /**
     * Calculates whether or not this circle contains another.
     * 
     * @return {@code true} if this contains {@code c}; {@code false}
     * otherwise.
     */
    public boolean contains(Circle c) {
        if(c.radius > radius) return false;
        float dx = x() - c.x();
        float dy = y() - c.y();
        float dr = radius - c.radius;
        // radius >= sqrt(dx*dx + dy*dy) + c.radius
        // radius - c.radius >= sqrt(dx*dx + dy*dy)
        // We want to avoid computing sqrt so instead we use
        // (radius - c.radius)^2 >= dx*dx + dy*dy,
        // Which requires the c.radius <= radius check at the start.
        return dr*dr >= dx*dx + dy*dy;
    }
    
    @Override
    public boolean containsPoint(float x, float y) {
        float dx = x() - x;
        float dy = y() - y;
        return dx*dx + dy*dy <= radius*radius;
    }
    
    protected void getProjection(float[] dest, int offset, float x, float y) {
        // A circle, being a uniform shape, is of constant width for all axes
        float mid = x()*x + y()*y;
        // TODO + or - radius doesn't work unless be divide by |axis|
        dest[offset]   = mid - radius;
        dest[offset+1] = mid + radius;
    }
    
    protected void getHorizontalProjection(float[] dest, int offset) {
        dest[offset]   = x() - radius;
        dest[offset+1] = x() + radius;
    }
    
    protected void getVerticalProjection(float[] dest, int offset) {
        dest[offset]   = y() - radius;
        dest[offset+1] = y() + radius;
    }

    @Override
    protected int getKey() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void importFromCompound(DataCompound dc) {
        c[0] = dc.getF32("cx");
        c[1] = dc.getF32("cy");
        //radius = dc.getFloat("r"); // is immutable sadly, watdo?
        Checks.TODO(); // todo
    }
    
    @Override
    public void exportToCompound(DataCompound dc) {
        dc.put("cx", c[0]);
        dc.put("cy", c[1]);
        dc.put("r", radius);
    }
    
}
