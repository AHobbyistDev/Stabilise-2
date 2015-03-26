package com.stabilise.util.shape;

import com.stabilise.util.maths.Matrix2;
import com.stabilise.util.maths.Vec2;

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
	public Rectangle(Vec2... vertices) {
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
	 */
	public Rectangle(Vec2 v00, Vec2 v01, Vec2 v10, Vec2 v11) {
		vertices = new Vec2[] { v00, v01, v11, v10 };
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
		vertices = new Vec2[] {
				new Vec2(x, y),
				new Vec2(x, y + height),
				new Vec2(x + width, y + height),
				new Vec2(x + width, y)
		};
	}
	
	@Override
	protected Vec2[] genAxes() {
		axes = new Vec2[] {
				getAxis(vertices[V00], vertices[V01]),
				getAxis(vertices[V00], vertices[V10])
		};
		genProjections();
		return axes;
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
	protected Rectangle newInstance() {
		return new Rectangle();
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * @throws NullPointerException if {@code verts} or any of its elements are
	 * {@code null}.
	 * @throws IllegalArgumentException if {@code verts.length != 4}.
	 */
	protected static void checkVerts(Vec2[] verts) {
		if(verts.length != 4)
			throw new IllegalArgumentException("A rectangle must have 4 (not" +
					verts.length + "!) vertices");
		for(Vec2 v : verts) {
			if(v == null)
				throw new NullPointerException("A polygon's vertices may not be null!");
		}
	}
	
}
