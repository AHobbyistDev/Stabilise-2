package com.stabilise.util.shape;

import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Vec2;

/**
 * A LightAABB is a slightly more lightweight variant of {@link
 * AxisAlignedBoundingBox}, which is overall generally less expensive to use.
 * 
 * <p>Unlike AxisAlignedBoundingBox, LightAABB is not a member of the
 * Polygon hierarchy as to avoid limitations imposed by superclasses.
 */
public class LightAABB extends AbstractPolygon implements AABB {
	
	/** The min and max vertices of this AABB. */
	public final Vec2 v00, v11;
	
	/** Array of vertices - lazily initialised by getVertices(). */
	private Vec2[] vertices;
	/** Self-projections. */
	private ShapeProjection[] projections;
	
	
	/**
	 * Creates a new AABB.
	 * 
	 * @param x The x-coordinate of the AABB's bottom-left vertex.
	 * @param y The y-coordinate of the AABB's bottom-left vertex.
	 * @param width The AABB's width.
	 * @param height The AABB's height.
	 */
	public LightAABB(float x, float y, float width, float height) {
		v00 = new Vec2(x, y);
		v11 = new Vec2(x + width, y + height);
		calcProjections();
	}
	
	/**
	 * Creates a new AABB.
	 * 
	 * @param v00 The min vertex (i.e. bottom left) of the AABB.
	 * @param v11 The max vertex (i.e. top right) of the AABB.
	 */
	public LightAABB(Vec2 v00, Vec2 v11) {
		this.v00 = v00;
		this.v11 = v11;
		calcProjections();
	}
	
	private void calcProjections() {
		projections = new ShapeProjection[] {
				new ShapeProjection(v00.x, v11.x),
				new ShapeProjection(v00.y, v11.y)
		};
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Note that as a LightAABB is defined in terms of two vertices, the
	 * returned LightAABB will retain the properties of an AABB, but its min
	 * and max vertices will be transformed as per the matrix.
	 */
	@Override
	public LightAABB transform(Matrix2 matrix) {
		return newInstance(
				matrix.transform(v00),
				matrix.transform(v11)
		);
	}
	
	@Override
	public LightAABB translate(float x, float y) {
		return newInstance(
				new Vec2(v00.x + x, v00.y + y),
				new Vec2(v11.x + x, v11.y + y)
		);
	}
	
	@Override
	public LightAABB reflect() {
		return newInstance(
				new Vec2(-v11.x, v00.y),
				new Vec2(-v00.x, v11.y)
		);
	}
	
	private Vec2[] genVertices() {
				return new Vec2[] {
				v00,
				new Vec2(v11.x, v00.y),//v10
				v11,
				new Vec2(v00.x, v11.y) //v01
		};
	}
	
	@Override
	protected Vec2[] getVertices() {
		return vertices == null ? vertices = genVertices() : vertices;
	}
	
	@Override
	public boolean intersects(AbstractPolygon p) {
		if(p.isAABB())
			return intersectsAABB((AABB)p);
		return super.intersects(p);
	}
	
	@Override
	protected boolean intersectsOnOwnAxes(Shape s) {
		return getHorizontalProjection().intersects(s.getHorizontalProjection()) &&
				getVerticalProjection().intersects(s.getVerticalProjection());
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
		return getV00().x <= a.getV11().x && getV11().x >= a.getV00().x
				&& getV00().y <= a.getV11().y && getV11().y >= a.getV00().y;
	}
	
	@Override
	public boolean contains(Shape s) {
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
		return getV00().x <= a.getV00().x && getV11().x >= a.getV11().x
				&& getV00().y <= a.getV00().y && getV11().y >= a.getV11().y;
	}
	
	@Override
	public boolean containsPoint(float x, float y) {
		return x >= v00.x && x <= v11.x && y >= v00.y && y <= v11.y;
	}
	
	@Override
	protected Vec2[] getAxes() {
		return Maths.UNIT_VECTORS;
	}
	
	@Override
	protected ShapeProjection getProjection(Vec2 axis) {
		// This method of computation is preferable to the default impl.,
		// which invokes getVertices(). We want to avoid getVertices() if
		// possible.
		
		float p0 = axis.dot(v00);
		float p1 = axis.dot(v11);
		float p2 = axis.dot(v00.x, v11.y);
		float p3 = axis.dot(v11.x, v00.y);
		
		return new ShapeProjection(
				Maths.min(Maths.min(p0, p1), Maths.min(p2, p3)),
				Maths.max(Maths.max(p0, p1), Maths.max(p2, p3))
		);
	}
	
	@Override
	ShapeProjection getHorizontalProjection() {
		return projections[0];
	}
	
	@Override
	ShapeProjection getVerticalProjection() {
		return projections[1];
	}
	
	@Override
	public Vec2 getV00() {
		return v00;
	}
	
	@Override
	public Vec2 getV11() {
		return v11;
	}
	
	/**
	 * Gets the x-coordinate of the bottom-left vertex - or the origin - of
	 * this AABB.
	 * 
	 * @return The x-coordinate of this AABB's origin.
	 */
	public float getOriginX() {
		return v00.x;
	}
	
	/**
	 * Gets the y-coordinate of the bottom-left vertex - or the origin - of
	 * this AABB.
	 * 
	 * @return The y-coordinate of this AABB's origin.
	 */
	public float getOriginY() {
		return v00.y;
	}
	
	/**
	 * Calculates the width of this AABB.
	 * 
	 * @return The width of this AABB.
	 */
	public float getWidth() {
		return v11.x - v00.x;
	}
	
	/**
	 * Calculates the height of this AABB.
	 * 
	 * @return The height of this AABB.
	 */
	public float getHeight() {
		return v11.y - v00.y;
	}
	
	@Override
	protected LightAABB newInstance() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("use the parametised alternative");
	}
	
	/**
	 * Creates a new LightAABB for duplication purposes. This is used to
	 * generate a new LightAABB whenever a duplicate is needed (i.e.,
	 * {@link #transform(Matrix2f)}, {@link #translate(float, float)},
	 * {@link #reflect()}, etc).
	 * 
	 * @return The new AABB.
	 */
	protected LightAABB newInstance(Vec2 v00, Vec2 v11) {
		return new LightAABB(v00, v11);
	}
	
}
