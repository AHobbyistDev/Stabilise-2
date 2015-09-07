package com.stabilise.util.shape3;

/**
 * A shape projection represents the 1D 'shadow' cast by a shape when it is
 * projected onto an axis.
 */
class ShapeProjection {
    
    /** The minimum and maximum values/coordinates of the projection. */
    public final float min, max;
    
    
    /**
     * Creates a new ShapeProjection.
     * 
     * @param min The projection's minimum value.
     * @param max The projection's maximum value.
     */
    public ShapeProjection(float min, float max) {
        this.min = min;
        this.max = max;
    }
    
    /**
     * Tests for projection intersection.
     * 
     * <p>This relation has the following properties;
     * 
     * <ul>
     * <li>It is <b>reflexive</b> - i.e. {@code a.intersects(a) == true}.
     * <li>It is <b>symmetric</b> - i.e. {@code a.intersects(b) ==
     *     b.intersects(a)}.
     * </ul>
     * 
     * @param p The projection to compare against.
     * 
     * @return {@code true} if the two projections overlap; {@code false}
     * if they do not.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public boolean intersects(ShapeProjection p) {
        return min <= p.max && max >= p.min;
    }
    
    /**
     * Tests for projection intersection given the components of another
     * projection.
     * 
     * @see #intersects(ShapeProjection)
     */
    public boolean intersects(float min, float max) {
        return this.min <= max && this.max >= min;
    }
    
    /**
     * Tests for containment of another projection.
     * 
     * <p>This relation has the following properties:
     * 
     * <ul>
     * <li>It is <b>reflexive</b> - i.e. {@code a.contains(a) == true}.
     * <li>It is <b>anti-symmetric</b> - i.e. {@code a.contains(b) !=
     *     b.contains(a)} unless {@code a == b}.
     * <li>It is <b>transitive</b> - i.e. {@code a.contains(b) && b.contains(c)
     *     => a.contains(c)}.
     * </ul>
     * 
     * @param p The projection to compare against.
     * 
     * @return {@code true} if this projection contains the other; {@code
     * false} otherwise.
     * @throws NullPointerException if {@code p} is {@code null}.
     */
    public boolean contains(ShapeProjection p) {
        return min <= p.min && max >= p.max;
    }
    
    /**
     * @param x The point.
     * 
     * @return {@code true} if the projection contains the point; {@code false}
     * if it doesn't.
     */
    public boolean containsPoint(float x) {
        return x >= min && x <= max;
    }
    
    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return -1;
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ShapeProjection)) return false;
        ShapeProjection p = (ShapeProjection)o;
        return min == p.min && max == p.max;
    }

}
