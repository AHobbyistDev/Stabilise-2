package com.stabilise.opengl;

import com.stabilise.util.Colour;

/**
 * This class contains font style information.
 */
public class FontStyle {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Possible font paragraphing alignments. */
	public static enum Alignment {
		LEFT, RIGHT, CENTRE;
	};
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The size of the font, in pixels. */
	public final int size;
	/** The font colour */
	public final Colour colour;
	/** The font alignment. */
	public final Alignment alignment;
	/** The font kerning, in pixels. */
	public final int kerning;
	/** The vertical kerning, in pixels. */
	public final int verticalKerning;
	
	
	/**
	 * Creates a new FontStyle.
	 * 
	 * @param size The size of the font.
	 * @param colour The colour of the font.
	 * @param alignment The alignment of the font.
	 * @param kerning The font kerning.
	 * @param verticalKerning The vertical kerning.
	 */
	public FontStyle(int size, Colour colour, Alignment alignment, int kerning, int verticalKerning) {
		this.size = size;
		this.colour = colour;
		this.alignment = alignment;
		this.kerning = kerning;
		this.verticalKerning = verticalKerning;
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null &&
				o instanceof FontStyle &&
				equals((FontStyle)o);
	}
	
	/**
	 * Checks for whether or not two FontStyles are equivalent.
	 * 
	 * @param fs The FontStyle.
	 * 
	 * @return {@code true} if and only if the two FontStyles are equivalent;
	 * {@code false} otherwise.
	 */
	public boolean equals(FontStyle fs) {
		return fs != null &&
				fs.size == size &&
				colour.equals(fs.colour) &&
				fs.alignment == alignment &&
				fs.kerning == kerning;
	}

}
