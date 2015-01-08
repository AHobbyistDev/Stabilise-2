package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.util.maths.Matrix2;

/**
 * An axis-aligned bounding box is a rectangular bounding volume bound to the
 * x and y axes, such that collision detection may be simplified.
 * 
 * <p>For cases where regular reference to an AABB's width and height are not
 * required, it may be preferable to use a {@link LightweightAABB} instead of an
 * AxisAlignedBoundingBox.
 */
public class AxisAlignedBoundingBox extends Rectangle implements AABB {
	
	/** The AABB's width. */
	public final float width;
	/** The AABB's height. */
	public final float height;
	
	
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
	public AxisAlignedBoundingBox(Vector2 v00, Vector2 v01, Vector2 v10, Vector2 v11) {
		this(v00, v01, v10, v11, v10.x - v01.x, v11.y - v00.y);
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
	 * 
	 * @throws NullPointerException if any of the vertices are {@code null}.
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public AxisAlignedBoundingBox(Vector2 v00, Vector2 v01, Vector2 v10, Vector2 v11,
			float width, float height) {
		super(v00, v01, v10, v11);
		this.width = width;
		this.height = height;
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
	public AxisAlignedBoundingBox(Vector2[] vertices) {
		super(vertices);
		this.width = vertices[V11].x - vertices[V00].x;
		this.height = vertices[V11].y - vertices[V00].y;
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
	}
	
	/**
	 * Creates an AABB from a precomputed variant.
	 */
	private AxisAlignedBoundingBox(Precomputed aabb) {
		vertices = aabb.vertices;
		width = aabb.width;
		height = aabb.height;
	}
	
	/**
	 * Convenience constructor.
	 */
	protected AxisAlignedBoundingBox(float width, float height) {
		super();
		this.width = width;
		this.height = height;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>This method fails fast by throwing an {@code
	 * IllegalArgumentException} if the given matrix transforms this into an
	 * invalid AABB.
	 * 
	 * @throws IllegalArgumentException if the given transformation matrix
	 * produces an invalid AABB.
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
	protected Vector2[] generateAxes() {
		return MathsUtil.UNIT_VECTORS;
	}
	
	@Override
	ShapeProjection getHorizontalProjection() {
		return new ShapeProjection(vertices[V00].x, vertices[V11].x);
	}
	
	@Override
	ShapeProjection getVerticalProjection() {
		return new ShapeProjection(vertices[V00].y, vertices[V11].y);
	}
	
	@Override
	protected boolean isAABB() {
		return true;
	}
	
	@Override
	public Vector2 getV00() {
		return vertices[V00];
	}
	
	@Override
	public Vector2 getV11() {
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
	public AxisAlignedBoundingBox precomputed() {
		return new Precomputed(this);
	}
	
	// Overriding for typecast purposes
	@Override
	public AxisAlignedBoundingBox notPrecomputed() {
		return this;
	}
	
	@Override
	protected Rectangle newInstance() {
		throw new UnsupportedOperationException();
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
		private ShapeProjection[] projections;
		
		
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
		public Precomputed(Vector2 v00, Vector2 v01, Vector2 v10, Vector2 v11) {
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
		public Precomputed(Vector2 v00, Vector2 v01, Vector2 v10, Vector2 v11,
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
		 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is
		 * v11, and vertices[3] is v01.
		 * 
		 * @throws IllegalArgumentException if {@code vertices.length != 4}.
		 */
		public Precomputed(Vector2[] vertices) {
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
			super(aabb.width, aabb.height);
			vertices = aabb.vertices;
			calculateProjections();
		}
		
		/**
		 * Calculates the shape's projection axes, and their respective
		 * projections.
		 */
		private void calculateProjections() {
			projections = new ShapeProjection[] {
					super.getHorizontalProjection(),
					super.getVerticalProjection()
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
			return new AxisAlignedBoundingBox(this);
		}
		
	}
	
}
