package com.stabilise.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import com.stabilise.util.annotation.LWJGLReliant;

/**
 * Texture VBOs serve as VBOs for textures.
 * 
 * @see VertexBufferObject VertexBufferObject for important notices.
 */
@LWJGLReliant
class TextureVBO extends VertexBufferObject {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The amount of data associated with each texture coordinate. */
	private static final int TEXTURE_SIZE = 2;	//u,v
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The sprite's texture data for VBO rendering. */
	private FloatBuffer textureData;
	/** The VBO texture handle. */
	private int textureHandle;
	/** The mode in which the texture data is to be used. */
	private int textureMode;
	
	
	/**
	 * Creates a new TextureVBO, as if by {@link #TextureVBO(boolean, boolean)
	 * new TextureVBO(false, false)}.
	 */
	TextureVBO() {
		this(VertexBufferObject.STATIC_DRAW, VertexBufferObject.STATIC_DRAW);
	}
	
	/**
	 * Creates a new VBO for textures.
	 * 
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed often.
	 * @param dynamicTexture Whether or not the texture data is likely to be
	 * changed often.
	 */
	TextureVBO(boolean dynamicVertices, boolean dynamicTexture) {
		this(dynamicVertices ? DYNAMIC_DRAW : STATIC_DRAW,
				dynamicTexture ? STATIC_DRAW : STATIC_DRAW);
	}
	
	/**
	 * Creates a new VBO for textures.
	 * 
	 * @param vertexMode The mode in which the vertex data is to operate.
	 * @param textureMode The mode in which the texture data is to operate.
	 */
	TextureVBO(int vertexMode, int textureMode) {
		super(QUAD_VERTICES, vertexMode);
		
		textureHandle = glGenBuffers();
		textureData = BufferUtils.createFloatBuffer(QUAD_VERTICES * TEXTURE_SIZE);
		this.textureMode = textureMode;
		
		setTextureData();
	}
	
	/**
	 * Sets the VBO's default texture data.
	 */
	private void setTextureData() {
		setTextureData(new float[] {
				0.0f, 0.0f,			// Top-left
				1.0f, 0.0f,			// Top-right
				1.0f, 1.0f,			// Bottom-right
				0.0f, 1.0f			// Bottom-left
		});
	}
	
	/**
	 * Sets the VBO's texture data.
	 * 
	 * @param data The texture data.
	 * 
	 * @throws java.nio.BufferOverflowException Thrown if
	 * {@code data.length > 8} (note that a length less than this will not
	 * throw an exception).
	 */
	void setTextureData(float[] data) {
		textureData.flip();
		textureData.clear();
		textureData.put(data);
		textureData.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, textureHandle);
		glBufferData(GL_ARRAY_BUFFER, textureData, textureMode);
		//glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	void preDraw() {
		glEnable(GL_TEXTURE_2D);		// TODO: Possibly unnecessary enabling - may impact performance
		
		glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
		glVertexPointer(VERTEX_SIZE, GL_FLOAT, 0, 0L);
		
		glBindBuffer(GL_ARRAY_BUFFER, textureHandle);
		glTexCoordPointer(TEXTURE_SIZE, GL_FLOAT, 0, 0L);
	}
	
	@Override
	void draw() {
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		
		glDrawArrays(GL_QUADS, 0, vertices);
		
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);
		
		//glBindBuffer(GL_ARRAY_BUFFER, 0);			// Unbinding
	}
	
	@Override
	void destroy() {
		super.destroy();
		glDeleteBuffers(textureHandle);
	}
	
}
