package com.stabilise.opengl;

import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;

import com.stabilise.util.annotation.LWJGLReliant;

/**
 * A SpriteBatch is a Sprite which is capable of containing multiple sprites of
 * different sizes.
 */
@LWJGLReliant
public class SpriteBatch extends Sprite {
	
	/**
	 * Creates a new SpriteBatch.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * SpriteBach's texture.
	 */
	public SpriteBatch(String textureFile) {
		super(textureFile);
	}
	
	/**
	 * Creates a new SpriteBatch, which uses static rendering by default.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * SpriteBatch's texture.
	 * @param marker The object which to mark the SpriteBatch's texture.
	 */
	public SpriteBatch(String textureFile, Object marker) {
		super(textureFile, marker);
	}
	
	/**
	 * Creates a new SpriteBatch.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * SpriteBatch's texture.
	 * @param marker The object which to mark the SpriteBatch's texture.
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed often.
	 */
	public SpriteBatch(String textureFile, Object marker, boolean dynamicVertices) {
		super(textureFile, marker, dynamicVertices, true);
	}
	
	/**
	 * Sets the SpriteBatch's texture data. It is implicitly trusted that the
	 * given values are legal, to avoid needing to perform potentially costly
	 * checks. All parameters should contain values between 0.0 and 1.0, which
	 * represent relative coordinates within the texture.
	 * 
	 * <p>{@code baseX + width} and {@code baseY + height} should never be
	 * greater than 1.0.
	 * 
	 * @param baseX The base x-coordinate of the desired portion of the
	 * texture.
	 * @param width The width of the desired portion of the texture.
	 * @param baseY The base y-coordinate of the desired portion of the
	 * texture.
	 * @param height The height of the desired portion of the texture.
	 */
	public void setTextureData(float baseX, float width, float baseY, float height) {
		setTextureData(getTextureData(baseX, width, baseY, height));
	}
	
	/**
	 * Sets the SpriteBatch's texture data. It is implicitly trusted that the
	 * {@code textureData} parameter is legal, to avoid needing to perform
	 * potentially costly checks.
	 * 
	 * @param textureData The texture data.
	 */
	public void setTextureData(float[] textureData) {
		vbo.setTextureData(textureData);
	}
	
	/**
	 * Sets the SpriteBatch's texture data and draws the sprite. It is
	 * implicitly trusted that the {@code textureData} parameter is legal,
	 * to avoid needing to perform potentially costly checks.
	 * 
	 * @param textureData The texture data.
	 */
	public void drawSprite(float[] textureData) {
		setTextureData(textureData);
		draw();
	}
	
	/**
	 * Sets the SpriteBatch's texture data and draws the sprite at the
	 * specified coordinates. It is implicitly trusted that the
	 * {@code textureData} parameter is legal, to avoid needing to perform
	 * potentially costly checks.
	 * 
	 * @param textureData The texture data.
	 * @param x The x-coordinate at which to draw the sprite, in pixels.
	 * @param y The y-coordinate at which to draw the sprite, in pixels.
	 */
	public void drawSprite(float[] textureData, int x, int y) {
		setTextureData(textureData);
		drawSprite(x, y);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>The sprite most recently drawn or otherwise set will be drawn by this
	 * method.
	 */
	@Override
	public void drawSprite(int x, int y) {
		super.drawSprite(x, y);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the texture coordinates corresponding to the region of a texture
	 * specified by the parameters. The parameters generally represent pixel
	 * coordinates of a texture, though they may represent multiples of pixels
	 * to account for texture scaling, as the {@code textureDimensions}
	 * parameter is used to ensure the returned texture data represents the
	 * required region irrespective of the scaling of the texture itself (i.e.,
	 * the returned texture data will represent the same relative region of a,
	 * say, 32x32 image and a 64x64 image).
	 * 
	 * @param location The top left corner of the desired region of the
	 * texture.
	 * @param dimensions The dimensions of the desired region of the texture.
	 * @param textureDimensions The dimensions of the texture.
	 * 
	 * @return The texture coordinates representing the desired region of a
	 * texture.
	 */
	public static float[] getTextureData(Point location, Point dimensions, Point textureDimensions) {
		return getTextureData(location.getX(), location.getY(), dimensions.getX(), dimensions.getY(), textureDimensions.getX(), textureDimensions.getY());
	}
	
	/**
	 * Gets the texture coordinates corresponding to the region of a texture
	 * specified by the parameters. The parameters generally represent pixel
	 * coordinates of a texture, though they may represent multiples of pixels
	 * to account for texture scaling, as the {@code textureWidth} and
	 * {@code textureHeight} parameters ensure the returned texture data
	 * represents the required region irrespective of the scaling of the
	 * texture itself (i.e., the returned texture data will represent the same
	 * relative region of a, say, 32x32 image and a 64x64 image).
	 * 
	 * <p>The parameters should ideally satisfy the following conditions,
	 * though it isn't strictly required that they do:
	 * 
	 * <pre>
	 * 0 <= baseX <= textureWidth
	 * -textureWidth <= width <= textureWidth, width != 0
	 * -textureWidth <= baseX + width <= textureWidth
	 * 0 <= baseY <= textureHeight
	 * -textureHeight <= height <= textureHeight, height != 0
	 * -textureHeight <= baseY + height <= textureHeight
	 * </pre>
	 * 
	 * @param baseX The x-coordinate of the top left corner of the desired
	 * region of the texture.
	 * @param baseY The y-coordinate of the top left corner of the desired
	 * region of the texture.
	 * @param width The width of the desired region of the texture.
	 * @param height The height of the desired region of the texture.
	 * @param textureWidth The width of the texture.
	 * @param textureHeight The height of the texture.
	 * 
	 * @return The texture coordinates representing the desired region of a
	 * texture.
	 */
	public static float[] getTextureData(int baseX, int baseY, int width, int height, int textureWidth, int textureHeight) {
		return getTextureData((float)baseX, (float)baseY, (float)width, (float)height, (float)textureWidth, (float)textureHeight);
	}
	
	/**
	 * Gets the texture coordinates corresponding to the region of a texture
	 * specified by the parameters. The parameters generally represent pixel
	 * coordinates of a texture, though they may represent multiples of pixels
	 * to account for texture scaling, as the {@code textureWidth} and
	 * {@code textureHeight} parameters ensure the returned texture data
	 * represents the required region irrespective of the scaling of the
	 * texture itself (i.e., the returned texture data will represent the same
	 * relative region of a, say, 32x32 image and a 64x64 image).
	 * 
	 * <p>The parameters should ideally satisfy the following conditions,
	 * though it isn't strictly required that they do:
	 * 
	 * <pre>
	 * 0 <= baseX <= textureWidth
	 * -textureWidth <= width <= textureWidth, width != 0
	 * -textureWidth <= baseX + width <= textureWidth
	 * 0 <= baseY <= textureHeight
	 * -textureHeight <= height <= textureHeight, height != 0
	 * -textureHeight <= baseY + height <= textureHeight
	 * </pre>
	 * 
	 * @param baseX The x-coordinate of the top left corner of the desired
	 * region of the texture.
	 * @param baseY The y-coordinate of the top left corner of the desired
	 * region of the texture.
	 * @param width The width of the desired region of the texture.
	 * @param height The height of the desired region of the texture.
	 * @param textureWidth The width of the texture.
	 * @param textureHeight The height of the texture.
	 * 
	 * @return The texture coordinates representing the desired region of a
	 * texture.
	 */
	public static float[] getTextureData(float baseX, float baseY, float width, float height, float absoluteWidth, float absoluteHeight) {
		baseX /= absoluteWidth;
		baseY /= absoluteHeight;
		width /= absoluteWidth;
		height /= absoluteHeight;
		return getTextureData(baseX, width, baseY, height);
	}
	
	/**
	 * Gets the texture coordinates corresponding to the region of a texture
	 * specified by the parameters. The parameters represent relative
	 * coordinates on a texture, and should ideally satisfy the following
	 * conditions, though it isn't strictly required that they do:
	 * 
	 * <pre>
	 * 0.0 <= baseX <= 1.0
	 * -1.0 <= width <= 1.0, width != 0.0
	 * -1.0 <= baseX + width <= 1.0
	 * 0.0 <= baseY <= 1.0
	 * -1.0 <= height <= 1.0, height != 0.0
	 * -1.0 <= baseY + height <= 1.0
	 * </pre>
	 * 
	 * @param baseX The relative x-coordinate of the top left corner of the
	 * desired region of the texture.
	 * @param width The relative width of the desired region of the texture.
	 * @param baseY The relative y-coordinate of the top left corner of the
	 * desired region of the texture.
	 * @param height The relative height of the desired region of the texture.
	 * 
	 * @return The texture coordinates representing the desired region of a
	 * texture.
	 */
	public static float[] getTextureData(float baseX, float width, float baseY, float height) {
		width += baseX;
		height += baseY;
		return new float[] {
				baseX, baseY,			// Top-left
				width, baseY,			// Top-right
				width, height,			// Bottom-right
				baseX, height			// Bottom-left
		};
	}
	
	/**
	 * Translates each point of texture coordinates by a given amount. It is
	 * implicitly trusted that {@code texCoords} has a length of 8.
	 * 
	 * @param texCoords The texture coordinates, which should have a length of
	 * 8.
	 * @param translation A 2D vector representing the translation.
	 */
	public static void translateTexCoords(float[] texCoords, Vector2f translation) {
		translateTexCoords(texCoords, translation.x, translation.y);
	}
	
	/**
	 * Translates each point of texture coordinates by a given amount. It is
	 * implicitly trusted that {@code texCoords} has a length of 8.
	 * 
	 * @param texCoords The texture coordinates, which should have a length of
	 * 8.
	 * @param dx The change in x.
	 * @param dy The change in y.
	 */
	public static void translateTexCoords(float[] texCoords, float dx, float dy) {
		texCoords[0] += dx;
		texCoords[1] += dy;
		texCoords[2] += dx;
		texCoords[3] += dy;
		texCoords[4] += dx;
		texCoords[5] += dy;
		texCoords[6] += dx;
		texCoords[7] += dy;
	}
	
}
