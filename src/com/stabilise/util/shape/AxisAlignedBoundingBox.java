package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.MathUtil;

/**
 * An axis-aligned bounding box is a rectangular bounding volume bound to the
 * x and y axes, such that collision detection may be simplified.
 * 
 * <p>For cases where regular reference to an AABB's width and height are not
 * required, it may be preferable to use a {@link FastAABB} instead of an
 * AxisAlignedBoundingBox.
 */
public class AxisAlignedBoundingBox extends DimensionedRectangle implements AABB {
	
	/**
	 * Creates a new AxisAlignedBoundingBox. It is implicitly trusted that the
	 * given vertices form a valid AABB. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param v00 The AABB's bottom-left vertex.
	 * @param v01 The AABB's top-left vertex.
	 * @param v10 The AABB's bottom-right vertex.
	 * @param v11 The AABB's top-right vertex.
	 */
	public AxisAlignedBoundingBox(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		super(v00, v01, v10, v11, v11.x - v00.x, v11.y - v00.y);
	}
	
	/**
	 * Creates a new AxisAlignedBoundingBox. It is implicitly trusted that the
	 * given vertices form a valid AABB. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * <p>This constructor is provided to allow one to explicitly set the
	 * dimensions, if they are known, to save computation time.
	 * 
	 * @param v00 The AABB's bottom-left vertex.
	 * @param v01 The AABB's top-left vertex.
	 * @param v10 The AABB's bottom-right vertex.
	 * @param v11 The AABB's top-right vertex.
	 * @param width The AABB's width.
	 * @param height The AABB's height.
	 */
	public AxisAlignedBoundingBox(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11,
			float width, float height) {
		super(v00, v01, v10, v11, width, height);
	}
	
	/**
	 * Creates a new AxisAlignedBoundingBox. It is implicitly trusted that the
	 * given vertices form a valid AABB. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param vertices The rectangle's vertices. These should be indexed
	 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v10,
	 * and vertices[3] is v11.
	 * 
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public AxisAlignedBoundingBox(Vector2f[] vertices) {
		super(vertices);
	}
	
	/**
	 * Creates a new AxisAlignedBoundingBox.
	 * 
	 * @param x The x-coordinate of the AABB's bottom-left vertex.
	 * @param y The y-coordinate of the AABB's bottom-left vertex.
	 * @param width The AABB's width.
	 * @param height The AABB's height.
	 */
	public AxisAlignedBoundingBox(float x, float y, float width, float height) {
		super(x, y, width, height);
	}
	
	/**
	 * Invoking this method will throw a UnsupportedOperationException, as an
	 * AABB may not be rotated.
	 */
	@Override
	public AxisAlignedBoundingBox rotate(float rotation) {
		throw new UnsupportedOperationException("An axis-aligned bounding box may not be rotated!");
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method fails fast by throwing an
	 * {@code IllegalArgumentException} if the given matrix transforms this
	 * into an invalid AABB.
	 * 
	 * @throws IllegalArgumentException if the given transformation matrix
	 * produces an invalid AABB.
	 */
	@Override
	public AxisAlignedBoundingBox transform(Matrix2f matrix) {
		AxisAlignedBoundingBox aabb = (AxisAlignedBoundingBox)super.transform(matrix);
		if(!aabb.isValid())
			throw new IllegalArgumentException("Invalid transformation");
		return aabb;
	}
	
	/**
	 * Transforms this AABB by applying the given transformation matrix to each
	 * of its vertices. Unlike {@link #transform(Matrix2f)}, this method
	 * accepts matrices which would transform this into an invalid AABB; thus,
	 * a {@code Quadrilateral} is returned, as if by
	 * {@link Quadrilateral#transform(Matrix2f)}.
	 * 
	 * @param matrix The transformation matrix.
	 * 
	 * @return The transformed quadrilateral.
	 */
	public Quadrilateral transformSafe(Matrix2f matrix) {
		return new Quadrilateral(
				transformVertex(matrix, v00),
				transformVertex(matrix, v01),
				transformVertex(matrix, v10),
				transformVertex(matrix, v11)
		);
	}
	
	@Override
	public AxisAlignedBoundingBox translate(float offsetX, float offsetY) {
		return (AxisAlignedBoundingBox)super.translate(offsetX, offsetY);
	}
	
	/*
	@Override
	public AxisAlignedBoundingBox translate(float offsetX, float offsetY) {
		return new AxisAlignedBoundingBox(v00.x + offsetX, v00.y + offsetY, width, height);
	}
	*/
	
	@Override
	public AxisAlignedBoundingBox reflect() {
		return new AxisAlignedBoundingBox(-v10.x, v00.y, width, height);
	}
	
	@Override
	public boolean intersects(AbstractPolygon p) {
		if(p.isAABB())
			return intersects((AABB)p);
		return super.intersects(p);
	}
	
	@Override
	protected boolean intersectsOnOwnAxes(Shape s) {
		return getHorizontalProjection().overlaps(s.getHorizontalProjection()) &&
				getVerticalProjection().overlaps(s.getVerticalProjection());
	}
	
	/**
	 * Calculates whether or not two axis-aligned bounding boxes intersect.
	 * 
	 * @param a The AABB with which to test intersection.
	 * 
	 * @return {@code true} if the two AABBs intersect; {@code false}
	 * otherwise.
	 */
	public boolean intersects(AxisAlignedBoundingBox a) {
		return intersectsAABB(a.v00, a.v11);
	}
	
	/**
	 * Calculates whether or not two axis-aligned bounding boxes intersect.
	 * 
	 * @param a The AABB with which to test intersection.
	 * 
	 * @return {@code true} if the two AABBs intersect; {@code false}
	 * otherwise.
	 */
	public boolean intersects(FastAABB a) {
		return intersectsAABB(a.v00, a.v11);
	}
	
	/**
	 * Calculates whether or not two axis-aligned bounding boxes intersect.
	 * 
	 * @param a The AABB with which to test intersection.
	 * 
	 * @return {@code true} if the two AABBs intersect; {@code false}
	 * otherwise.
	 */
	private boolean intersects(AABB a) {
		return intersectsAABB(a.getV00(), a.getV11());
	}
	
	/**
	 * Calculates whether or not two AABBs intersect based on the min and max
	 * vertices of the other.
	 * 
	 * @param v00 The min vertex (i.e. bottom left) of the other AABB.
	 * @param v11 The max vertex (i.e. top right) of the other AABB.
	 * 
	 * @return {@code true} if the two AABBs intersect; {@code false}
	 * otherwise.
	 */
	private boolean intersectsAABB(Vector2f o00, Vector2f o11) {
		return v00.x <= o11.x && v11.x >= o00.x && v00.y <= o11.y && v11.y >= o00.y;
	}
	
	@Override
	public boolean containsPoint(float x, float y) {
		return x >= v00.x && x <= v11.x && y >= v00.y && y <= v11.y;
	}
	
	@Override
	public boolean containsPoint(Vector2f p) {
		return p.x >= v00.x && p.x <= v11.x && p.y >= v00.y && p.y <= v11.y;
	}
	
	@Override
	protected Vector2f[] generateAxes() {
		return MathUtil.UNIT_VECTORS;
	}
	
	@Override
	public ShapeProjection getHorizontalProjection() {
		return new ShapeProjection(v00.x, v11.x);
	}
	
	@Override
	public ShapeProjection getVerticalProjection() {
		return new ShapeProjection(v00.y, v11.y);
	}
	
	@Override
	protected boolean isAABB() {
		return true;
	}
	
	@Override
	public Vector2f getV00() {
		return v00;
	}
	
	@Override
	public Vector2f getV11() {
		return v11;
	}
	
	/**
	 * Checks for whether or not this is a valid AABB. 
	 * 
	 * <!-- [TODO: something about floating point rounding errors] -->
	 * 
	 * @return {@code true} if this AABB is valid; {@code false} otherwise.
	 */
	public boolean isValid() {
		return v00.x == v01.x && v10.x == v11.x && v00.y == v10.y && v01.y == v11.y;
	}
	
	@Override
	public AxisAlignedBoundingBox precomputed() {
		return new Precomputed(this);
	}
	
	@Override
	protected AxisAlignedBoundingBox newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		return new AxisAlignedBoundingBox(v00, v01, v10, v11);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The precomputed variant of an AxisAlignedBoundingBox.
	 * 
	 * <p>Though an instance of this class may be instantiated directly, its
	 * declared type should simply be that of AxisAlignedBoundingBox.
	 */
	public static final class Precomputed extends AxisAlignedBoundingBox {
		
		/** The shape's projections for each of its axes. */
		protected ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed AxisAlignedBoundingBox. It is implicitly
		 * trusted that the given vertices form a valid AABB. Invalid vertices
		 * may produce undefined behaviour.
		 * 
		 * @param v00 The AABB's bottom-left vertex.
		 * @param v01 The AABB's top-left vertex.
		 * @param v10 The AABB's bottom-right vertex.
		 * @param v11 The AABB's top-right vertex.
		 */
		public Precomputed(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			super(v00, v01, v10, v11);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed AxisAlignedBoundingBox. It is implicitly
		 * trusted that the given vertices form a valid AABB. Invalid vertices
		 * may produce undefined behaviour.
		 * 
		 * <p>This constructor is provided to allow one to explicitly set the
		 * dimensions, if they are known, to save computation time.
		 * 
		 * @param v00 The AABB's bottom-left vertex.
		 * @param v01 The AABB's top-left vertex.
		 * @param v10 The AABB's bottom-right vertex.
		 * @param v11 The AABB's top-right vertex.
		 * @param width The AABB's width.
		 * @param height The AABB's height.
		 */
		public Precomputed(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11,
				float width, float height) {
			super(v00, v01, v10, v11, width, height);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed AxisAlignedBoundingBox. It is implicitly
		 * trusted that the given vertices form a valid AABB. Invalid vertices
		 * may produce undefined behaviour.
		 * 
		 * @param vertices The rectangle's vertices. These should be indexed
		 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v10,
		 * and vertices[3] is v11.
		 * 
		 * @throws IllegalArgumentException if {@code vertices.length != 4}.
		 */
		public Precomputed(Vector2f[] vertices) {
			super(vertices);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed AxisAlignedBoundingBox.
		 * 
		 * @param x The x-coordinate of the AABB's bottom-left vertex.
		 * @param y The y-coordinate of the AABB's bottom-left vertex.
		 * @param width The AABB's width.
		 * @param height The AABB's height.
		 */
		public Precomputed(float x, float y, float width, float height) {
			super(x, y, width, height);
			calculateProjections();
		}
		
		/**
		 * Constructor to be used by AxisAlignedBoundingBox.
		 */
		private Precomputed(AxisAlignedBoundingBox aabb) {
			super(aabb.v00, aabb.v01, aabb.v10, aabb.v11, aabb.width, aabb.height);
			calculateProjections();
		}
		
		/**
		 * Calculates the shape's projection axes, and their respective
		 * projections.
		 */
		private void calculateProjections() {
			projections = new ShapeProjection[] {
					new ShapeProjection(v00.x, v11.x),
					new ShapeProjection(v00.y, v11.y)
			};
		}
		
		@Override
		protected ShapeProjection getProjection(int i) {
			return projections[i];
		}
		
		@Override
		public ShapeProjection getHorizontalProjection() {
			return projections[0];
		}
		
		@Override
		public ShapeProjection getVerticalProjection() {
			return projections[1];
		}
		
		@Override
		public AxisAlignedBoundingBox notPrecomputed() {
			return new AxisAlignedBoundingBox(v00, v01, v10, v11, width, height);
		}
		
		@Override
		protected AxisAlignedBoundingBox newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			return new Precomputed(v00, v01, v10, v11);
		}
		
	}
	
}
