package com.stabilise.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.stabilise.util.annotation.LWJGLReliant;

/**
 * ShapeVertexBufferObjects serve as VBOs for coloured shapes.
 * 
 * @see VertexBufferObject VertexBufferObject for important notices.
 */
@LWJGLReliant
class ShapeVBO extends VertexBufferObject {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The amount of data associated with each colour. */
	private static final int COLOUR_SIZE = 4;	//r,g,b,a
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The shape's colour data for VBO rendering. */
	protected FloatBuffer colourData;
	/** The VBO texture handle. */
	private int colourHandle;
	/** The mode in which the vertex data is to operate. */
	private int colourMode;
	
	
	/**
	 * Creates a new rectangular shape VBO, which uses static data.
	 */
	ShapeVBO() {
		this(QUAD_VERTICES);
	}
	
	/**
	 * Creates a new shape VBO, which uses static data.
	 * 
	 * @param vertices The number of vertices for the shape.
	 */
	ShapeVBO(int vertices) {
		this(vertices, VertexBufferObject.STATIC_DRAW, VertexBufferObject.STATIC_DRAW);
	}
	
	/**
	 * Creates a new shape VBO.
	 * 
	 * @param vertices The number of vertices for the shape.
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed often.
	 * @param dynamicColours Whether or not the colour data is likely to be
	 * changed often.
	 */
	ShapeVBO(int vertices, boolean dynamicVertices, boolean dynamicColours) {
		this(vertices, dynamicVertices ? VertexBufferObject.DYNAMIC_DRAW : VertexBufferObject.STATIC_DRAW,
				dynamicColours ? VertexBufferObject.DYNAMIC_DRAW : VertexBufferObject.STATIC_DRAW);
	}
	
	/**
	 * Creates a new shape VBO.
	 * 
	 * @param vertices The number of vertices for the shape.
	 * @param vertexMode The mode in which the vertex data is to operate.
	 * @param colourMode The mode in which the colour data is to operate.
	 */
	ShapeVBO(int vertices, int vertexMode, int colourMode) {
		super(vertices, vertexMode);
		
		colourHandle = glGenBuffers();
		colourData = BufferUtils.createFloatBuffer(vertices * COLOUR_SIZE);
		this.colourMode = colourMode;
	}
	
	/**
	 * Sets the VBO's colour data.
	 * 
	 * @param data The colour data.
	 * 
	 * @throws java.nio.BufferOverflowException Thrown if
	 * {@code data.length > 4*v}, where {@code v} is number of vertices of this
	 * VBO (note that a length less than this will not throw an exception).
	 */
	void setColours(float[] data) {
		//if(data.length != vertices * COLOUR_SIZE)
		//	throw new IllegalArgumentException("Colour data given is of invalid size! (" + data.length + "/" + (vertices * COLOUR_SIZE) + ")");
		
		colourData.flip();
		colourData.clear();
		colourData.put(data);
		colourData.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, colourHandle);
		glBufferData(GL_ARRAY_BUFFER, colourData, colourMode);
		//glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	void preDraw() {
		glDisable(GL_TEXTURE_2D);			// TODO: Possibly unnecessary disabling - may impact performance
		
		glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
		glVertexPointer(VERTEX_SIZE, GL_FLOAT, 0, 0L);
		
		glBindBuffer(GL_ARRAY_BUFFER, colourHandle);
		glColorPointer(COLOUR_SIZE, GL_FLOAT, 0, 0L);
	}
	
	@Override
	void draw() {
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		
		if(vertices == QUAD_VERTICES)
			glDrawArrays(GL_QUADS, 0, vertices);
		else if(vertices == 3)
			glDrawArrays(GL_TRIANGLES, 0, vertices);
		else
			glDrawArrays(GL_POLYGON, 0, vertices);			// TODO: I don't trust this
		
		glDisableClientState(GL_COLOR_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);
		
		//glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	void destroy() {
		super.destroy();
		glDeleteBuffers(colourHandle);
	}
	
}
