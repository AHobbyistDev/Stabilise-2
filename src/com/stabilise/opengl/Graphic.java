package com.stabilise.opengl;

import com.stabilise.opengl.shader.ColourEffectShaderProgram;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * Graphics are displayable objects.
 */
@LWJGLReliant
public abstract class Graphic {
	
	/** The graphic's x-coordinate */
	public float x = 0;
	/** The graphic's y-coordinate */
	public float y = 0;
	
	/** The graphic's width. */
	protected int width = 0;
	/** The sprite's height. */
	protected int height = 0;
	
	/** The graphic's rotation, in degrees. */
	public float rotation = 0;
	/** The x-coordinate of the graphic's transformational pivot. */
	protected int pivotX = 0;
	/** The y-coordinate of the graphic's transformational pivot. */
	protected int pivotY = 0;
	
	/** The graphic's shader. */
	protected ColourEffectShaderProgram shader;
	
	/** True if the destroy() method has been invoked. */
	protected boolean destroyed = false;
	
	
	/** 
	 * Draws the graphic.
	 */
	public final void draw() {
		// Don't draw the graphic if it has been destroyed.
		// This is but a safeguard; if a graphic has been destroyed it should be
		// the responsibility of the destroyer to delete the sprite.
		if(destroyed) {
			Log.critical("Attempting to draw a graphic which has been destroyed!");
			return;
		}
		
		preDraw();
		drawGraphic();
		postDraw();
	}
	
	/**
	 * Prepares to draw the graphic.
	 */
	protected void preDraw() {
		/*
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glPushMatrix();
		
		glTranslatef(x, y, 0);
		
		transform();
		*/
	}
	
	/**
	 * Transforms the graphic during the pre-drawing process.
	 */
	protected void transform() {
		/*
		if(rotation != 0)
			glRotatef(rotation, 0, 0, 1);
		// Centre the sprite on its pivot for rotation 
		glTranslatef(-pivotX, -pivotY, 0);
		*/
	}
	
	/**
	 * Draws the graphic.
	 */
	protected abstract void drawGraphic();
	
	/**
	 * Finalises drawing the graphic.
	 */
	protected void postDraw() {
		/*
		glPopMatrix();
		*/
	}
	
	/**
	 * Binds the shader.
	 * 
	 * <p>This is never called directly within {@code Graphic} and as such must
	 * be called appropriately by its subclasses.
	 * 
	 * @throws NullPointerException Thrown if {@link #shader} is {@code null}.
	 */
	protected void bindShader() {
		shader.bind();
	}
	
	/**
	 * Safely binds the shader - that is, first checks for whether or not it is
	 * null.
	 * 
	 * <p>This is never called directly within {@code Graphic} and as such must
	 * be called appropriately by its subclasses.
	 */
	protected void bindShaderSafe() {
		if(shader != null)
			bindShader();
	}
	
	/**
	 * Unbinds the shader.
	 * 
	 * <p>This is never called directly within {@code Graphic} and as such must
	 * be called appropriately by its subclasses.
	 * 
	 * @throws NullPointerException Thrown if {@link #shader} is {@code null}.
	 */
	protected void unbindShader() {
		shader.unbind();
	}
	
	/**
	 * Safely unbinds the shader - that is, first checks for whether or not it
	 * is null.
	 * 
	 * <p>This is never called directly within {@code Graphic} and as such must
	 * be called appropriately by its subclasses.
	 */
	protected void unbindShaderSafe() {
		if(shader != null)
			unbindShader();
	}
	
	/**
	 * Releases all of the graphic's resources.
	 */
	public void destroy() {
		destroyed = true;
		if(shader != null)
			shader.unload();
	}
	
	//--------------------==========--------------------
	//---------=====Getter/Setter Wrappers=====---------
	//--------------------==========--------------------
	
	/**
	 * Gets the width of the graphic.
	 * 
	 * @return The graphic's width.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height of the graphic.
	 * 
	 * @return The graphic's height.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Sets the width of the graphic.
	 * 
	 * @param width The new width.
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * Sets the height of the graphic.
	 * 
	 * @param height The new height.
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Sets the graphic's transformational pivot. The scale of the graphic is
	 * ignored for the purpose of the pivot.
	 * 
	 * @param x The x-coordinate of the pivot, in pixels, left-right.
	 * @param y The y-coordinate of the pivot, in pixels, bottom-up.
	 */
	public void setPivot(int x, int y) {
		pivotX = x;
		pivotY = y;
	}
	
}
