package com.stabilise.opengl;

import com.stabilise.util.Log;
import com.stabilise.util.MathUtil;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * A spritesheet is effectively a grid of smaller sprites tiled into one; each
 * sprite within a SpriteSheet has the same dimensions.
 */
@LWJGLReliant
public class SpriteSheet extends Sprite {
	
	/** The width of each sprite unit. */
	protected int spriteWidth;
	/** The height of each sprite unit. */
	protected int spriteHeight;
	/** The number of textures in each row of the sheet. */
	protected int cols;
	/** The number of textures in each column of the sheet */
	protected int rows;
	
	/** The reciprocal of cols, for speeding up calculations. */
	private float colsReciprocal;
	/** The reciprocal of rows, for speeding up calculations. */
	private float rowsReciprocal;
	
	/** The most recently used sprite column for rendering. */
	protected int currCol = -1;
	/** The most recently used sprite row for rendering. */
	protected int currRow = -1;
	
	/** The spritesheet's flip state when last a sprite was set. */
	protected boolean lastFlipped = flipped;
	
	
	/**
	 * Creates a new SpriteSheet.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * spritesheet texture.
	 * @param cols The number of columns in the spritesheet.
	 * @param rows The number of rows in the spritesheet.
	 */
	public SpriteSheet(String textureFile, int cols, int rows) {
		this(textureFile, cols, rows, null);
	}
	
	/**
	 * Creates a new SpriteSheet.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * spritesheet texture.
	 * @param cols The number of columns in the spritesheet.
	 * @param rows The number of rows in the spritesheet.
	 * @param marker The object which to mark the spritesheet's texture.
	 */
	public SpriteSheet(String textureFile, int cols, int rows, Object marker) {
		this(textureFile, cols, rows, marker, false);
	}
	
	/**
	 * Creates a new SpriteSheet.
	 * 
	 * @param textureFile The file name of the image to be used for the
	 * spritesheet texture.
	 * @param cols The number of columns in the spritesheet.
	 * @param rows The number of rows in the spritesheet.
	 * @param marker The object which to mark the spritesheet's texture.
	 * @param dynamicVertices Whether or not the vertex data is likely to be
	 * changed constantly.
	 */
	public SpriteSheet(String textureFile, int cols, int rows, Object marker, boolean dynamicVertices) {
		super(textureFile, marker, dynamicVertices, true);
		
		init(cols, rows);
	}
	
	/**
	 * Initiates the SpriteSheet.
	 * 
	 * @param cols The number of sprites across.
	 * @param rows The number of sprites down.
	 */
	protected void init(int cols, int rows) {
		if(!MathUtil.isPowerOfTwo(texture.getWidth()) || !MathUtil.isPowerOfTwo(texture.getHeight()))
			Log.critical("It is strongly recommended that a spritesheet's dimensions are powers of two. (" + texture.getName() + ")");
		
		this.cols = cols;
		this.rows = rows;
		colsReciprocal = 1f / cols;
		rowsReciprocal = 1f / rows;
		spriteWidth = texture.getWidth() / cols;
		spriteHeight = texture.getHeight() / rows;
		
		//vbo.setVertexData(spriteWidth * xScale, spriteHeight * yScale);
		vbo.setVertexData(spriteWidth, spriteHeight);
	}
	
	/**
	 * Sets the active sprite (the one which will be drawn on an invocation of
	 * {@link #draw()}.
	 * 
	 * @param col The x position of the sprite within the spritesheet.
	 * @param row The y position of the sprite within the spritesheet.
	 * 
	 * @return {@code true} if the sprite is valid and has been set; 
	 * {@code false} otherwise.
	 */
	public boolean setSprite(int col, int row) {
		if(col < 0 || row < 0 || col > cols || row > rows) {
			Log.critical("Invalid sprite pos on \"" + texture.getName() + "\" (" + col + "," + row + ")");
			return false;
		}
		
		// Make sure we only reset the buffers if the sprite we're drawing isn't also the one we
		// drew last - otherwise we'd just be wasting processing power in doing so.
		// However, I still feel as if this is too processing-power-consuming
		if(col != currCol || row != currRow || lastFlipped != flipped) {
			currCol = col;
			currRow = row;
			
			//vbo.setVertexData(spriteWidth * xScale, spriteHeight * yScale, VertexBufferObject.DYNAMIC_DRAW);
			
			// N.B. texture.getWidth() / spriteWidth = cols
			// spriteWidth / texture.getWidth() = 1 / cols = colsReciprocal, ditto with height and rows
			float colsInit = col * colsReciprocal;
			float colsOffset = colsInit + colsReciprocal;
			float rowsInit = row * rowsReciprocal;
			float rowsOffset = rowsInit + rowsReciprocal;
			if(flipped) {
				vbo.setTextureData(new float[] {
						colsOffset, rowsInit,
						colsInit, rowsInit,
						colsInit, rowsOffset,
						colsOffset, rowsOffset
				});
			} else {
				vbo.setTextureData(new float[] {
						colsInit, rowsInit,
						colsOffset, rowsInit,
						colsOffset, rowsOffset,
						colsInit, rowsOffset
				});
			}
			
			lastFlipped = flipped;
			
			/*
			// Terribly unoptimised and wasteful, improved above
			vbo.setTextureData(new float[] {
					(float)col * spriteWidth / texture.getWidth(), (float)row * spriteHeight / texture.getHeight(),
					(float)(col + 1) * spriteWidth / texture.getWidth(), (float)row * spriteHeight  / texture.getHeight(),
					(float)(col + 1) * spriteWidth / texture.getWidth(), (float)(row + 1) * spriteHeight / texture.getHeight(),
					(float)col * spriteWidth / texture.getWidth(), (float)(row + 1) * spriteHeight / texture.getHeight()
			});
			*/
		}
		
		return true;
	}
	
	/**
	 * Draws a sprite from the spritesheet.
	 * 
	 * @param sprite The sprite number to render. (propagates left-to-right,
	 * top-to-bottom, starts at 0)
	 * @param x The x-coordinate at which to draw the sprite.
	 * @param y The y-coordinate at which to draw the sprite.
	 */
	public void drawSprite(int sprite, int x, int y) {
		// note sprite / cols relies on automatic integer rounding
		drawSprite(sprite % cols, sprite / cols, x, y);
	}
	
	/**
	 * Draws a sprite from the spritesheet.
	 * 
	 * @param col The x position of the sprite within the spritesheet.
	 * (Note that this begins at 0)
	 * @param row The y position of the sprite within the spritesheet.
	 * (Note that this begins at 0)
	 * @param x The x-coordinate at which to draw the sprite.
	 * @param y The y-coordinate at which to draw the sprite.
	 */
	public void drawSprite(int col, int row, int x, int y) {
		if(!setSprite(col, row)) 
			return;
		
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
	//---------=====Getter/Setter Wrappers=====---------
	//--------------------==========--------------------
	
	/**
	 * Gets the width of each individual sprite.
	 * 
	 * @return The width of each individual sprite.
	 */
	public int getSpriteWidth() {
		return spriteWidth;
	}
	
	/**
	 * Gets the height of each individual sprite.
	 * 
	 * @return The height of each individual sprite.
	 */
	public int getSpriteHeight() {
		return spriteHeight;
	}
	
	/**
	 * Gets the scaled width of each individual sprite.
	 * 
	 * @return The scaled width of each individual sprite.
	 */
	public int getScaledSpriteWidth() {
		return (int)(spriteWidth * xScale);
	}
	
	/**
	 * Gets the scaled height of each individual sprite.
	 * 
	 * @return The scaled height of each individual sprite.
	 */
	public int getScaledSpriteHeight() {
		return (int)(spriteHeight * yScale);
	}
	
	/**
	 * Sets the scaled dimensions of the spritesheet.
	 * 
	 * @param width The new scaled width of the spritesheet.
	 * @param height The new scaled height of the spritesheet.
	 */
	@Override
	public void setScaledDimensions(int width, int height) {
		this.width = width;
		this.height = height;
		xScale = (float)width / texture.getWidth();
		yScale = (float)height / texture.getHeight();
		vbo.setVertexData(spriteWidth * xScale, spriteHeight * yScale);
	}
	
	/**
	 * Sets the scaled width of the spritesheet.
	 * 
	 * @param width The new width of the spritesheet.
	 */
	@Override
	public void setScaledWidth(int width) {
		this.width = width;
		xScale = (float)width / texture.getWidth();
		vbo.setVertexData(spriteWidth * xScale, spriteHeight * yScale);
	}
	
	/**
	 * Sets the scaled height of the spritesheet.
	 * 
	 * @param height The new height of the spritesheet.
	 */
	@Override
	public void setScaledHeight(int height) {
		this.height = height;
		yScale = (float)height / texture.getHeight();
		vbo.setVertexData(spriteWidth * xScale, spriteHeight * yScale);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method will not flip the SpriteSheet's current texture
	 * data, but future invocations of {@link #setSprite(int, int)},
	 * {@link #drawSprite(int, int, int)} or
	 * {@link #drawSprite(int, int, int, int)} will be result in a sprite
	 * with the desired flip drawn.
	 */
	@Override
	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}

}
