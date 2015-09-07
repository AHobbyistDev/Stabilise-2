package com.stabilise.util.shape2;

/**
 * A polygon is a shape with any number of vertices.
 * 
 * <p>This class provides standard implementations of the SAT algorithm for
 * collision detection.
 */
abstract class AbstractPolygon extends Shape {
    
    @Override
    public boolean intersects(Shape s) {
        if(s instanceof AbstractPolygon)
            return intersectsPolygon((AbstractPolygon)s);
        if(s instanceof Circle)
            return intersectsOnOwnAxes(s);
        
        // TODO: Could result in an infinite loop if the other shape does the
        // same thing
        return s.intersects(this);
    }
    
    /**
     * Calculates whether or not two polygons intersect.
     * 
     * <p>This method has a minimum computation time of O(1) and a maximum of
     * O(n+m), where n is this polygon's number of projection axes, and m is
     * the number of projection axes of the other polygon. If the polygons
     * intersect, the computation time will always be O(n+m), and if they do
     * not, the computation time may be anywhere between O(1) and O(n+m).
     * 
     * <p>Note that this may return a false positive if either of the polygons
     * are not convex.
     * 
     * @param p The polygon with which to test intersection.
     * 
     * @return {@code true} if the two polygons intersect; {@code false}
     * otherwise.
     */
    public boolean intersectsPolygon(AbstractPolygon p) {
        return intersectsOnOwnAxes(p) && p.intersectsOnOwnAxes(this);
    }
    
    /**
     * Calculates whether or not two shapes appear to intersect based on the
     * axes of this polygon. Note that even if this returns {@code true}, the
     * shapes may not necessarily intersect, as in all but a few special
     * cases (e.g. two axis-aligned bounding boxes), the axes of both shapes
     * need to be checked. Refer instead to - in the case of polygons -
     * {@link #intersectsPolygon(AbstractPolygon)} to check for a collision using the
     * axes of both shapes.
     * 
     * <p>This method has a minimum computation time of O(1) and a maximum of
     * O(n), where n is this polygon's number of projection axes. If the
     * shapes intersect, the computation time will always be O(n), and if
     * they do not, the computation time may be anywhere between O(1) and O(n).
     * 
     * <p>Note that this may return a false positive if either of the shapes
     * are not convex.
     * 
     * <p>Subclasses of AbstractPolygon which utilise precomputation should
     * override this method to delegate it to {@link
     * #intersectsOnOwnAxesPrecomputed(Shape)}.
     * 
     * @param s The shape with which to test intersection.
     * 
     * @return {@code true} if this polygon 'intersects' with the given shape;
     * {@code false} if it does not.
     */
    protected boolean intersectsOnOwnAxes(Shape s) {
        System.out.println("intersectsOnOwnAxes SHOULD NOT BE A THING THAT IS USED");
        /*
        Vec2[] axes = getAxes();
        for(Vec2 axis : axes) {
            if(!getProjection(axis).intersects(s.getProjection(axis)))
                return false;
        }
        return true;
        */
        float[] axes = getAxes();
        float[] proj = new float[4]; // min1, max1, min2, max2
        for(int i = 0; i < axes.length; i += 2) {
            getProjection(proj, 0, axes[i], axes[i+1]);
            s.getProjection(proj, 2, axes[i], axes[i+1]);
            if(!projsIntersect(proj[0], proj[1], proj[2], proj[3]))
                return false;
        }
        return true;
    }
    
    /**
     * As {@link #intersectsOnOwnAxes(Shape)}, but optimised for
     * precomputation.
     * 
     * @param s The shape with which to test intersection.
     * 
     * @return {@code true} if this polygon 'intersects' with the given shape;
     * {@code false} if it does not.
     */
    protected boolean intersectsOnOwnAxesPrecomputed(Shape s) {
        /*
        Vec2[] axes = getAxes();
        for(int i = 0; i < axes.length; i++) {
            if(!getProjection(i).intersects(s.getProjection(axes[i])))
                return false;
        }
        return true;
        */
        float[] axes = getAxes();
        float[] proj = new float[2]; // min, max
        for(int i = 0; i < axes.length; i += 2) {
            s.getProjection(proj, 0, axes[i], axes[i+1]);
            if(!projectionIntersects(i, proj[0], proj[1]))
                return false;
        }
        return true;
    }
    
    @Override
    public boolean contains(Shape s) {
        /*
        Vec2[] axes = getAxes();
        for(Vec2 axis : axes) {
            if(!getProjection(axis).contains(s.getProjection(axis)))
                return false;
        }
        return true;
        */
        float[] axes = getAxes();
        float[] proj = new float[4]; // min1, max1, min2, max2
        for(int i = 0; i < axes.length; i += 2) {
            getProjection(proj, 0, axes[i], axes[i+1]);
            s.getProjection(proj, 2, axes[i], axes[i+1]);
            if(!projContains(proj[0], proj[1], proj[2], proj[3]))
                return false;
        }
        return true;
    }
    
    /**
     * As, {@link #contains(Shape)}, but optimised for precomputation.
     * 
     * @return {@code true} if this polygon contains the given shape; {@code
     * false} otherwise.
     */
    protected boolean containsPrecomputed(Shape s) {
        /*
        Vec2[] axes = getAxes();
        for(int i = 0; i < axes.length; i++) {
            if(!getProjection(i).contains(s.getProjection(axes[i])))
                return false;
        }
        return true;
        */
        float[] axes = getAxes();
        float[] proj = new float[2]; // min, max
        for(int i = 0; i < axes.length; i += 2) {
            s.getProjection(proj, 0, axes[i], axes[i+1]);
            if(!projectionContains(i, proj[0], proj[1]))
                return false;
        }
        return true;
    }
    
    @Override
    public boolean containsPoint(float x, float y) {
        /*
        Vec2[] axes = getAxes();
        for(Vec2 axis : axes) {
            if(!getProjection(axis).containsPoint(axis.dot(x, y)))
                return false;
        }
        return true;
        */
        float[] axes = getAxes();
        float[] proj = new float[3]; // min, max, p
        for(int i = 0; i < axes.length; i += 2) {
            getProjection(proj, 0, axes[i], axes[i+1]);
            proj[2] = x*axes[i] + y*axes[i+1]; // dot product
            if(!projContainsPoint(proj[0], proj[1], proj[2]))
                return false;
        }
        return true;
    }
    
    /**
     * As with {@link #containsPoint(float, float)}, but optimised for a shape
     * which precomputes its projections.
     * 
     * <p>Subclasses should override {@link #containsPoint(float, float)} and
     * redirect it to this method if said subclass utilises precomputation.
     */
    protected boolean containsPointPrecomputed(float x, float y) {
        /*
        Vec2[] axes = getAxes();
        for(int i = 0; i < axes.length; i++) {
            if(!getProjection(i).containsPoint(axes[i].dot(x, y)))
                return false;
        }
        return true;
        */
        float[] axes = getAxes();
        for(int i = 0; i < axes.length; i += 2) {
            if(!projectionContainsPoint(i, x*axes[i] + y*axes[i+1]))
                return false;
        }
        return true;
    }
    
}
