package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

/**
 * This class represents a rectangular shape - that is, a quadrilateral with
 * opposite sides parallel, and right-angles between each side.
 */
public class Rectangle extends Quadrilateral {
	
	/**
	 * Creates a new Rectangle. It is implicitly trusted that the given
	 * vertices form a valid Rectangle. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param v00 The rectangle's bottom-left vertex.
	 * @param v01 The rectangle's top-left vertex.
	 * @param v10 The rectangle's bottom-right vertex.
	 * @param v11 The rectangle's top-right vertex.
	 */
	public Rectangle(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		super(v00, v01, v10, v11);
	}
	
	/**
	 * Creates a new Rectangle. It is implicitly trusted that the given
	 * vertices form a valid Rectangle. Invalid vertices may produce undefined
	 * behaviour.
	 * 
	 * @param vertices The rectangle's vertices. These should be indexed
	 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v10,
	 * and vertices[3] is v11.
	 * 
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public Rectangle(Vector2f[] vertices) {
		super(vertices);
	}

	/**
	 * Creates a new Rectangle.
	 * 
	 * @param x The x-coordinate of the rectangle's bottom-left vertex.
	 * @param y The y-coordinate of the rectangle's bottom-left vertex.
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 */
	public Rectangle(float x, float y, float width, float height) {
		super(
				new Vector2f(x, y),
				new Vector2f(x, y + height),
				new Vector2f(x + width, y),
				new Vector2f(x + width, y + height)
		);
	}
	
	@Override
	protected Vector2f[] getAxes() {
		// Rectangles require only two axes; as a rectangle consists of two
		// pairs of parallel sides, two axes would otherwise be duplicates -
		// hence, we can ignore the dupes.
		return new Vector2f[] {
				getAxis(v00, v01),
				getAxis(v00, v10)
		};
	}
	
	@Override
	public Rectangle transform(Matrix2f matrix) {
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
	
	@Override
	protected Rectangle newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		return new Rectangle(v00, v01, v10, v11);
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
		protected Vector2f[] axes;
		/** The shape's projections for each of its axes. */
		protected ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed Rectangle. It is implicitly trusted that
		 * the given vertices form a valid Rectangle. Invalid vertices may
		 * produce undefined behaviour.
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
		 * Creates a new precomputed Rectangle. It is implicitly trusted that
		 * the given vertices form a valid Rectangle. Invalid vertices may
		 * produce undefined behaviour.
		 * 
		 * @param vertices The rectangle's vertices. These should be indexed
		 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is
		 * v10, and vertices[3] is v11.
		 * 
		 * @throws IllegalArgumentException if {@code vertices.length != 4}.
		 */
		public Precomputed(Vector2f[] vertices) {
			super(vertices);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed Rectangle.
		 * 
		 * @param x The x-coordinate of the rectangle's bottom-left vertex.
		 * @param y The y-coordinate of the rectangle's bottom-left vertex.
		 * @param width The rectangle's width.
		 * @param height The rectangle's height.
		 */
		public Precomputed(float x, float y, float width, float height) {
			super(x, y, width, height);
			calculateProjections();
		}
		
		/**
		 * Constructor to be used by Rectangle.
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
		public Rectangle notPrecomputed() {
			return new Rectangle(v00, v01, v10, v11);
		}
		
		@Override
		protected Rectangle newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			return new Precomputed(v00, v01, v10, v11);
		}
		
	}
	
}
