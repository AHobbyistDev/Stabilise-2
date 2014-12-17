package com.stabilise.util.shape;

import com.badlogic.gdx.math.Vector2;
import com.stabilise.util.maths.Matrix2;

/**
 * A polygon is a shape with any number of vertices.
 * 
 * <p>The worst-case expensiveness of a polygon to compute scales linearly
 * with its number of vertices.
 */
public class Polygon extends AbstractPolygon {
	
	/** The polygon's vertices. Should be treated in most cases as final. */
	protected Vector2[] vertices;
	
	
	/**
	 * Creates a new Polygon.
	 * 
	 * @param vertices The polygon's vertices. These should be indexed such
	 * that adjacent vertices are adjacent in the array, with the first vertex
	 * also adjacent to the last vertex.
	 * 
	 * @throws NullPointerException if {@code vertices} or any of its elements
	 * are {@code null}.
	 * @throws IllegalArgumentException if {@code vertices.length < 3}.
	 */
	public Polygon(Vector2... vertices) {
		if(vertices.length < 3)
			throw new IllegalArgumentException("vertices.length < 3");
		
		for(Vector2 v : vertices) {
			if(v == null)
				throw new NullPointerException("A vertex is null");
		}
		
		this.vertices = vertices;
	}
	
	/**
	 * Constructor to be used when checking the vertex array would be pointless
	 * and wasteful.
	 */
	protected Polygon() {}
	
	/**
	 * Constructor to be used by the Precomputed subclass.
	 */
	private Polygon(Precomputed p) {
		vertices = p.vertices;
	}
	
	@Override
	public Polygon transform(Matrix2 matrix) {
		return newInstance(getTransformedVertices(matrix));
	}
	
	@Override
	public Polygon rotate(float rotation) {
		float cos = (float)Math.cos(rotation);
		float sin = (float)Math.sin(rotation);
		Vector2[] verts = new Vector2[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			verts[i] = rotateVertex(vertices[i], cos, sin);
		return newInstance(verts);
	}
	
	@Override
	public Polygon translate(float x, float y) {
		Vector2[] verts = new Vector2[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			verts[i] = new Vector2(vertices[i].x + x, vertices[i].y + y);
		return newInstance(verts);
	}
	
	@Override
	public Polygon reflect() {
		Vector2[] verts = new Vector2[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			verts[i] = new Vector2(-vertices[i].x, vertices[i].y);
		return newInstance(verts);
	}
	
	@Override
	protected Vector2[] getVertices() {
		return vertices;
	}
	
	@Override
	public Polygon precomputed() {
		return new Precomputed(this);
	}
	
	// Overriding for typecast purposes
	@Override
	public Polygon notPrecomputed() {
		return this;
	}
	
	/**
	 * Creates and returns a new Polygon for duplication purposes. This is used
	 * to create a new instance for transform(), rotate(), translate() and
	 * reflect().
	 * 
	 * <p>The returned polygon's vertices will not be set.
	 * 
	 * <p>This method should <i>not</i> be invoked directly.
	 * 
	 * @return The new polygon.
	 */
	protected Polygon newInstance() {
		return new Polygon();
	}
	
	/**
	 * Creates and returns a new Polygon for duplication purposes. This is used
	 * to create a new instance for transform(), rotate(), translate() and
	 * reflect().
	 * 
	 * <p>In general, it's better to override {@link #newInstance()} instead of
	 * this, as this hooks on to it.
	 * 
	 * @param vertices The vertices to set as the polygon's vertices.
	 * 
	 * @return The new polygon.
	 */
	protected Polygon newInstance(Vector2[] vertices) {
		// Also works, but less extensible for subclasses
		//return newPolygon(vertices);
		
		Polygon p = newInstance();
		p.vertices = vertices;
		return p;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Constructs and returns a new Polygon with the specified vertices. The
	 * returned polygon is equivalent to one constructed as if by
	 * {@link #Polygon(Vector2[]) new Polygon(vertices)}, however, this method
	 * provides faster performance as the supplied vertices are not checked for
	 * validity. As such, constructing a Polygon using this method is suitable
	 * only when it is known that the supplied vertices are valid.
	 * 
	 * @param vertices The polygon's vertices.
	 * 
	 * @return The new polygon.
	 */
	public static Polygon newPolygon(Vector2[] vertices) {
		Polygon p = new Polygon();
		p.vertices = vertices;
		return p;
		
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
		
		/** The projection axes. */
		protected Vector2[] axes;
		/** The projections corresponding to each axis. */
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
		public Precomputed(Vector2... vertices) {
			super(vertices);
			calculateProjections();
		}
		
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
		 * projections. Should be called when the polygon is constructed.
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
		public Polygon notPrecomputed() {
			return new Polygon(this);
		}
		
		@Override
		protected Polygon newInstance() {
			return new Precomputed();
		}
		
		@Override
		protected Polygon newInstance(Vector2[] vertices) {
			Precomputed p = (Precomputed)super.newInstance(vertices);
			p.calculateProjections();
			return p;
		}
		
	}
	
}
