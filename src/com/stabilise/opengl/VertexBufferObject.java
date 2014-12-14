package com.stabilise.opengl;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;

import org.lwjgl.BufferUtils;

import com.stabilise.util.annotation.LWJGLReliant;

/**
 * VertexBufferObjects form fundamental render objects within OpenGL, and form
 * the basis of rendering (in the manner being used within this project, at
 * least).
 * 
 * <p>VertexBufferObject and its subclasses expose a number of methods with
 * sensitive parameters, such as {@link #setVertexData(float[])}, but in the
 * interest of minimising performance overhead, no checking is performed to
 * ensure their validity. As such, in cases where VBOs are directly interacted
 * with, it is prudent to ensure correct data is passed to them.
 */
@LWJGLReliant
abstract class VertexBufferObject {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of vertices for a quadrilateral object. */
	protected static final int QUAD_VERTICES = 4;
	
	/** The amount of data associated with each vertex coordinate. */
	protected static final int VERTEX_SIZE = 3;	//x,y,z
	
	/** Indicates the static usage of a VBO. */
	static final int STATIC_DRAW = GL_STATIC_DRAW;
	/** Indicates the dynamic usage of a VBO. */
	static final int DYNAMIC_DRAW = GL_DYNAMIC_DRAW;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The number of vertices. */
	protected int vertices;
	
	/** The sprite's (term used broadly) vertex data for VBO rendering. */
	protected FloatBuffer vertexData;
	/** The VBO vertex handle. */
	protected int vertexHandle;
	/** The mode in which the vertex data is to operate. */
	protected int vertexMode;
	
	
	/**
	 * Creates a new VertexBufferObject, as if by
	 * {@link #VertexBufferObject(int, boolean)
	 * new VertexBufferObject(vertices, false)}.
	 * 
	 * @param vertices The number of vertices in the VBO.
	 */
	VertexBufferObject(int vertices) {
		this(vertices, STATIC_DRAW);
	}
	
	/**
	 * Creates a new VertexBufferObject.
	 * 
	 * @param vertices The number of vertices in the VBO.
	 * @param dynamicVertices Whether or not the vertex data is likely to
	 * change often.
	 */
	VertexBufferObject(int vertices, boolean dynamicVertices) {
		this(vertices, dynamicVertices ? DYNAMIC_DRAW : STATIC_DRAW);
	}
	
	/**
	 * Creates a new VertexBufferObject.
	 * 
	 * @param vertices The number of vertices in the VBO.
	 * @param vertexMode The mode in which the vertex data is to operate.
	 * (Either {@link #STATIC_DRAW} or {@link #DYNAMIC_DRAW}.)
	 */
	VertexBufferObject(int vertices, int vertexMode) {
		this.vertices = vertices;
		
		vertexHandle = glGenBuffers();
		vertexData = BufferUtils.createFloatBuffer(vertices * VERTEX_SIZE);
		this.vertexMode = vertexMode;
	}
	
	/**
	 * Sets the VBO's vertex data. This should only be invoked if this VBO has
	 * 4 vertices, though it may not necessarily throw an exception otherwise.
	 * 
	 * @param width The VBO's width.
	 * @param height The VBO's height.
	 */
	void setVertexData(float width, float height) {
		setVertexData(width, height, 0);
	}
	
	/**
	 * Sets the VBO's vertex data. This should only be invoked if this VBO has
	 * 4 vertices, though it may not necessarily throw an exception otherwise.
	 * 
	 * @param width The VBO's width.
	 * @param height The VBO's height.
	 * @param depth The VBO's depth.
	 */
	void setVertexData(float width, float height, float depth) {
		//if(vertices != QUAD_VERTICES)
		//	throw new RuntimeException("Cannot set rectangular vertex data for a non-rectangular VBO!");
		
		setVertexData(new float[] {
				0.0f,	height,	depth,		// Top-left
				width,	height,	depth,		// Top-right
				width,	0.0f,	depth,		// Bottom-right
				0.0f,	0.0f,	depth		// Bottom-left
		});
	}
	
	/**
	 * Sets the VBO's vertex data.
	 * 
	 * @param data The vertex data.
	 * 
	 * @throws java.nio.BufferOverflowException Thrown if
	 * {@code data.length > 4*v}, where {@code v} is number of vertices of this
	 * VBO (note that a length less than this will not throw an exception).
	 */
	void setVertexData(float[] data) {
		//if(data.length != vertices * VERTEX_SIZE)
		//	throw new IllegalArgumentException("Vertex data given is of invalid size! (" + data.length + "/" + (vertices * VERTEX_SIZE) + ")");
		
		vertexData.flip();
		vertexData.clear();
		vertexData.put(data);
		vertexData.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
		glBufferData(GL_ARRAY_BUFFER, vertexData, vertexMode);
		//glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Prepares to draw the VBO.
	 */
	abstract void preDraw();
	
	/**
	 * Draws the VBO to the current OpenGL context.
	 */
	abstract void draw();
	
	/**
	 * Finishes drawing the VBO.
	 */
	//abstract void postDraw();
	
	/**
	 * Destroys the VBO.
	 */
	void destroy() {
		glDeleteBuffers(vertexHandle);
	}
	
}
