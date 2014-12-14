package com.stabilise.opengl;

import com.stabilise.util.Colour;
import com.stabilise.util.MathUtil;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * A Sprite serves as a wrapper for a 2D bitmap texture which is capable of
 * being drawn to the OpenGL context.
 * 
 * <!-- TODO: this class and its derivatives are incredibly inefficient -->
 */
@LWJGLReliant
public class Sprite extends Graphic {
	
	/** The sprite's associated texture. */
	protected Texture texture;
	/** The sprite's texture group. */
	protected Object marker = null;
	
	/** The sprite's VertexBufferObject. */
	protected TextureVBO vbo;
	
	/** Internal processor-conservative storage of the x-axis scaling. */
	protected float xScale = 1;
	/** Internal processor-conservative storage of the y-axis scaling. */
	protected float yScale = 1;
	
	/** Whether or not the sprite has a shader applied. */
	protected boolean hasShader = false;
	/** The sprite's tint. */
	private Colour tint;
	/** The sprite's alpha. */
	private float alpha = 1.0f;
	
	/** Whether or not the sprite has been flipped. */
	protected boolean flipped = false;
	
	
	/**
	 * Creates a new Sprite.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * Sprite's texture.
	 */
	public Sprite(String textureFile) {
		this(textureFile, null);
	}
	
	/**
	 * Creates a new Sprite, which is uses static rendering by default.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * Sprite's texture.
	 * @param marker The object with which to mark the Sprite's texture.
	 */
	public Sprite(String textureFile, Object marker) {
		this(textureFile, marker, false, false);
	}
	
	/**
	 * Creates a new Sprite.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * Sprite's texture.
	 * @param marker The object with which to mark the Sprite's texture.
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed often.
	 * @param dynamicTexture Whether or not the texture data is likely to be
	 * changed often.
	 */
	public Sprite(String textureFile, Object marker, boolean dynamicVertices, boolean dynamicTexture) {
		/*
		this.marker = marker;
		try {
			texture = Texture.loadTexture(textureFile).addMarker(marker);
			width = texture.getWidth();
			height = texture.getHeight();
			
			//texture.bind();
			vbo = new TextureVBO(dynamicVertices, dynamicTexture);
			vbo.setVertexData(width, height);
			
			// Just grab the shader regardless of use for now
			shader = ColourEffectShaderProgram.getColourEffectShaderProgram();
			//shader = ShaderProgram.getDefaultShaderProgram();
			
		//} catch(IOException | LWJGLException e) {
		} catch(Exception e) {
			Log.critical("Could not load sprite texture \"" + textureFile + "\"", e);
			destroyed = true;
		}
		*/
	}
	
	/**
	 * Applies a filter to the sprite's texture.
	 * 
	 * @param filter Either Texture.LINEAR or Texture.NEAREST.
	 */
	public void filter(int filter) {
		texture.filter(filter);
	}
	
	/**
	 * Applies a filter to the sprite's texture.
	 * 
	 * @param minFilter The mag filter.
	 * @param magFilter The min filter.
	 */
	public void filter(int minFilter, int magFilter) {
		texture.filter(minFilter, magFilter);
	}
	
	/**
	 * Sets the sprite's alpha.
	 * 
	 * @param alpha The sprite's alpha, from 0.0 to 1.0.
	 * 
	 * @throws IllegalArgumentException If the given value is outside the
	 * allowable range.
	 */
	public void setAlpha(float alpha) {
		if(alpha < 0.0f || alpha > 1.0f)
			throw new IllegalArgumentException("The alpha must be between 0.0 and 1.0!");
		
		this.alpha = alpha;
		
		if(alpha == 1.0f)
			removeEffectShader();
		else
			setupEffectShader();
	}
	
	/**
	 * Tints the Sprite completely.
	 * 
	 * <p>An invocation of this behaves the same as
	 * {@link #tint(Colour, float) tint(colour, 1.0f)}
	 * 
	 * @param colour The colour to tint the sprite.
	 */
	public void tint(Colour colour) {
		tint(colour, 1.0f);
	}
	
	/**
	 * Tints the Sprite.
	 * 
	 * @param colour The colour to tint the sprite.
	 * @param opacity The opacity of the tint, from 0.0 to 1.0.
	 * 
	 * @throws IllegalArgumentException if the given opacity is < 0.0 or > 1.0.
	 */
	public void tint(Colour colour, float opacity) {
		if(opacity < 0.0f || opacity > 1.0f)
			throw new IllegalArgumentException("The opacity must be between 0.0 and 1.0!");
		
		tint = new Colour(colour);
		tint.setAlpha(opacity);
		
		setupEffectShader();
	}
	
	/**
	 * Removes any tint from the sprite.
	 */
	public void removeTint() {
		if(tint != null)
			tint.setAlpha(0.0f);
		
		removeEffectShader();
	}
	
	/**
	 * Sets up the effect shader, if it is not already set up.
	 */
	private void setupEffectShader() {
		if(hasShader)
			return;
		
		hasShader = true;
		//shader = ColourEffectShaderProgram.getColourEffectShaderProgram();
	}
	
	/**
	 * Removes the effect shader if there are no effects used, and it is not
	 * already set up.
	 */
	private void removeEffectShader() {
		// Don't remove it if there are still things it can do
		if(!hasShader || alpha != 1.0f || (tint != null && tint.getAlpha() != 0.0f))
			return;
		
		hasShader = false;
		//shader = ShaderProgram.getDefaultShaderProgram();
	}
	
	/**
	 * Draws the Sprite at the specified coordinates. The Sprite's coordinates
	 * will be set by this method.
	 * 
	 * @param x The x-coordinate at which to draw the Sprite, in pixels.
	 * @param y The y-coordinate at which to draw the Sprite, in pixels.
	 */
	public void drawSprite(int x, int y) {
		this.x = x;
		this.y = y;
		
		draw();
	}
	
	@Override
	protected void transform() {
		/*
		if(rotation != 0)
			glRotatef((float)(rotation),0,0,1);
		// Centre the sprite on its pivot for rotation 
		glTranslatef(-pivotX * xScale, -pivotY * yScale, 0);
		*/
	}
	
	@Override
	protected void drawGraphic() {
		texture.bind();
		vbo.preDraw();
		applyShader();
		vbo.draw();
	}
	
	/**
	 * Applies the colour effect shader, if applicable.
	 */
	protected void applyShader() {
		if(hasShader) {
			bindShader();
			//shader.uploadTexCoords();
			
			//try {
			shader.setAlpha(alpha);
			if(tint != null) {
				shader.setOpacity(tint.getAlpha());
				shader.setTint(tint.getRed(), tint.getGreen(), tint.getBlue());
			} else {
				shader.setOpacity(0.0f);
				shader.setTint(0.0f, 0.0f, 0.0f);
			}
			//} catch(LWJGLException e) {
			//	Log.critical("Could not set colour effect shader uniform: " + e.getMessage());
			//}
		}
		
		/*
		// Vincent's shader-related stuff, for reference
		
		GL20.glUniform1i(screenInstance.uniform_index, index);
		GL20.glUniform1f(screenInstance.uniform_tileRatioX, tileRatioX);
		GL20.glUniform1f(screenInstance.uniform_tileRatioY, tileRatioY);
		
		GL20.glEnableVertexAttribArray(screenInstance.attrib_coord2d);
		GL20.glVertexAttribPointer(screenInstance.attrib_coord2d, 2, GL11.GL_FLOAT, false, 0, 0);
		screenInstance.translate(position);
		GL11.glDrawArrays(GL11.GL_QUADS, 0, vbo_tile.size());
		screenInstance.translate(position.negate(null));
		
		GL20.glDisableVertexAttribArray(screenInstance.attrib_coord2d);
		*/
	}
	
	@Override
	protected void postDraw() {
		super.postDraw();
		if(hasShader)
			unbindShader();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		vbo.destroy();
		
		if(marker != null)
			Texture.unloadTexture(texture.getName(), marker);
		else
			Texture.unloadTexture(texture.getName());
	}
	
	//--------------------==========--------------------
	//---------=====Getter/Setter Wrappers=====---------
	//--------------------==========--------------------
	
	/**
	 * Gets the scaled width of the sprite.
	 * 
	 * @return The sprite's scaled width.
	 */
	@Override
	public int getWidth() {
		return super.getWidth();
	}
	
	/**
	 * Gets the scaled height of the sprite.
	 * 
	 * @return The sprite's scaled height.
	 */
	@Override
	public int getHeight() {
		return super.getHeight();
	}
	
	/**
	 * Gets the width of the sprite's texture.
	 * 
	 * @return The width of the sprite's texture.
	 */
	public int getTextureWidth() {
		return texture.getWidth();
	}
	
	/**
	 * Gets the height of the sprite's texture.
	 * 
	 * @return The height of the sprite's texture.
	 */
	public int getTextureHeight() {
		return texture.getHeight();
	}
	
	/**
	 * Gets the x-axis scaling of the sprite.
	 * 
	 * @return The x-axis scaling of the sprite.
	 */
	public float getScaleX() {
		return xScale;
	}
	
	/**
	 * Gets the y-axis scaling of the sprite.
	 * 
	 * @return The y-axis scaling of the sprite.
	 */
	public float getScaleY() {
		return yScale;
	}
	
	/**
	 * Sets the scaled dimensions of the sprite.
	 * 
	 * @param width The new scaled width of the sprite.
	 * @param height The new scaled height of the sprite.
	 */
	public void setScaledDimensions(int width, int height) {
		this.width = width;
		this.height = height;
		xScale = (float)width / texture.getWidth();
		yScale = (float)height / texture.getHeight();
		vbo.setVertexData(width, height);
	}
	
	/**
	 * Sets the scaled width of the sprite.
	 * 
	 * @return width The new scaled width of the sprite.
	 */
	public void setScaledWidth(int width) {
		this.width = width;
		xScale = (float)width / texture.getWidth();
		vbo.setVertexData(width, height);
	}
	
	/**
	 * Sets the scaled height of the sprite.
	 * 
	 * @param height The new scaled height of the sprite.
	 */
	public void setScaledHeight(int height) {
		this.height = height;
		yScale = (float)height / texture.getHeight();
		vbo.setVertexData(width, height);
	}
	
	/**
	 * Sets both the x-axis and y-axis scaling of the sprite.
	 * 
	 * @param scale The new scale of the sprite.
	 */
	public void setScale(float scale) {
		setScaleX(scale);
		setScaleY(scale);
	}
	
	/**
	 * Sets the x-axis scaling of the sprite.
	 * 
	 * @param scale The new scale of the sprite.
	 */
	public void setScaleX(float scale) {
		setScaledWidth(MathUtil.fastRound(scale * texture.getWidth()));
		xScale = scale;
	}
	
	/**
	 * Sets the y-axis scaling of the sprite.
	 * 
	 * @param scale The new scale of the sprite.
	 */
	public void setScaleY(float scale) {
		setScaledHeight(MathUtil.fastRound(scale * texture.getHeight()));
		yScale = scale;
	}
	
	/**
	 * Resets the sprite's dimensions to those of its texture.
	 */
	public void resetDimensions() {
		setScaledWidth(texture.getWidth());
		setScaledHeight(texture.getHeight());
	}
	
	/**
	 * Gets the horizontal flip of the sprite.
	 * 
	 * @return Whether or not the sprite is flipped.
	 */
	public boolean getFlipped() {
		return flipped;
	}
	
	/**
	 * Sets the horizontal flip of the sprite.
	 * 
	 * @param flipped Whether or not the sprite should be flipped.
	 */
	public void setFlipped(boolean flipped) {
		if(this.flipped != flipped) {
			if(this.flipped = flipped) {
				vbo.setTextureData(new float[] {
						1, 0,			// Top-left
						0, 0,			// Top-right
						0, 1,			// Bottom-right
						1, 1			// Bottom-left
				});
			} else {
				vbo.setTextureData(new float[] {
						0, 0,			// Top-left
						1, 0,			// Top-right
						1, 1,			// Bottom-right
						0, 1			// Bottom-left
				});
			}
		}
	}
	
	/**
	 * Sets the sprite's texture marker, if the sprite currently has none.
	 * 
	 * @param marker The marker with which to attempt to mark the sprite's
	 * texture.
	 */
	public void setTextureMarker(Object marker) {
		if(marker == null) {
			this.marker = marker;
			texture.addMarker(marker);
		}
	}
}
