package com.stabilise.util.shape;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Vector2f;

/**
 * This class represents polygon with an indeterminate number of vertices.
 */
public class Polygon extends AbstractPolygon {
	
	/** The polygon's vertices. Not final due to the blank constructor. */
	protected Vector2f[] vertices;
	
	
	/**
	 * Creates a new Polygon.
	 * 
	 * @param vertices The polygon's vertices.
	 * 
	 * @throws NullPointerException if {@code vertices} or any of its elements
	 * are {@code null}.
	 * @throws IllegalArgumentException if {@code vertices.length < 3}.
	 */
	public Polygon(Vector2f[] vertices) {
		if(vertices == null)
			throw new NullPointerException("A polygon's vertices may not be null!");
		if(vertices.length < 3)
			throw new IllegalArgumentException("A polygon must have at least 3 vertices!");
		
		for(Vector2f v : vertices) {
			if(v == null)
				throw new NullPointerException("A polygon's vertices may not be null!");
		}
		
		this.vertices = vertices;
	}
	
	/**
	 * Constructor to be used when checking the vertex array would be pointless
	 * and wasteful.
	 */
	private Polygon() {}
	
	/**
	 * Constructor to be used by the Precomputed subclass.
	 */
	private Polygon(Precomputed p) {
		vertices = p.vertices;
	}
	
	@Override
	public Polygon transform(Matrix2f matrix) {
		Polygon p = newPolygon();
		p.vertices = getTransformedVertices(matrix);
		return p;
	}
	
	@Override
	public Polygon translate(float x, float y) {
		Vector2f[] verts = new Vector2f[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			verts[i] = new Vector2f(vertices[i].x + x, vertices[i].y + y);
		Polygon p = newPolygon();
		p.vertices = verts;
		return p;
	}
	
	@Override
	protected Vector2f[] getVertices() {
		return vertices;
	}
	
	@Override
	public Polygon reflect() {
		// N.B. The cloned polygon's vertices will propagate in an anticlockwise
		// fashion if the vertices of this polygon propagate clockwise, and visa-
		// versa. This has no impact on operation, though.
		Vector2f[] verts = new Vector2f[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			verts[i] = new Vector2f(-vertices[i].x, vertices[i].y);
		Polygon p = newPolygon();
		p.vertices = verts;
		return p;
	}
	
	@Override
	public Precomputed precomputed() {
		return new Precomputed(this);
	}
	
	/**
	 * Creates a new Polygon for duplication purposes.
	 * 
	 * @return The new blank polygon.
	 */
	protected Polygon newPolygon() {
		return new Polygon();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * The precomputed variant of a polygon.
	 * 
	 * <p>Though an instance of this class may be instantiated directly, its
	 * declared type should simply be that of Polygon.
	 */
	public static final class Precomputed extends Polygon {
		
		/** The shape's projection axes. */
		protected Vector2f[] axes;
		/** The shape's projections for each of its axes. */
		protected ShapeProjection[] projections;
		
		
		/**
		 * Creates a new precomputed Polygon.
		 * 
		 * @param vertices The polygon's vertices.
		 * 
		 * @throws NullPointerException if {@code vertices} or any of its elements
		 * are {@code null}.
		 * @throws IllegalArgumentException if {@code vertices.length < 3}.
		 */
		public Precomputed(Vector2f[] vertices) {
			super(vertices);
			calculateProjections();
		}
		
		/**
		 * Constructor to be used when checking the vertex array would be
		 * pointless and wasteful.
		 */
		private Precomputed() {}
		
		/**
		 * Constructor to be used by Polygon.
		 */
		private Precomputed(Polygon p) {
			super();
			vertices = p.vertices;
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
		public Polygon notPrecomputed() {
			return new Polygon(this);
		}
		
		@Override
		protected Polygon newPolygon() {
			return new Precomputed();
		}
		
	}
	
}
