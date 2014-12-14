package com.stabilise.opengl;

import static org.lwjgl.opengl.GL11.glPopMatrix;

import com.stabilise.util.annotation.LWJGLReliant;

/**
 * This class provides a convenient and easy way to display custom fonts using
 * spritesheets.
 */
@LWJGLReliant
public class Font extends SpriteSheet {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Aligns the text to the left. */
	public static final int ALIGN_LEFT = 0;
	/** Aligns the text to the right. */
	public static final int ALIGN_RIGHT = 1;
	/** Aligns the text to the centre. */
	public static final int ALIGN_CENTRE = 2;
	
	/**
	 * This is the reference map for each character.
	 * Each font spritesheet should place each character respectively in the
	 * grid positions as indicated by this constant.
	 */
	public static final String CHARS = "" +
			"ABCDEFGHIJKLMNOP" +
			"QRSTUVWXYZ.,!?'\"" +
			"0123456789=+-_:;" +
			"/\\()<>[]|*%$^&  " +
			"abcdefghijklmnop" +
			"qrstuvwxyz      " +
			"                " +
			"                " +
			"                ";
	/** The number of columns in each font spritesheet - based off the CHARS
	 * constant. */
	private static final int FONT_SHEET_COLS = 16;
	/** The number of rows in each font spritesheet - based off the CHARS
	 * constant. */
	private static final int FONT_SHEET_ROWS_NOLOWERCASE = 4;
	/** The number of rows in a font spritesheet that includes lowercase
	 * letters. */
	private static final int FONT_SHEET_ROWS_LOWERCASE = 8;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The font size, in pixels. */
	private int size;
	
	/** True if the font supports lowercase (4:1 w:h ratio is false, 2:1 w:h
	 * ratio is true.) */
	private boolean supportsLowercase;
	
	
	/**
	 * Creates a new Font.
	 * 
	 * @param fontFile The file name of the image to be used for the font's
	 * spritesheet.
	 */
	public Font(String fontFile) {
		super(fontFile, 0, 0);
	}
	
	/**
	 * Creates a new Font.
	 * 
	 * @param fontFile The file name of the image to be used for the font's
	 * spritesheet.
	 * @param marker The object which to mark the texture.
	 */
	public Font(String fontFile, Object marker) {
		super(fontFile, 0, 0, marker);
	}
	
	/**
	 * Initiates the font.
	 * 
	 * @param cols Ignored; set automatically.
	 * @param rows Ignored; set automatically.
	 */
	@Override
	protected void init(int cols, int rows) {
		// TODO: Non-square fonts?
		
		int ratio = texture.getWidth() / texture.getHeight();
		
		cols = FONT_SHEET_COLS;
		
		if(ratio == 2) {
			supportsLowercase = true;
			rows = FONT_SHEET_ROWS_LOWERCASE;
		} else if(ratio == 4) {
			supportsLowercase = false;
			rows = FONT_SHEET_ROWS_NOLOWERCASE;
		} else {
			throw new RuntimeException("Font spritesheet \"" + texture.getName() + "\" has illegal dimensions - " + texture.getWidth() + "x" + texture.getHeight() + " - ensure they are powers of 2 and in a 4:1 or 2:1 ratio.");
		}
		
		super.init(cols, rows);
		
		size = spriteHeight;
		
		//spriteWidthScale = rows * texture.getWidth() / (texture.getHeight() * cols);
		
		//filter(Texture.NEAREST_MIPMAP_NEAREST, Texture.NEAREST);
		filter(Texture.NEAREST);
	}
	
	/**
	 * Draws a single line of text to the screen.
	 * 
	 * @param line The line of text to display.
	 * @param x The x-coordinate at which to display the text.
	 * @param y The y-coordinate at which to display the text.
	 * @param style The font style.
	 */
	public void drawLine(String line, int x, int y, FontStyle style) {
		drawLine(line, x, y, style, true);
	}
	
	/**
	 * Draws a single line of text to the screen.
	 * 
	 * @param line The line of text to display.
	 * @param x The x-coordinate at which to display the text, in pixels.
	 * @param y The y-coordinate at which to display the text, in pixels.
	 * @param style The font style.
	 * @param setProperties Whether or not the properties (e.g., tint, px,
	 * shader) should be set.
	 */
	private void drawLine(String line, int x, int y, FontStyle style, boolean setProperties) {
		if(setProperties) {
			tint(style.colour);
			setAlpha(style.colour.getAlpha());
			setSize(style.size);
			applyShader();
		}
		
		if(!supportsLowercase)
			line = line.toUpperCase();
		
		for(int i = 0; i < line.length(); i++) {
			int index = CHARS.indexOf(line.charAt(i));
			// Possible performance saving
			if(index == -1 || line.charAt(i) == ' ') continue;
			
			//int col = CHARS.indexOf(line.charAt(i)) % (texture.width / spriteWidth);
			//int row = (CHARS.indexOf(line.charAt(i)) - col) / (texture.width / spriteWidth);
			
			int col = index % cols;
			int row = (index - col) / cols;
			int xLoc = 0;
			
			if(style.alignment == FontStyle.Alignment.LEFT) {
				//xLoc = (int)(x + xScale*i*(spriteWidth + kerning));
				xLoc = (int)(x + i*(size + style.kerning));
			} else if(style.alignment == FontStyle.Alignment.RIGHT) {
				//xLoc = (int)(x - xScale*(line.length()-i)*(spriteWidth + kerning));
				//xLoc = (int)(x - xScale*((line.length()-i)*spriteWidth + (line.length()-i-1)*kerning));
				xLoc = (int)(x + (i-line.length())*(size + style.kerning) - style.kerning);
			} else if(style.alignment == FontStyle.Alignment.CENTRE) {
				//xLoc = (int)(x + xScale*i*(spriteWidth + kerning) + xScale*kerning/2f - xScale*line.length()*(spriteWidth + kerning)/2f);
				// This line is the simplified form of the above formula - I've kept it commented out in case what's
				// below doesn't make much sense.
				xLoc = (int)(x + (size + style.kerning)*(i - line.length()/2f) + style.kerning/2f);
			}
			
			drawSprite(col, row, xLoc, y);
		}
		
		if(setProperties)
			unbindShader();
	}
	
	/**
	 * Draws multiple lines of text to the screen.
	 * 
	 * @param lines The lines of text to display.
	 * @param x The x-coordinate at which to display the text, in pixels.
	 * @param y The y-coordinate at which to display the text, in pixels.
	 * @param style The font style.
	 */
	public void drawLines(String[] lines, int x, int y, FontStyle style) {
		tint(style.colour);
		setAlpha(style.colour.getAlpha());
		setSize(style.size);
		//applyShader();
		for(int i = 0; i < lines.length; i++) {
			//drawLine(lines[i], x, (int)(y - getScaleY()*i*(spriteHeight + style.verticalKerning)), style);
			drawLine(lines[i], x, (int)(y - i*(style.size + style.verticalKerning)), style, false);
		}
		unbindShader();
	}
	
	/*
	@Override
	protected void drawGraphic() {
		texture.bind();
		if(hasShader)
			shader.uploadTexCoords();
		vbo.draw();
	}
	*/
	
	@Override
	protected void postDraw() {
		// TODO: super.super.super.postDraw() would be preferable
		glPopMatrix();
	}
	
	//--------------------==========--------------------
	//---------=====Getter/Setter Wrappers=====---------
	//--------------------==========--------------------
	
	/**
	 * Sets the font size.
	 * 
	 * @param size The height of the font, in pixels.
	 * 
	 * @throws IllegalArgumentException Thrown if size <= 0.
	 */
	public void setSize(int size) {
		if(this.size == size)
			return;
		
		if(size <= 0)
			throw new IllegalArgumentException("The font size must be a number >= 1!");
		
		this.size = size;
		
		//setScaledWidth(px * rows * texture.getWidth() / texture.getHeight());
		//setScaledHeight(px * rows);
		
		//scaledSpriteWidth = px * spriteWidthScale;
		//scaledSpriteHeight = size;
		
		vbo.setVertexData(size, size);
	}
	
	/**
	 * Calculates the number of characters of the font that could fit within
	 * the given number of pixels if rendered with a given font style.
	 * 
	 * @param width The width to test.
	 * @param style The font style.
	 * 
	 * @return The number of characters that could fit.
	 */
	public int getNumFittingCharacters(int width, FontStyle style) {
		return width / (style.size * style.kerning);
	}
	
	@Override
	public int getScaledSpriteWidth() {
		return size;
	}
	
	@Override
	public int getScaledSpriteHeight() {
		return size;
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setScaledDimensions(int width, int height) {
		throw new RuntimeException("setScaledWidth(int) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setScaledWidth(int width) {
		throw new RuntimeException("setScaledWidth(int) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setScaledHeight(int height) {
		throw new RuntimeException("setScaledHeight(int) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setScale(float scale) {
		throw new RuntimeException("setScaleX(float) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setScaleX(float scale) {
		throw new RuntimeException("setScaleX(float) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setScaleY(float scale) {
		throw new RuntimeException("setScaleY(float) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void resetDimensions() {
		throw new RuntimeException("resetDimensions() may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setWidth(int width) {
		throw new RuntimeException("setWidth(int) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setHeight(int height) {
		throw new RuntimeException("setHeight(int) may not be invoked for a Font!");
	}
	
	/**
	 * Throws a RuntimeException - this may not be invoked for a Font.
	 */
	@Override
	public void setPivot(int x, int y) {
		throw new RuntimeException("setPivot(int, int) may not be invoked for a Font!");
	}

}
