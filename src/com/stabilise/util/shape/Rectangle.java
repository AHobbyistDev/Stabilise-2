package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.maths.Matrix2;

/**
 * A rectangle is a quadrilateral with opposite sides parallel, and
 * right-angles between each side.
 */
public class Rectangle extends Polygon {
	
	/** Array indices to use when referencing vertices in the {@link
	 * Polygon#vertices vertices} array. */
	public static final int V00 = 0, V01 = 2, V10 = 3, V11 = 2;
	
	
	/**
	 * Constructor to be used when checking the vertex array would be pointless
	 * and wasteful.
	 */
	protected Rectangle() {
		super();
	}
	
	/**
	 * Creates a new Rectangle. It is implicitly trusted that the given
	 * vertices form a valid Rectangle. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param vertices The rectangle's vertices. These should be indexed
	 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v11,
	 * and vertices[3] is v01.
	 * 
	 * @throws NullPointerException if {@code vertices} or any of its elements
	 * are {@code null}.
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public Rectangle(Vector2[] vertices) {
		super();
		checkVerts(vertices);
		this.vertices = vertices;
	}
	
	/**
	 * Creates a new Rectangle. It is implicitly trusted that the given
	 * vertices form a valid Rectangle. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param v00 The bottom-left vertex.
	 * @param v01 The top-left vertex.
	 * @param v10 The bottom-right vertex.
	 * @param v11 The top-right vertex.
	 * 
	 * @throws NullPointerException if {@code vertices} or any of its elements
	 * are {@code null}.
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public Rectangle(Vector2 v00, Vector2 v01, Vector2 v10, Vector2 v11) {
		super();
		this.vertices = new Vector2[] { v00, v01, v11, v10 };
	}

	/**
	 * Creates a new Rectangle.
	 * 
	 * @param x The x-coordinate of the rectangle's bottom-left vertex.
	 * @param y The y-coordinate of the rectangle's bottom-left vertex.
	 * @param width The rectangle's width. This should be positive.
	 * @param height The rectangle's height. This should be positive.
	 */
	public Rectangle(float x, float y, float width, float height) {
		super();
		this.vertices = new Vector2[] {
				new Vector2(x, y),
				new Vector2(x, y + height),
				new Vector2(x + width, y + height),
				new Vector2(x + width, y)
		};
	}
	
	@Override
	protected Vector2[] getAxes() {
		// Rectangles require only two axes; as a rectangle consists of two
		// pairs of parallel sides, two axes would otherwise be duplicates -
		// hence, we can ignore the dupes.
		return new Vector2[] {
				getAxis(vertices[V00], vertices[V01]),
				getAxis(vertices[V00], vertices[V10])
		};
	}
	
	@Override
	public Rectangle transform(Matrix2 matrix) {
		return (Rectangle)super.transform(matrix);
	}
	
	@Override
	public Rectangle translate(float offsetX, float offsetY) {
		return (Rectangle)super.translate(offsetX, offsetY);
	}
	
	@Override
	public Rectangle reflect() {
		return (Rectangle)super.reflect();
	}
	
	@Override
	public Rectangle precomputed() {
		return new Precomputed(this);
	}
	
	// Overriding for typecast purposes
	@Override
	public Rectangle notPrecomputed() {
		return this;
	}
	
	@Override
	protected Rectangle newInstance() {
		return new Rectangle();
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * @throws NullPointerException if {@code verts} or any of its elements are
	 * {@code null}.
	 * @throws IllegalArgumentException if {@code verts.size != 4}.
	 */
	protected static void checkVerts(Vector2[] verts) {
		if(verts.length != 4)
			throw new IllegalArgumentException("vertices.size != 4");
		for(Vector2 v : verts) {
			if(v == null)
				throw new NullPointerException("A polygon's vertices may not be null!");
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The precomputed variant of a rectangle.
	 * 
	 * <p>Though an instance of this class may be instantiated directly, its
	 * declared type should simply be that of Rectangle.
	 */
	public static final class Precomputed extends Rectangle {
		
		/** The shape's projection axes. */
		protected Vector2[] axes;
		/** The shape's projections for each of its axes. */
		protected ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed Rectangle. It is implicitly trusted that
		 * the given vertices form a valid Rectangle. Invalid vertices may
		 * produce undefined behaviour.
		 * 
		 * @param vertices The rectangle's vertices. These should be indexed
		 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is
		 * v11, and vertices[3] is v01.
		 * 
		 * @throws NullPointerException if {@code vertices} or any of its
		 * elements are {@code null}.
		 * @throws IllegalArgumentException if {@code vertices.length != 4}.
		 */
		public Precomputed(Vector2[] vertices) {
			super(vertices);
			calculateProjections();
		}

		/**
		 * Creates a new precomputed Rectangle.
		 * 
		 * @param x The x-coordinate of the rectangle's bottom-left vertex.
		 * @param y The y-coordinate of the rectangle's bottom-left vertex.
		 * @param width The rectangle's width. Should be positive.
		 * @param height The rectangle's height. Should be positive.
		 */
		public Precomputed(float x, float y, float width, float height) {
			super(x, y, width, height);
			calculateProjections();
		}
		
		/**
		 * Constructor to be used by Rectangle.
		 */
		private Precomputed(Rectangle r) {
			super();
			vertices = r.vertices;
			calculateProjections();
		}
		
		private Precomputed() {}
		
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
		@Override protected Vector2[] getAxes() { return axes; }
		@Override protected ShapeProjection getProjection(int i) { return projections[i]; }
		@Override public boolean containsPoint(Vector2 p) { return containsPointPrecomputed(p); }
		@Override protected boolean intersectsOnOwnAxes(Shape s) { return intersectsOnOwnAxesPrecomputed(s); }
		
		@Override
		public Rectangle notPrecomputed() {
			Rectangle r = new Rectangle();
			r.vertices = vertices;
			return r;
		}
		
		@Override
		protected Rectangle newInstance() {
			return new Precomputed();
		}
		
		@Override
		protected Rectangle newInstance(Vector2[] vertices) {
			Precomputed p = (Precomputed)super.newInstance(vertices);
			p.calculateProjections();
			return p;
		}
		
	}
	
}
