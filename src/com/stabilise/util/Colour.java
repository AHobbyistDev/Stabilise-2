package com.stabilise.util;

/**
 * An RGBA colour.
 * 
 * <p>Note that no checking is done within this class to ensure inputted float
 * and integer values are within the allowed ranges of 0.0-1.0 and 0-255
 * respectively, for the purposes of performance.
 * 
 * <p>Instances of this class are mutable and hence not intrinsically
 * thread-safe.
 */
public class Colour {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	public static final Colour CLEAR = new Colour(0x00000000);
	public static final Colour BLACK = new Colour(0xFF000000);
	public static final Colour DARK_GREY = new Colour(0xFF404040);
	public static final Colour GREY = new Colour(0xFF808080);
	public static final Colour LIGHT_GREY = new Colour(0xFFBFBFBF);
	public static final Colour WHITE = new Colour(0xFFFFFFFF);
	public static final Colour RED = new Colour(0xFFFF0000);
	public static final Colour GREEN = new Colour(0xFF00FF00);
	public static final Colour BLUE = new Colour(0xFF0000FF);
	public static final Colour CYAN = new Colour(0xFF00FFFF);
	public static final Colour MAGENTA = new Colour(0xFFFF00FF);
	public static final Colour YELLOW = new Colour(0xFFFFFF00);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The colour's RGBA components, from 0.0 to 1.0. */
	private float r, g, b, a;
	
	
	/**
	 * Creates a new Colour.
	 * 
	 * @param r The red component of the colour, from 0 to 255.
	 * @param g The green component of the colour, from 0 to 255.
	 * @param b The blue component of the colour, from 0 to 255.
	 * @param a The alpha component of the colour, from 0 to 255.
	 */
	public Colour(int r, int g, int b, int a) {
		setRGBA(r, g, b, a);
	}
	
	/**
	 * Creates a new Colour.
	 * 
	 * @param r The red component of the colour, from 0.0 to 1.0.
	 * @param g The green component of the colour, from 0.0 to 1.0.
	 * @param b The blue component of the colour, from 0.0 to 1.0.
	 * @param a The alpha component of the colour, from 0.0 to 1.0.
	 */
	public Colour(float r, float g, float b, float a) {
		setRGBA(r, g, b, a);
	}
	
	/**
	 * Creates a new Colour.
	 * 
	 * @param colour The colour, encoded as 0xAARRGGBB.
	 */
	public Colour(int colour) {
		setIntValue(colour);
	}
	
	/**
	 * Creates a new colour.
	 * 
	 * @param colour The colour to copy.
	 */
	public Colour(Colour colour) {
		setRGBA(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha());
	}
	
	/**
	 * Creates a new colour that is the average of two colours.
	 * 
	 * @param colour1 The first colour.
	 * @param colour2 The second colour.
	 */
	public Colour(Colour colour1, Colour colour2) {
		setRGBA(
			(colour1.getRed() + colour2.getRed()) / 2,
			(colour1.getGreen() + colour2.getGreen()) / 2,
			(colour1.getBlue() + colour2.getBlue()) / 2,
			(colour1.getAlpha() + colour2.getAlpha()) / 2
		);
	}
	
	/**
	 * Gets the integer value of the colour, encoded as 0xAARRGGBB.
	 * 
	 * @return The value of the colour.
	 */
	public int getIntValue() {
		// For 0-255 integers:
		//colour = ((a & 0xFF) << 24) + ((r & 0xFF) << 16) + ((g & 0xFF) << 8) + ((b & 0xFF) << 0);
		// For 0-1 floats:
		return ((int)(a * 255 + 0.5f) << 24) +
				((int)(r * 255 + 0.5f) << 16) +
				((int)(g * 255 + 0.5f) << 8) +
				(int)(b * 255 + 0.5f);
	}
	
	/**
	 * Gets the red component of the colour.
	 * 
	 * @return The red component of the colour, from 0.0 to 1.0.
	 */
	public float getRed() {
		return r;
	}
	
	/**
	 * Gets the green component of the colour.
	 * 
	 * @return The green component of the colour, from 0.0 to 1.0.
	 */
	public float getGreen() {
		return g;
	}
	
	/**
	 * Gets the blue component of the colour.
	 * 
	 * @return The blue component of the colour, from 0.0 to 1.0.
	 */
	public float getBlue() {
		return b;
	}
	
	/**
	 * Gets the alpha component of the colour.
	 * 
	 * @return The alpha component of the colour, from 0.0 to 1.0.
	 */
	public float getAlpha() {
		return a;
	}
	
	/**
	 * Sets the value of the colour.
	 * 
	 * @param colour The integer value of the colour, encoded as 0xAARRGGBB.
	 */
	public void setIntValue(int colour) {
		a = ((colour >> 24) & 0xFF) / 255f;
		r = ((colour >> 16) & 0xFF) / 255f;
		g = ((colour >> 8) & 0xFF) / 255f;
		b = ((colour >> 0) & 0xFF) / 255f;
	}
	
	/**
	 * Sets the RGBA channels of the colour.
	 * 
	 * @param r The red value of the colour, from 0 to 255.
	 * @param g The green value of the colour, from 0 to 255.
	 * @param b The blue value of the colour, from 0 to 255.
	 * @param a The alpha value of the colour, from 0 to 255.
	 */
	public void setRGBA(int r, int g, int b, int a) {
		this.r = r / 255f;
		this.g = g / 255f;
		this.b = b / 255f;
		this.a = a / 255f;
	}
	
	/**
	 * Sets the RGBA channels of the colour.
	 * 
	 * @param r The red value of the colour, from 0.0 to 1.0.
	 * @param g The green value of the colour, from 0.0 to 1.0.
	 * @param b The blue value of the colour, from 0.0 to 1.0.
	 * @param a The alpha value of the colour, from 0.0 to 1.0.
	 */
	public void setRGBA(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	/**
	 * Sets the red component of the colour.
	 * 
	 * @param red The red component of the colour, from 0 to 255.
	 */
	public void setRed(int red) {
		r = red / 255f;
		//colour = (colour & 0xFF00FFFF) | ((red & 0xFF) << 16);
	}
	
	/**
	 * Sets the green component of the colour.
	 * 
	 * @param green The green component of the colour, from 0 to 255.
	 */
	public void setGreen(int green) {
		g = green / 255f;
		//colour = (colour & 0xFFFF00FF) | ((green & 0xFF) << 8);
	}
	
	/**
	 * Sets the blue component of the colour.
	 * 
	 * @param blue The blue component of the colour, from 0 to 255.
	 */
	public void setBlue(int blue) {
		b = blue / 255f;
		//colour = (colour & 0xFFFFFF00) | ((blue & 0xFF) << 0);
	}
	
	/**
	 * Sets the alpha component of the colour.
	 * 
	 * @param alpha The alpha component of the colour, from 0 to 255.
	 */
	public void setAlpha(int alpha) {
		a = alpha / 255f;
		//colour = (colour & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
	}
	
	/**
	 * Sets the red component of the colour.
	 * 
	 * @param red The red component of the colour, from 0.0 to 1.0.
	 */
	public void setRed(float red) {
		r = red;
		//colour = (colour & 0xFF00FFFF) | ((int)((r * 255 + 0.5f)) << 16);
	}
	
	/**
	 * Sets the green component of the colour.
	 * 
	 * @param green The green component of the colour, from 0.0 to 1.0.
	 */
	public void setGreen(float green) {
		g = green;
		//colour = (colour & 0xFF00FFFF) | ((int)((g * 255 + 0.5f)) << 8);
	}
	
	/**
	 * Sets the blue component of the colour.
	 * 
	 * @param blue The blue component of the colour, from 0.0 to 1.0.
	 */
	public void setBlue(float blue) {
		b = blue;
		//colour = (colour & 0xFF00FFFF) | ((int)((b * 255 + 0.5f)) << 0);
	}
	
	/**
	 * Sets the alpha component of the colour.
	 * 
	 * @param alpha The alpha component of the colour, from 0.0 to 1.0.
	 */
	public void setAlpha(float alpha) {
		a = alpha;
		//colour = (colour & 0x00FFFFFF) | ((int)(a * 255 + 0.5f) << 24);
	}
	
	/**
	 * Gets the inverse colour of this one.
	 * 
	 * @return The inverse colour.
	 */
	public Colour inverse() {
		return new Colour(1f-r, 1f-g, 1f-b, 1f-a);
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o instanceof Colour && equals((Colour)o);
	}
	
	/**
	 * Indicates whether or not some other Colour is "equal" to this one.
	 * 
	 * @param c The colour.
	 * 
	 * @return {@code true} if the given colour is equal to this one;
	 * {@code false} otherwise.
	 * @see Object#equals(Object)
	 */
	public boolean equals(Colour c) {
		return r == c.r && g == c.g && b == c.b && a == c.a;
	}
	
	@Override
	public String toString() {
		return "Colour[" + Integer.toHexString(getIntValue()) + "]";
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Clones a colour with a new alpha channel.
	 * 
	 * @param c The colour.
	 * @param alpha The alpha component of the colour, from 0.0 to 1.0.
	 * 
	 * @return The new colour.
	 */
	public static Colour cloneColourWithAlpha(Colour c, float alpha) {
		return new Colour(c.r, c.b, c.g, alpha);
	}
	
}
