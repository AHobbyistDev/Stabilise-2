package com.stabilise.util.shape;

import java.util.function.UnaryOperator;

import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Vec2;

/**
 * An Axis-Aligned Bounding Box, or AABB for short, is a rectangle whose edges
 * are aligned with the x and y axes. AABBs are typically much faster than
 * ordinary polygons due to the large number of assumptions that can be made
 * about their behaviour.
 * 
 * <p>AABB is not a member of the {@code Polygon} hierarchy as to avoid the
 * limitations imposed by Polygon, and more effectively introduce
 * optimisations.
 */
public class AABB extends AbstractPolygon {
    
    /** An array containing the unit vectors {@link #VEC_X} and {@link #VEC_Y}. */
    private static final Vec2[] UNIT_VECTORS = new Vec2[] {
        Maths.VEC_X, Maths.VEC_Y
    };
    
    /** The min and max vertices of this AABB. */
    public final Vec2 v00, v11;
    
    
    /**
     * Creates a new AABB. It is implicitly trusted that the given values for
     * {@code width} and {@code height} are non-negative.
     * 
     * @param x The x-coordinate of the AABB's bottom-left vertex.
     * @param y The y-coordinate of the AABB's bottom-left vertex.
     * @param width The AABB's width.
     * @param height The AABB's height.
     */
    public AABB(float x, float y, float width, float height) {
        v00 = Vec2.immutable(x, y);
        v11 = Vec2.immutable(x + width, y + height);
    }
    
    /**
     * Creates a new AABB.
     * 
     * @param v00 The min vertex (i.e. bottom left) of the AABB.
     * @param v11 The max vertex (i.e. top right) of the AABB.
     */
    public AABB(Vec2 v00, Vec2 v11) {
        this.v00 = v00;
        this.v11 = v11;
    }
    
    @Override
    public AABB transform(UnaryOperator<Vec2> f) {
        return new AABB(f.apply(v00), f.apply(v11));
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public AABB rotate(float rotation) {
        throw new UnsupportedOperationException("Cannot rotate an AABB!");
    }
    
    @Override
    public AABB reflect() {
        // We need to manually do this since AABB breaks if v00 is to the right
        // of v11, as we make algorithmic simplifications on the assumption
        // that v00 is always to the left of v11.
        return new AABB(
                Vec2.immutable(-v11.x(), v00.y()),
                Vec2.immutable(-v00.x(), v11.y())
        );
    }
    
    /*
    private Vec2[] genVertices() {
        return Vec2.immutable[] {
                v00,
                Vec2.immutable(v11.x(), v00.y()),//v10
                v11,
                Vec2.immutable(v00.x(), v11.y()) //v01
        };
    }
    */
    
    @Override
    protected Vec2[] getVertices() {
        //throw new UnsupportedOperationException("AABB should have no need "
        //        + "for getVertices()!");
        return new Vec2[] {
                v00,
                Vec2.immutable(v11.x(), v00.y()),//v10
                v11,
                Vec2.immutable(v00.x(), v11.y()) //v01
        };
    }
    
    @Override
    public boolean intersectsPolygon(AbstractPolygon p) {
        if(p instanceof AABB)
            return intersectsAABB((AABB)p);
        return super.intersectsPolygon(p);
    }
    
    @Override
    protected boolean intersectsOnOwnAxes(Shape s) {
        //return getHorizontalProjection().intersects(s.getHorizontalProjection()) &&
        //        getVerticalProjection().intersects(s.getVerticalProjection());
        return s.getHorizontalProjection().intersects(v00.x(), v11.x()) &&
                s.getVerticalProjection().intersects(v00.y(), v11.y());
    }
    
    /**
     * Calculates whether or not two axis-aligned bounding boxes intersect.
     * 
     * @param a The AABB with which to test intersection.
     * 
     * @return {@code true} if the two AABBs intersect; {@code false}
     * otherwise.
     */
    public boolean intersectsAABB(AABB a) {
        return v00.x() <= a.v11.x() && v11.x() >= a.v00.x()
                && v00.y() <= a.v11.y() && v11.y() >= a.v00.y();
    }
    
    @Override
    public boolean contains(Shape s) {
        if(s instanceof AABB)
            return containsAABB((AABB)s);
        return getHorizontalProjection().contains(s.getHorizontalProjection()) &&
                getVerticalProjection().contains(s.getVerticalProjection());
    }
    
    /**
     * Calculates whether or not this AABB contains the specified AABB.
     * 
     * @return {@code true} if this AABB contains {@code a}; {@code false}
     * otherwise.
     */
    public boolean containsAABB(AABB a) {
        return v00.x() <= a.v00.x() && v11.x() >= a.v11.x()
                && v00.y() <= a.v00.y() && v11.y() >= a.v11.y();
    }
    
    @Override
    public boolean containsPoint(float x, float y) {
        return x >= v00.x() && x <= v11.x() && y >= v00.y() && y <= v11.y();
    }
    
    @Override
    protected Vec2[] getAxes() {
        return UNIT_VECTORS;
    }
    
    @Override
    protected ShapeProjection getProjection(Vec2 axis) {
        // This method of computation is preferable to the default impl.,
        // which invokes getVertices().
        
        float p0 = axis.dot(v00);
        float p1 = axis.dot(v11);
        float p2 = axis.dot(v00.x(), v11.y());
        float p3 = axis.dot(v11.x(), v00.y());
        
        return new ShapeProjection(
                Maths.min(Maths.min(p0, p1), Maths.min(p2, p3)),
                Maths.max(Maths.max(p0, p1), Maths.max(p2, p3))
        );
    }
    
    @Override
    ShapeProjection getHorizontalProjection() {
        return new ShapeProjection(v00.x(), v11.x());
    }
    
    @Override
    ShapeProjection getVerticalProjection() {
        return new ShapeProjection(v00.y(), v11.y());
    }
    
    /**
     * Gets the x-coordinate of the bottom-left vertex - or the origin - of
     * this AABB.
     * 
     * @return The x-coordinate of this AABB's origin.
     */
    public float getOriginX() {
        return v00.x();
    }
    
    /**
     * Gets the y-coordinate of the bottom-left vertex - or the origin - of
     * this AABB.
     * 
     * @return The y-coordinate of this AABB's origin.
     */
    public float getOriginY() {
        return v00.y();
    }
    
    /**
     * Gets the x-coordinate of the top-right vertex of this AABB.
     */
    public float getMaxX() {
        return v11.x();
    }
    
    /**
     * Gets the y-coordinate of the top-right vertex of this AABB.
     */
    public float getMaxY() {
        return v11.y();
    }
    
    /**
     * Calculates and returns the width of this AABB.
     */
    public float width() {
        return v11.x() - v00.x();
    }
    
    /**
     * Calculates and returns the height of this AABB.
     */
    public float height() {
        return v11.y() - v00.y();
    }
    
}
