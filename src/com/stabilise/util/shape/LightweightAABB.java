package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.maths.MathsUtil;
import com.stabilise.util.maths.Matrix2;

/**
 * A LightweightAABB is a slightly more lightweight variant of {@link
 * AxisAlignedBoundingBox}, which is overall generally less expensive to use.
 * 
 * <p>Unlike AxisAlignedBoundingBox, LightweightAABB is not a member of the
 * Polygon hierarchy as to avoid limitations imposed by superclasses.
 */
public class LightweightAABB extends AbstractPolygon implements AABB {
	
	/** The min vertex (i.e. bottom left) of the AABB. This is exposed for
	 * convenience purposes, and should be treated as if it is immutable. */
	public final Vector2 v00;
	/** The max vertex (i.e. top right) of the AABB. This is exposed for
	 * convenience purposes, and should be treated as if it is immutable.*/
	public final Vector2 v11;
	
	
	/**
	 * Creates a new AABB.
	 * 
	 * @param x The x-coordinate of the AABB's bottom-left vertex.
	 * @param y The y-coordinate of the AABB's bottom-left vertex.
	 * @param width The AABB's width.
	 * @param height The AABB's height.
	 */
	public LightweightAABB(float x, float y, float width, float height) {
		v00 = new Vector2(x, y);
		v11 = new Vector2(x + width, y + height);
	}
	
	/**
	 * Creates a new AABB.
	 * 
	 * @param v00 The min vertex (i.e. bottom left) of the AABB.
	 * @param v11 The max vertex (i.e. top right) of the AABB.
	 */
	public LightweightAABB(Vector2 v00, Vector2 v11) {
		this.v00 = v00;
		this.v11 = v11;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Note that as a LightweightAABB is defined in terms of two vertices,
	 * the returned LightweightAABB will retain the properties of an AABB, but
	 * its min and max vertices will be transformed as per the matrix.
	 */
	@Override
	public LightweightAABB transform(Matrix2 matrix) {
		return newInstance(
				matrix.transform(v00),
				matrix.transform(v11)
		);
	}
	
	@Override
	public LightweightAABB translate(float x, float y) {
		return newInstance(
				new Vector2(v00.x + x, v00.y + y),
				new Vector2(v11.x + x, v11.y + y)
		);
	}
	
	@Override
	public LightweightAABB reflect() {
		return newInstance(
				new Vector2(-v11.x, v00.y),
				new Vector2(-v00.x, v11.y)
		);
	}
	
	@Override
	protected Vector2[] getVertices() {
		return new Vector2[] {
				v00,
				new Vector2(v11.x, v00.y),//v10
				v11,
				new Vector2(v00.x, v11.y) //v01
		};
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
	protected Vector2[] generateAxes() {
		return MathsUtil.UNIT_VECTORS;
	}
	
	@Override
	ShapeProjection getHorizontalProjection() {
		return new ShapeProjection(v00.x, v11.x);
	}
	
	@Override
	ShapeProjection getVerticalProjection() {
		return new ShapeProjection(v00.y, v11.y);
	}
	
	@Override
	protected boolean isAABB() {
		return true;
	}
	
	@Override
	public Vector2 getV00() {
		return v00;
	}
	
	@Override
	public Vector2 getV11() {
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
	public LightweightAABB precomputed() {
		return new Precomputed(this);
	}
	
	// Overriding for typecast purposes
	@Override
	public LightweightAABB notPrecomputed() {
		return this;
	}
	
	/**
	 * Creates a new LightweightAABB for duplication purposes. This is used to
	 * generate a new LightweightAABB whenever a duplicate is needed (i.e.,
	 * {@link #transform(Matrix2f)}, {@link #translate(float, float)},
	 * {@link #reflect()}, etc).
	 * 
	 * @return The new AABB.
	 */
	protected LightweightAABB newInstance(Vector2 v00, Vector2 v11) {
		return new LightweightAABB(v00, v11);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The precomputed variant of an AABB.
	 * 
	 * <p>Though an instance of this class may be instantiated directly, its
	 * declared type should simply be that of LightweightAABB.
	 */
	public static final class Precomputed extends LightweightAABB {
		
		/** All four of the AABB's vertices. */
		private Vector2[] vertices;
		/** The AABB's own projections */
		private ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed AABB.
		 * 
		 * @param x The x-coordinate of the AABB's bottom-left vertex.
		 * @param y The y-coordinate of the AABB's bottom-left vertex.
		 * @param width The AABB's width.
		 * @param height The AABB's height.
		 */
		public Precomputed(float x, float y, float width, float height) {
			super(x, y, width, height);
			precompute();
		}
		
		/**
		 * Creates a new precomputed AABB.
		 * 
		 * @param v00 The min vertex (i.e. bottom left) of the AABB.
		 * @param v11 The max vertex (i.e. top right) of the AABB.
		 */
		public Precomputed(Vector2 v00, Vector2 v11) {
			super(v00, v11);
			precompute();
		}
		
		/**
		 * Constructor to be used by LightweightAABB.
		 */
		private Precomputed(LightweightAABB a) {
			super(a.v00, a.v11);
			precompute();
		}
		
		/**
		 * Calculates the AABB's vertices and projections.
		 */
		private void precompute() {
			vertices = super.getVertices();
			projections = new ShapeProjection[] {
				super.getHorizontalProjection(),
				super.getVerticalProjection()
			};
		}
		
		@Override
		protected Vector2[] getVertices() {
			return vertices;
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
		public LightweightAABB notPrecomputed() {
			return new LightweightAABB(v00, v11);
		}
		
		@Override
		protected LightweightAABB newInstance(Vector2 v00, Vector2 v11) {
			return new Precomputed(v00, v11);
		}
		
	}
	
}
