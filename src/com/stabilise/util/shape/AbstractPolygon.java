package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;

/**
 * A polygon is a shape with any number of vertices.
 */
abstract class AbstractPolygon extends PrecomputableShape {
	
	@Override
	public boolean intersects(Shape s) {
		if(s instanceof AbstractPolygon)
			return intersects((AbstractPolygon)s);
		if(s instanceof Circle)
			return intersectsOnOwnAxes(s);
		
		// TODO: Could result in an infinite loop if the other shape does the same thing
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
	public boolean intersects(AbstractPolygon p) {
		return intersectsOnOwnAxes(p) && p.intersectsOnOwnAxes(this);
	}
	
	/**
	 * Calculates whether or not two shapes appear to intersect based on the
	 * axes of this polygon. Note that even if this returns {@code true}, the
	 * shapes may not necessarily intersect, as in all but a few special
	 * cases (e.g. two axis-aligned bounding boxes), the axes of both shapes
	 * need to be checked. Refer instead to - in the case of polygons -
	 * {@link #intersects(Polygon)} to check for a collision using the
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
	 * <p>Precomputed variants of an AbstractPolygon should override this
	 * method to delegate it to {@link #intersectsOnOwnAxesPrecomputed(Shape)}.
	 * 
	 * @param s The shape with which to test intersection.
	 * 
	 * @return {@code true} if this polygon 'intersects' with the given shape;
	 * {@code false} if it does not.
	 */
	protected boolean intersectsOnOwnAxes(Shape s) {
		Vector2[] axes = generateAxes();
		for(Vector2 axis : axes) {
			if(!getProjection(axis).overlaps(s.getProjection(axis)))
				return false;
		}
		return true;
	}
	
	/**
	 * As, {@link #intersectsOnOwnAxes(Shape)}, but optimised for
	 * precomputation.
	 * 
	 * @param s The shape with which to test intersection.
	 * 
	 * @return {@code true} if this polygon 'intersects' with the given shape;
	 * {@code false} if it does not.
	 */
	@ForPrecomputedVariant
	protected boolean intersectsOnOwnAxesPrecomputed(Shape s) {
		for(int i = 0; i < getAxes().length; i++) {
			if(!getProjection(i).overlaps(s.getProjection(getAxes()[i])))
				return false;
		}
		return true;
	}
	
}
