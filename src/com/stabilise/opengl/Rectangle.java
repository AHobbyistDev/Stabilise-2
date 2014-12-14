package com.stabilise.opengl;

import com.stabilise.util.Colour;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * A simple rectangular-shaped coloured graphic.
 */
@LWJGLReliant
public class Rectangle extends Graphic {
	
	/** The rectangle's VertexBufferObject. */
	private ShapeVBO vbo;
	
	
	/**
	 * Creates a new Rectangle of dimensions (0,0), which is expected to not
	 * have its vertices or colours change often.
	 */
	public Rectangle() {
		this(0, 0);
	}
	
	/**
	 * Creates a new Rectangle, which is expected to not have its vertices or
	 * colours change often.
	 * 
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 */
	public Rectangle(int width, int height) {
		this.width = width;
		this.height = height;
		vbo = new ShapeVBO(VertexBufferObject.QUAD_VERTICES);
		resize();
	}
	
	/**
	 * Creates a new Rectangle of dimensions (0,0).
	 * 
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed often.
	 * @param dynamicColours Whether or not the colour data is to be changed
	 * often.
	 */
	public Rectangle(boolean dynamicVertices, boolean dynamicColours) {
		this(0, 0, dynamicVertices, dynamicColours);
	}
	
	/**
	 * Creates a new Rectangle.
	 * 
	 * @param width The rectangle's width.
	 * @param height The rectangle's height.
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed often.
	 * @param dynamicColours Whether or not the colour data is to be changed
	 * often.
	 */
	public Rectangle(int width, int height, boolean dynamicVertices, boolean dynamicColours) {
		this.width = width;
		this.height = height;
		vbo = new ShapeVBO(4, dynamicVertices, dynamicColours);
		resize();
	}
	
	@Override
	protected void drawGraphic() {
		vbo.preDraw();
		vbo.draw();
	}
	
	/**
	 * Resizes the rectangle.
	 * 
	 * @param width The new width of the rectangle.
	 * @param height The new height of the rectangle.
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		resize();
	}
	
	/**
	 * Handles resizing of the rectangle.
	 */
	private void resize() {
		vbo.setVertexData(width, height);
	}
	
	/**
	 * Fills the rectangle with a single opaque colour.
	 * 
	 * @param colour The colour with which to fill the rectangle.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle fill(Colour colour) {
		return fill(colour, 1.0f);
	}
	
	/**
	 * Fills the rectangle with a single colour.
	 * 
	 * @param colour The colour with which to fill the rectangle.
	 * @param opacity The opacity of the colour, from 0.0 to 1.0.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle fill(Colour colour, float opacity) {
		return colourVertices(colour, opacity, colour, opacity, colour, opacity, colour, opacity);
	}
	
	/**
	 * Fills the rectangle with an opaque gradient.
	 * 
	 * @param leftColour The colour on the left of the rectangle.
	 * @param rightColour The colour on the right of the rectangle.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle gradientLeftToRight(Colour leftColour, Colour rightColour) {
		return gradientLeftToRight(leftColour, 1.0f, rightColour, 1.0f);
	}
	
	/**
	 * Fills the rectangle with a gradient.
	 * 
	 * @param leftColour The colour on the left of the rectangle.
	 * @param leftColourOpacity The opacity of the left colour, from 0.0 to
	 * 1.0.
	 * @param rightColour The colour on the right of the rectangle.
	 * @param rightColourOpacity The opacity of the right colour, from 0.0 to
	 * 1.0.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle gradientLeftToRight(Colour leftColour, float leftColourOpacity, Colour rightColour, float rightColourOpacity) {
		return colourVertices(
				leftColour, leftColourOpacity,
				rightColour, rightColourOpacity,
				leftColour, leftColourOpacity,
				rightColour, rightColourOpacity
		);
	}
	
	/**
	 * Fills the rectangle with an opaque gradient.
	 * 
	 * @param topColour The colour on the top of the rectangle.
	 * @param bottomColour The colour on the bottom of the rectangle.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle gradientTopToBottom(Colour topColour, Colour bottomColour) {
		return gradientTopToBottom(topColour, 1.0f, bottomColour, 1.0f);
	}
	
	/**
	 * Fills the rectangle with a gradient.
	 * 
	 * @param topColour The colour on the top of the rectangle.
	 * @param topColourOpacity The opacity of the top colour, from 0.0 to 1.0.
	 * @param bottomColour The colour on the bottom of the rectangle.
	 * @param bottomColourOpacity The opacity of the bottom colour, from 0.0 to
	 * 1.0.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle gradientTopToBottom(Colour topColour, float topColourOpacity, Colour bottomColour, float bottomColourOpacity) {
		return colourVertices(
				topColour, topColourOpacity,
				topColour, topColourOpacity,
				bottomColour, bottomColourOpacity,
				bottomColour, bottomColourOpacity
		);
	}
	
	/**
	 * Fills the rectangle with different opaque colours at each corner.
	 * 
	 * @param topLeftColour The colour at the top-left of the rectangle.
	 * @param topRightColour The colour at the top-right of the rectangle.
	 * @param bottomRightColour The colour at the bottom-right of the
	 * rectangle.
	 * @param bottomLeftColour The colour at the bottom-left of the rectangle.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle colourVertices(Colour topLeftColour, Colour topRightColour, Colour bottomRightColour, Colour bottomLeftColour) {
		return colourVertices(topLeftColour, 1.0f, topRightColour, 1.0f, bottomRightColour, 1.0f, bottomLeftColour, 1.0f);
	}
	
	/**
	 * Fills the rectangle with different colours at each corner.
	 * 
	 * @param topLeftColour The colour at the top-left of the rectangle.
	 * @param topLeftOpacity The opacity of the top-left colour, from 0.0 to
	 * 1.0.
	 * @param topRightColour The colour at the top-right of the rectangle.
	 * @param topRightOpacity The opacity of the top-right colour, from 0.0 to
	 * 1.0.
	 * @param bottomRightColour The colour at the bottom-right of the
	 * rectangle.
	 * @param bottomRightOpacity The opacity of the bottom-right colour, from
	 * 0.0 to 1.0.
	 * @param bottomLeftColour The colour at the bottom-left of the rectangle.
	 * @param bottomLeftOpacity The opacity of the bottom-left colour, from 0.0
	 * to 1.0.
	 * 
	 * @return The Rectangle, for chaining operations.
	 */
	public Rectangle colourVertices(
			Colour topLeftColour, float topLeftOpacity,
			Colour topRightColour, float topRightOpacity,
			Colour bottomRightColour, float bottomRightOpacity,
			Colour bottomLeftColour, float bottomLeftOpacity) {
		
		vbo.setColours(new float[] { 
				topLeftColour.getRed(), topLeftColour.getGreen(), topLeftColour.getBlue(), topLeftOpacity,
				topRightColour.getRed(), topRightColour.getGreen(), topRightColour.getBlue(), topRightOpacity,
				bottomRightColour.getRed(), bottomRightColour.getGreen(), bottomRightColour.getBlue(), bottomRightOpacity,
				bottomLeftColour.getRed(), bottomLeftColour.getGreen(), bottomLeftColour.getBlue(), bottomLeftOpacity
		});
		
		return this;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		vbo.destroy();
	}
	
	//--------------------==========--------------------
	//---------=====Getter/Setter Wrappers=====---------
	//--------------------==========--------------------
	
	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		resize();
	}
	
	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		resize();
	}
	
}
