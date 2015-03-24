package com.stabilise.util.shape;

import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Vec2;

/**
 * An axis-aligned bounding box is a rectangular bounding volume bound to the
 * x and y axes, such that collision detection may be simplified.
 * 
 * <p>For cases where regular reference to an AABB's width and height are not
 * required, it may be preferable to use a {@link LightAABB} instead of an
 * AxisAlignedBoundingBox.
 */
public class AxisAlignedBoundingBox extends Rectangle implements AABB {
	
	/** The AABB's dimensions. These values aren't stricly needed as they can
	 * be computed from the vertices, but they are cached as a memory -> 
	 * computation time tradeoff. */
	public final float width,  height;
	
	
	/**
	 * Creates a new AxisAlignedBoundingBox. It is implicitly trusted that the
	 * given vertices form a valid AABB. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param v00 The AABB's bottom-left vertex.
	 * @param v01 The AABB's top-left vertex.
	 * @param v10 The AABB's bottom-right vertex.
	 * @param v11 The AABB's top-right vertex.
	 * 
	 * @throws NullPointerException if any of the vertices are {@code null}.
	 */
	public AxisAlignedBoundingBox(Vec2 v00, Vec2 v01, Vec2 v10, Vec2 v11) {
		super(v00, v01, v10, v11);
		width = v10.x - v01.x;
		height = v11.y - v00.y;
		calcProjections();
	}
	
	/**
	 * Creates a new AxisAlignedBoundingBox. It is implicitly trusted that the
	 * given vertices form a valid AABB. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param vertices The AABB's vertices. These should be indexed
	 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v11,
	 * and vertices[3] is v01.
	 * 
	 * @throws NullPointerException if {@code vertices} or any of its elements
	 * are {@code null}.
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public AxisAlignedBoundingBox(Vec2... vertices) {
		super(vertices);
		this.width = vertices[V11].x - vertices[V00].x;
		this.height = vertices[V11].y - vertices[V00].y;
		calcProjections();
	}
	
	/**
	 * Creates a new AxisAlignedBoundingBox.
	 * 
	 * @param x The x-coordinate of the AABB's bottom-left vertex.
	 * @param y The y-coordinate of the AABB's bottom-left vertex.
	 * @param width The AABB's width. This should be positive.
	 * @param height The AABB's height. This should be positive.
	 */
	public AxisAlignedBoundingBox(float x, float y, float width, float height) {
		super(x, y, width, height);
		this.width = width;
		this.height = height;
		calcProjections();
	}
	
	/**
	 * Convenience constructor which does NOT initialise the vertices.
	 */
	private AxisAlignedBoundingBox(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	private void calcProjections() {
		projections = new ShapeProjection[] {
				new ShapeProjection(vertices[V00].x, vertices[V11].x),
				new ShapeProjection(vertices[V00].y, vertices[V11].y)
		};
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalArgumentException if the given transformation matrix
	 * produces a shape which is not an AABB.
	 */
	@Override
	public AxisAlignedBoundingBox transform(Matrix2 matrix) {
		AxisAlignedBoundingBox aabb = (AxisAlignedBoundingBox)super.transform(matrix);
		if(!aabb.isValid())
			throw new IllegalArgumentException("Invalid transformation");
		return aabb;
	}
	
	/**
	 * Transforms this AABB by applying the given transformation matrix to each
	 * of its vertices. Unlike {@link #transform(Matrix2f)}, this method
	 * accepts matrices which would transform this into an invalid AABB; thus,
	 * a {@code Polygon} is returned, as if by {@link
	 * Polygon#transform(Matrix2f)}.
	 * 
	 * @param matrix The transformation matrix.
	 * 
	 * @return The transformed quadrilateral.
	 */
	public Polygon transformSafe(Matrix2 matrix) {
		return new Polygon(getTransformedVertices(matrix));
	}
	
	@Override
	public AxisAlignedBoundingBox translate(float offsetX, float offsetY) {
		return new AxisAlignedBoundingBox(
				getV00().x + offsetX,
				getV00().y + offsetY,
				width, height
		);
	}
	
	@Override
	public AxisAlignedBoundingBox reflect() {
		return new AxisAlignedBoundingBox(-vertices[V11].x, vertices[V00].y, width, height);
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
	 * Calculates whether or not this AABB intersects with another.
	 * 
	 * @return {@code true} if this intersects with {@code a}; {@code false}
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
		return x >= getV00().x && x <= getV11().x 
				&& y >= getV00().y && y <= getV11().y;
	}
	
	@Override
	protected Vec2[] getAxes() {
		return Maths.UNIT_VECTORS;
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
		return vertices[V00];
	}
	
	@Override
	public Vec2 getV11() {
		return vertices[V11];
	}
	
	/**
	 * Checks for whether or not this is a valid AABB. 
	 * 
	 * <!-- [TODO: something about floating point rounding errors] -->
	 * 
	 * @return {@code true} if this AABB is valid; {@code false} otherwise.
	 */
	public boolean isValid() {
		return vertices[V00].x == vertices[V01].x
				&& vertices[V10].x == vertices[V11].x
				&& vertices[V00].y == vertices[V10].y
				&& vertices[V01].y == vertices[V11].y;
	}
	
	@Override
	protected AxisAlignedBoundingBox newInstance() {
		throw new UnsupportedOperationException("Can't use an empty constructor"
				+ " for an AABB as width and height are final fields!");
	}
	
	@Override
	protected AxisAlignedBoundingBox newInstance(Vec2[] verts) {
		AxisAlignedBoundingBox aabb = new AxisAlignedBoundingBox(
				verts[V11].x - verts[V00].x,
				verts[V11].y - verts[V00].y
		);
		aabb.vertices = verts;
		aabb.calcProjections();
		return aabb;
	}
	
}
