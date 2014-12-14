package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

/**
 * A quadrilateral is a polygonal shape with four vertices.
 */
public class Quadrilateral extends AbstractPolygon {
	
	/** The quadrilateral's vertices. The points are named following the
	 * convention v[x][y], such that v00 is the bottom-left corner, and v11 is
	 * the top-right. Note that, however, with rotation, this may not be the
	 * literal case.
	 * 
	 * <p>Note that while these vertices are exposed as public fields, this is
	 * purely for convenience, and one should <b>not</b> modify them or their
	 * components. */
	public final Vector2f v00, v01, v10, v11;
	
	
	/**
	 * Creates a new Quadrilateral. It is implicitly trusted that the given
	 * vertices form a convex quadrilateral; such checking is not performed.
	 * 
	 * @param v00 The quadrilateral's bottom-left vertex.
	 * @param v01 The quadrilateral's top-left vertex.
	 * @param v10 The quadrilateral's bottom-right vertex.
	 * @param v11 The quadrilateral's top-right vertex.
	 */
	public Quadrilateral(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		this.v00 = v00;
		this.v01 = v01;
		this.v10 = v10;
		this.v11 = v11;
	}
	
	/**
	 * Creates a new Quadrilateral. It is implicitly trusted that the given
	 * vertices form a convex quadrilateral; such checking is not performed.
	 * 
	 * @param vertices The quadrilateral's vertices. These should be indexed
	 * such that vertices[0] is v00, vertices[1] is v01, vertices[2] is v10,
	 * and vertices[3] is v11.
	 * 
	 * @throws IllegalArgumentException if {@code vertices.length != 4}.
	 */
	public Quadrilateral(Vector2f[] vertices) {
		if(vertices.length != 4)
			throw new IllegalArgumentException("A quadrilateral does not have" + vertices.length + " vertices!");
		v00 = vertices[0];
		v01 = vertices[1];
		v10 = vertices[2];
		v11 = vertices[3];
	}
	
	@Override
	public Quadrilateral transform(Matrix2f matrix) {
		return newInstance(
				transformVertex(matrix, v00),
				transformVertex(matrix, v01),
				transformVertex(matrix, v10),
				transformVertex(matrix, v11)
		);
	}
	
	@Override
	public Quadrilateral translate(float offsetX, float offsetY) {
		return newInstance(
				new Vector2f(v00.x + offsetX, v00.y + offsetY),
				new Vector2f(v01.x + offsetX, v01.y + offsetY),
				new Vector2f(v10.x + offsetX, v10.y + offsetY),
				new Vector2f(v11.x + offsetX, v11.y + offsetY)
		);
	}
	
	@Override
	public Quadrilateral reflect() {
		return newInstance(
				new Vector2f(-v10.x, v10.y),
				new Vector2f(-v11.x, v11.y),
				new Vector2f(-v00.x, v00.y),
				new Vector2f(-v01.x, v01.y)
		);
	}
	
	@Override
	protected Vector2f[] getVertices() {
		return new Vector2f[] {
				v00, v10, v11, v01
		};
	}
	
	@Override
	public Quadrilateral precomputed() {
		return new Precomputed(this);
	}
	
	/**
	 * Creates a new Quadrilateral for duplication purposes. This is used to
	 * generate a new Quadrilateral whenever a duplicate is needed (i.e.,
	 * {@link #transform(Matrix2f)}, {@link #translate(float, float)},
	 * {@link #reflect()}, etc).
	 * 
	 * @return The new quadrilateral.
	 */
	protected Quadrilateral newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
		return new Quadrilateral(v00, v01, v10, v11);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The precomputed variant of a quadrilateral.
	 * 
	 * <p>Though an instance of this class may be instantiated directly, its
	 * declared type should simply be that of Quadrilateral.
	 */
	public static final class Precomputed extends Quadrilateral {
		
		/** The shape's projection axes. */
		protected Vector2f[] axes;
		/** The shape's projections for each of its axes. */
		protected ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed Quadrilateral. It is implicitly trusted
		 * that the given vertices form a convex quadrilateral; such checking
		 * is not performed.
		 * 
		 * @param v00 The quadrilateral's bottom-left vertex.
		 * @param v01 The quadrilateral's top-left vertex.
		 * @param v10 The quadrilateral's bottom-right vertex.
		 * @param v11 The quadrilateral's top-right vertex.
		 */
		public Precomputed(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			super(v00, v01, v10, v11);
			calculateProjections();
		}
		
		/**
		 * Creates a new precomputed Quadrilateral. It is implicitly trusted
		 * that the given vertices form a convex quadrilateral; such checking
		 * is not performed.
		 * 
		 * @param vertices The quadrilateral's vertices. These should be indexed
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
		 * Constructor to be used by Quadrilateral.
		 */
		private Precomputed(Quadrilateral q) {
			super(q.v00, q.v01, q.v10, q.v11);
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
		public Quadrilateral notPrecomputed() {
			return new Quadrilateral(v00, v01, v10, v11);
		}
		
		@Override
		protected Quadrilateral newInstance(Vector2f v00, Vector2f v01, Vector2f v10, Vector2f v11) {
			return new Precomputed(v00, v01, v10, v11);
		}
		
	}
	
}