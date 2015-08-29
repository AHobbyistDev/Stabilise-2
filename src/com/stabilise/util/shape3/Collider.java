package com.stabilise.util.shape3;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * A class which facilitates shape collision computation for a single thread
 * as to provide shared optimisations across threads.
 */
@NotThreadSafe
public class Collider {
	
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
	 *   polygon.
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
	
	
	private Collider() {
		
	}
	
	public static boolean intersects(Polygon3 p1, float x1, float y1, Polygon3 p2,
			float x2, float y2) {
		return intersects(p1, p2, x2-x1, y2-y1);
	}
	
	/**
	 * Checks to see whether or not two polygons intersect. p1 is translated by
	 * (dx, dy) first.
	 */
	public static boolean intersects(Polygon3 p1, Polygon3 p2, float dx, float dy) {
		int n = p1.vertices.length;
		float off; // projection offset for p1
		
		// If p1 hasn't generated its axes & projections, we gen them.
		if(p1.axes == null) {
			Shape.generateAxes(p1.vertices, p1.axes = new float[n]);
			Shape.genProjections(p1.vertices, p1.axes, p1.projs = new float[n]);
		}
		
		// Now, we test for collision using the axes of p1
		for(int i = 0; i < n; i += 2) {
			// Since p1 is translated its projection will be offset on each
			// axis. By distributivity of the dot product we just add the
			// translation's projection to each of p1's respective projections.
			off = dx*p1.axes[i] + dy*p1.axes[i+1]; // dot product
			// We simultaneously project p2 onto p1's axes and test for
			// projection overlap. If they don't, we know immediately that the
			// shapes don't intersect.
			if(!Shape.projectionsOverlap(p2.vertices, p1.axes[i], p1.axes[i+1],
					p1.projs[i]+off, p1.projs[i+1]+off))
				return false;
		}
		
		// Now we move onto testing with the projections of p2...
		n = p2.vertices.length;
		
		// If p2 hasn't generated its axes & projections, we gen them
		if(p2.axes == null) {
			Shape.generateAxes(p2.vertices, p2.axes = new float[n]);
			Shape.genProjections(p2.vertices, p2.axes, p2.projs = new float[n]);
		}
		
		// Now, we test for collision using the axes of p2
		for(int i = 0; i < n; i += 2) {
			// The offset for p1, as above
			off = dx*p2.axes[i] + dy*p2.axes[i+1]; // dot product
			// Test for projection overlap, as above. In preference to adding
			// offset to p1's projection, we subtract it from p2's.
			if(!Shape.projectionsOverlap(p1.vertices, p2.axes[i], p2.axes[i+1],
					p2.projs[i]-off, p2.projs[i+1]-off))
				return false;
		}
		
		// All tests passed = shapes intersect
		return true;
	}
	
}
