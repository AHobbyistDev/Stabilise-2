package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.MathUtil;

/**
 * A DimensionedRectangle is a Rectangle which holds its width and height in
 * the {@link #width} and {@link #height} fields.
 */
public class DimensionedRectangle extends Rectangle {
	
	/** The rectangle's width. */
	public final float width;
	/** The rectangle's height. */
	public final float height;
	
	
	/**
	 * Creates a new DimensionedRectangle. It is implicitly trusted that the
	 * given vertices form a valid rectangle. Invalid vertices may produce
	 * undefined behaviour.
	 * 
	 * <p>Note that this constructor has some computation overhead in that
	 * the width and height of this rectangle must be calculated in a manner
	 * which accounts for rotation.
	 * 
	 * @param v00 The rectangle's bottom-left vertex.
	 * @param v01 The rectangle's top-left vertex.
	 * @param v10 The rectangle's bottom-right vertex.
	 * @param v11 The rectangle's top-right vertex.
	 */
	public DimensionedRectangle(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		this(v00, v01, v10, v11, MathUtil.distance(v10, v00), MathUtil.distance(v01, v00));
	}
	
	/**
	 * Creates a new DimensionedRectangle. It is implicitly trusted that the
	 * given vertices form a valid rectangle. Invalid vertices may produce
	 * undefined behaviour.
	 * 
	 * <p>This constructor is provided to allow one to explicitly set the
	 * dimensions, if they are known, to save computation time.
	 * 
	 * @param v00 The rectangle's bottom-left vertex.
	 * @param v01 The rectangle's top-left vertex.
	 * @param v10 The rectangle's bottom-right vertex.
	 * @param v11 The rectangle's top-right vertex.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle.
	 */
	public DimensionedRectangle(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11,
			float width, float height) {
		super(v00, v01, v10, v11);
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Creates a new DimensionedRectangle. It is implicitly trusted that the
	 * given vertices form a valid rectangle. Invalid vertices may produce
	 * undefined behaviour.
	 * 
	 * @param vertices The rectangle's vertices. These should be indexed
	 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v10,
	 * and vertices[3] is v11.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code vertices.length != 4}.
	 */
	public DimensionedRectangle(Vector2f[] vertices) {
		super(vertices);
		width = v11.x - v00.x;
		height = v11.y - v00.y;
	}
	
	/**
	 * Creates a new DimensionedRectangle.
	 * 
	 * @param x The x-coordinate of the rectangle's bottom-left vertex.
	 * @param y The y-coordinate of the rectangle's bottom-left vertex.
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 */
	public DimensionedRectangle(float x, float y, float width, float height) {
		super(x, y, width, height);
		this.width = width;
		this.height = height;
	}
	
	@Override
	public DimensionedRectangle transform(Matrix2f matrix) {
		return (DimensionedRectangle)super.transform(matrix);
	}
	
	@Override
	public DimensionedRectangle translate(float offsetX, float offsetY) {
		return (DimensionedRectangle)super.translate(offsetX, offsetY);
	}
	
	@Override
	public DimensionedRectangle reflect() {
		return (DimensionedRectangle)super.reflect();
	}
	
	@Override
	public DimensionedRectangle precomputed() {
		return new Precomputed(this);
	}
	
	@Override
	protected DimensionedRectangle newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		return new DimensionedRectangle(v00, v01, v10, v11);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The precomputed variant of a dimensioned rectangle.
	 * 
	 * <p>Though an instance of this class may be instantiated directly, its
	 * declared type should simply be that of DimensionedRectangle.
	 */
	public static final class Precomputed extends DimensionedRectangle {
		
		/** The shape's projection axes. */
		protected Vector2f[] axes;
		/** The shape's projections for each of its axes. */
		protected ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed DimensionedRectangle. It is implicitly
		 * trusted that the given vertices form a valid rectangle. Invalid
		 * vertices may produce undefined behaviour.
		 * 
		 * @param v00 The rectangle's bottom-left vertex.
		 * @param v01 The rectangle's top-left vertex.
		 * @param v10 The rectangle's bottom-right vertex.
		 * @param v11 The rectangle's top-right vertex.
		 */
		public Precomputed(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			super(v00, v01, v10, v11);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed DimensionedRectangle. It is implicitly
		 * trusted that the given vertices form a valid rectangle. Invalid
		 * vertices may produce undefined behaviour.
		 * 
		 * <p>This constructor is provided to allow one to explicitly set the
		 * dimensions, if they are known, to save computation time.
		 * 
		 * @param v00 The rectangle's bottom-left vertex.
		 * @param v01 The rectangle's top-left vertex.
		 * @param v10 The rectangle's bottom-right vertex.
		 * @param v11 The rectangle's top-right vertex.
		 * @param width The width of the rectangle.
		 * @param height The height of the rectangle.
		 */
		public Precomputed(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11,
				float width, float height) {
			super(v00, v01, v10, v11, width, height);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed DimensionedRectangle. It is implicitly
		 * trusted that the given vertices form a valid rectangle. Invalid
		 * vertices may produce undefined behaviour.
		 * 
		 * @param vertices The rectangle's vertices. These should be indexed
		 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v10,
		 * and vertices[3] is v11.
		 * 
		 * @throws IllegalArgumentException Thrown if {@code vertices.length !=
		 * 4}.
		 */
		public Precomputed(Vector2f[] vertices) {
			super(vertices);
			calculateProjections();
		}
		
		/**
		 * Constructor to be used by DimensionedRectangle.
		 */
		private Precomputed(Rectangle r) {
			super(r.v00, r.v01, r.v10, r.v11);
			calculateProjections();
		}
		
		/**
		 * Calculates the shape's projection axes, and their respective
		 * projections.
		 */
		private void calculateProjections() {
			axes = generateAxes();
			projections = new ShapeProjection[axes.length];
			
			for(int i = 0; i < axes.length; i++)
				projections[i] = getProjection(axes[i]);
		}
		
		// Precomputification
		@Override protected Vector2f[] getAxes() { return axes; }
		@Override protected ShapeProjection getProjection(int i) { return projections[i]; }
		@Override public boolean containsPoint(Vector2f p) { return containsPointPrecomputed(p); }
		@Override protected boolean intersectsOnOwnAxes(Shape s) { return intersectsOnOwnAxesPrecomputed(s); }
		
		@Override
		public DimensionedRectangle notPrecomputed() {
			return new DimensionedRectangle(v00, v01, v10, v11, width, height);
		}
		
		@Override
		protected DimensionedRectangle newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			return new Precomputed(v00, v01, v10, v11);
		}
		
	}
	
}
