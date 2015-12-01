package com.stabilise.util.shape;

import java.util.function.BiPredicate;

import javax.annotation.concurrent.ThreadSafe;

import com.stabilise.util.maths.Maths;

/**
 * A class which facilitates shape collision computation.
 */
@ThreadSafe
public class Collider {
    
    private Collider() { throw new AssertionError(); } // non-instantiable
    
    /** Collision tester - normal. */
    private static interface ColTestNor extends BiPredicate<Shape, Shape> {}
    /** Collision tester w/ offset/translation. */
    private static interface ColTestOff {
        /** Tests for shape intersection with s1 offset by (dx,dy). */
        boolean test(Shape s1, Shape s2, float dx, float dy);
    }
    
    // vtable for fast dynamic collision types
    private static final int TAB_SIZE = 4;
    private static final int TAB_SHIFT = Maths.log2(TAB_SIZE) / 2;
    private static final ColTestNor[] TAB_NOR;
    private static final ColTestOff[] TAB_OFF;
    
    // Keys
    static final int K_POLY = 0;
    static final int K_AABB = 1;
    
    static {
        // Initialise the vtable
        TAB_NOR = new ColTestNor[TAB_SIZE];
        TAB_OFF = new ColTestOff[TAB_SIZE];
        
        TAB_NOR[keyFor(K_POLY, K_POLY)] = (s1,s2) -> intersectsPoly((Polygon)s1,(Polygon)s2);
        TAB_NOR[keyFor(K_POLY, K_AABB)] = (s1,s2) -> intersectsAABBPoly((AABB)s2,(Polygon)s1);
        TAB_NOR[keyFor(K_AABB, K_POLY)] = (s1,s2) -> intersectsAABBPoly((AABB)s1,(Polygon)s2);
        TAB_NOR[keyFor(K_AABB, K_AABB)] = (s1,s2) -> intersectsAABB((AABB)s1,(AABB)s2);
        
        TAB_OFF[keyFor(K_POLY, K_POLY)] = (s1,s2,x,y) -> intersectsPoly((Polygon)s1,(Polygon)s2,x,y);
        TAB_OFF[keyFor(K_POLY, K_AABB)] = (s1,s2,x,y) -> intersectsAABBPoly((AABB)s2,(Polygon)s1,-x,-y);
        TAB_OFF[keyFor(K_AABB, K_POLY)] = (s1,s2,x,y) -> intersectsAABBPoly((AABB)s1,(Polygon)s2,x,y);
        TAB_OFF[keyFor(K_AABB, K_AABB)] = (s1,s2,x,y) -> intersectsAABB((AABB)s1,(AABB)s2,x,y);
    }
    
    private static int keyFor(int k1, int k2) {
        return (k1 << TAB_SHIFT) | k2;
    }
    
    /*
     * Optimisations which can be made:
     * 
     * - A translated shape shares axes with its parent.
     *     - Foreseen complexity: parent shape may not yet have axes generated.
     * - A translated shape can derive its self-projections from its parent
     *   by simple O(n) addition (O(1) per projection). Note, however, that
     *   this could lead to significant rounding errors if applied repeatedly!
     *     - Foreseen complexity: parent shape may not yet have projections
     *       generated.
     * - Vertex arrays can be reused.
     * - Cache optimisation [do not do until more experienced]
     * - We could create a big vertex array which is shared between shapes, but
     *   then we have the problem of memory reuse/fragmentation/etc (i.e. we'd
     *   basically be making our own garbage collector and memory allocator).
     *   It'd just be simpler to allocate a new vertex array for every new
     *   new polygon.
     * - When generating self-projection onto an axis, it is only necessary to
     *   project one of the two vertices which generated the axis (as they have
     *   identical projections on their axis). However, the overhead of
     *   ensuring we only project the correct n-1 vertices (instead of n) will
     *   probably overshadow the win of not calculating a proj.
     */
    
    /*
     * Notes on algorithmic complexity:
     * 
     * Transformation: O(n)
     * Axis generation: O(n)
     * Projection onto an axis: O(n)
     *     Genning all projections: O(n^2)
     *     Genning projections of a translated shape using the projections of
     *         the original: O(n)
     */
    
    /**
     * Tests for shape intersection.
     * 
     * @return {@code true} if the shapes intersect; {@code false} otherwise.
     * @throws NullPointerException if either shape is {@code null}.
     */
    public static boolean intersects(Shape s1, Shape s2) {
        return TAB_NOR[keyFor(s1.getKey(), s2.getKey())].test(s1, s2);
    }
    
    /**
     * Tests for shape intersection, with {@code s1} translated by <tt>(dx,dy)
     * </tt>.
     * 
     * @return {@code true} if the shapes intersect; {@code false} otherwise.
     * @throws NullPointerException if either shape is {@code null}.
     */
    public static boolean intersects(Shape s1, Shape s2, float dx, float dy) {
        return TAB_OFF[keyFor(s1.getKey(), s2.getKey())].test(s1, s2, dx, dy);
    }
    
    /**
     * Tests for polygon intersection.
     * 
     * @return {@code true} if the polygons intersect; {@code false} otherwise.
     * @throws NullPointerException if either polygon is {@code null}.
     */
    public static boolean intersectsPoly(Polygon p1, Polygon p2) {
        return intersectsOnPolyAxes(p1, p2) && intersectsOnPolyAxes(p2, p1);
    }
    
    /**
     * Tests for polygon intersection, with {@code p1} translated by <tt>
     * (dx,dy)</tt>.
     * 
     * @return {@code true} if the polygons intersect; {@code false} otherwise.
     * @throws NullPointerException if either polygon is {@code null}.
     */
    public static boolean intersectsPoly(Polygon p1, Polygon p2, float dx, float dy) {
        int n = p1.verts.length;
        float off; // projection offset for p1
        
        // If p1 hasn't generated its axes & projections, we gen them.
        p1.genCollisionData();
        
        // Now, we test for collision using the axes of p1
        for(int i = 0; i < n; i += 2) {
            // Since p1 is translated its projection will be offset on each
            // axis. By distributivity of the dot product we just add the
            // translation's projection to each of p1's respective projections.
            off = dx*p1.axes[i] + dy*p1.axes[i+1]; // dot product
            // We simultaneously project p2 onto p1s axes and test for
            // projection overlap. If they don't, we know immediately that the
            // shapes don't intersect.
            if(!Shape.projectionsOverlap(p2.verts, p1.axes[i], p1.axes[i+1],
                    p1.projs[i]+off, p1.projs[i+1]+off))
                return false;
        }
        
        // Now we move onto testing with the projections of p2...
        n = p2.verts.length;
        
        // If p2 hasn't generated its axes & projections, we gen them
        p2.genCollisionData();
        
        // Now, we test for collision using the axes of p2
        for(int i = 0; i < n; i += 2) {
            // The offset for p1, as above
            off = dx*p2.axes[i] + dy*p2.axes[i+1]; // dot product
            // Test for projection overlap, as above. Instead of adding offset
            // to p1s projection, we subtract it from p2s.
            if(!Shape.projectionsOverlap(p1.verts, p2.axes[i], p2.axes[i+1],
                    p2.projs[i]-off, p2.projs[i+1]-off))
                return false;
        }
        
        // All tests passed = shapes intersect
        return true;
    }
    
    /**
     * Tests for intersection between an AABB and a polygon.
     * 
     * @return {@code true} if the shapes intersect; {@code false} otherwise.
     * @throws NullPointerException if either argument is {@code null}.
     */
    public static boolean intersectsAABBPoly(AABB b, Polygon p) {
        return intersectsOnAABBAxes(p, b, 0, 0) && intersectsOnPolyAxes(p, b);
    }
    
    /** tests for intersects with the AABB offset by (dy,dx) */
    public static boolean intersectsAABBPoly(AABB b, Polygon p, float dx, float dy) {
        return intersectsOnAABBAxes(p, b, dx, dy) && intersectsOnPolyAxes(p, b, dx, dy);
    }
    
    /**
     * Tests for AABB intersection.
     * 
     * @return {@code true} if the AABBs intersect; {@code false} otherwise.
     * @throws NullPointerException if either AABB is {@code null}.
     */
    public static boolean intersectsAABB(AABB b1, AABB b2) {
        return b1.minX() <= b2.maxX() && b2.minX() <= b1.maxX()
            && b1.minY() <= b2.maxY() && b2.minY() <= b1.maxY();
    }
    
    /**
     * Tests for AABB intersection, with {@code b1} translated by <tt>(dx,dy)
     * </tt>.
     * 
     * @return {@code true} if the AABBs intersect; {@code false} otherwise.
     * @throws NullPointerException if either AABB is {@code null}.
     */
    public static boolean intersectsAABB(AABB b1, AABB b2, float dx, float dy) {
        return (b1.minX() + dx) <= b2.maxX() && b2.minX() <= (b1.maxX() + dx)
            && (b1.minY() + dy) <= b2.maxY() && b2.minY() <= (b1.maxY() + dy);
    }
    
    /** Tests for intersection using the axes of p1 */
    private static boolean intersectsOnPolyAxes(Polygon p1, Polygon p2) {
        // If p1 hasn't generated its axes & projections, we gen them.
        p1.genCollisionData();
        
        // Now, we test for collision using the axes of p1
        for(int i = 0; i < p1.axes.length; i += 2) {
            // Test for projection overlap. If the projections don't overlap,
            // we know immediately that the shapes don't intersect.
            if(!Shape.projectionsOverlap(p2.verts, p1.axes[i], p1.axes[i+1],
                    p1.projs[i], p1.projs[i+1]))
                return false;
        }
        
        // All tests passed = shapes appear to intersect.
        return true;
    }
    
    /** Tests for intersection using the axes of p */
    private static boolean intersectsOnPolyAxes(Polygon p, AABB b) {
        // If p hasn't generated its axes & projections, we gen them.
        p.genCollisionData();
        
        // Now, we test for collision using the axes of p
        for(int i = 0; i < p.axes.length; i += 2) {
            // Test for projection overlap. If the projections don't overlap,
            // we know immediately that the shapes don't intersect.
            if(!Shape.projectionsOverlapAABB(b.verts, p.axes[i], p.axes[i+1],
                    p.projs[i], p.projs[i+1]))
                return false;
        }
        
        // All tests passed = shapes appear to intersect.
        return true;
    }
    
    /** Tests for intersection using the axes of p with b offset by (dx,dy) */
    private static boolean intersectsOnPolyAxes(Polygon p, AABB b, float dx, float dy) {
        // If p hasn't generated its axes & projections, we gen them.
        p.genCollisionData();
        
        float off;
        
        // Now, we test for collision using the axes of p
        for(int i = 0; i < p.axes.length; i += 2) {
            off = dx*p.axes[i] + dy*p.axes[i+1];
            // Test for projection overlap. If the projections don't overlap,
            // we know immediately that the shapes don't intersect.
            if(!Shape.projectionsOverlapAABB(b.verts, p.axes[i], p.axes[i+1],
                    p.projs[i]-off, p.projs[i+1]-off))
                return false;
        }
        
        // All tests passed = shapes appear to intersect.
        return true;
    }
    
    /** Tests for intersection using the axes of b, with b offset by (dx,dy) */
    private static boolean intersectsOnAABBAxes(Polygon p, AABB b, float dx, float dy) {
        float min = p.verts[0];
        float max = min;
        
        // Get the x projections of p
        for(int i = 2; i < p.verts.length; i += 2) {
            if(p.verts[i] < min)
                min = p.verts[i];
            else if(p.verts[i] > max)
                max = p.verts[i];
        }
        
        if(min > (b.maxX() + dx) || max < (b.minX() + dx)) return false;
        
        // Get the y projections of p
        min = max = p.verts[1]; // reset
        for(int i = 3; i < p.verts.length; i += 2) {
            if(p.verts[i] < min)
                min = p.verts[i];
            else if(p.verts[i] > max)
                max = p.verts[i];
        }
        
        if(min > (b.maxY() + dy) || max < (b.minY() + dy)) return false;
        
        // All tests passed = shapes appear to intersect.
        return true;
    }
    
}
