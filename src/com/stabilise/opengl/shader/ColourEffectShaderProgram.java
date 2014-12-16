package com.stabilise.opengl.shader;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import org.lwjgl.LWJGLException;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * The standard shader which generates colour effects (e.g. tint) for graphics.
 */
@LWJGLReliant
@Deprecated
public class ColourEffectShaderProgram extends ShaderProgram {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The vertex shader for the colour effect shader program. */
	private static final String COLOUR_EFFECT_VERTEX_SHADER_SRC = "" +
			"attribute vec2 " + ATTRIBUTE_POSITION_NAME + ";\n" +
			"attribute vec4 " + ATTRIBUTE_COLOUR_NAME + ";\n" +
			"attribute vec2 " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
			"varying vec2 vTexCoord;\n" +
			"void main() {\n" +
			" vTexCoord = " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
			" gl_Position = ftransform();\n" +
			"}";
	/** The colour effect vertex shader ID. */
	private static int COLOUR_EFFECT_VERTEX_SHADER = 0;
	/** The fragment shader for the colour effect shader program. */
	private static final String COLOUR_EFFECT_FRAGMENT_SHADER_SRC = "" +
			"uniform sampler2D " + UNIFORM_TEXTURE_NAME + ";\n" +
			"uniform vec3 tint;\n" +
			"uniform float opacity;\n" +
			"uniform float alpha;\n" +
			"varying vec2 vTexCoord;\n" +
			"void main() {\n" +
			" vec4 texColor = texture2D(" + UNIFORM_TEXTURE_NAME + ", vTexCoord);\n" +
			//" gl_FragColor = vec4(texColor.rgb*tint*opacity, texColor.a*alpha);\n" +	// Multiplicative shader
			" gl_FragColor = vec4(texColor.rgb*(1.0-opacity) + tint*opacity, texColor.a*alpha);\n" +		// Additive shader
			"}";
	/** The colour effect fragment shader ID. */
	private static int COLOUR_EFFECT_FRAGMENT_SHADER = 0;
	/** The colour effect shader program. */
	private static ColourEffectShaderProgram COLOUR_EFFECT_SHADER_PROGRAM;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The ID of the alpha uniform. */
	private int alphaID;
	/** The ID of the opacity uniform. */
	private int opacityID;
	/** The ID of the tint uniform. */
	private int tintID;
	
	
	/**
	 * Creates the colour effect shader program.
	 * 
	 * @throws LWJGLException Thrown if the program could not be created.
	 */
	public ColourEffectShaderProgram() throws LWJGLException {
		super(getColourEffectVertexShader(), getColourEffectFragmentShader(), SPRITE_ATTRIBUTES);
		
		unloadable = false;
		
		alphaID = getUniformID("alpha");
		opacityID = getUniformID("opacity");
		tintID = getUniformID("tint");
	}
	
	/**
	 * Sets the shader's alpha uniform.
	 * 
	 * @param alpha The alpha.
	 */
	public void setAlpha(float alpha) {
		try {
			setUniformFloat(alphaID, alpha);
		} catch(LWJGLException e) {
			// ignored
		}
	}
	
	/**
	 * Sets the shader's opacity uniform. The opacity controls the strength of
	 * the tint; 0.0 means the tint is nonexistent, and 1.0 means the tint is
	 * completely opaque.
	 * 
	 * @param opacity The opacity.
	 */
	public void setOpacity(float opacity) {
		try {
			setUniformFloat(opacityID, opacity);
		} catch(LWJGLException e) {
			// ignored
		}
	}
	
	/**
	 * Sets the shader's tint uniform.
	 * 
	 * @param r The tint's red component.
	 * @param g The tint's green component.
	 * @param b The tint's blue component.
	 */
	public void setTint(float r, float g, float b) {
		try {
			setUniformVec3f(tintID, r, g, b);
		} catch(LWJGLException e) {
			// ignored
		}
	}
	
	/**
	 * Gets the colour effect vertex shader, and compiles it if it does not
	 * exist.
	 * 
	 * @return The ID of the colour effect vertex shader, or {@code 0} if it
	 * could not be compiled.
	 */
	private static int getColourEffectVertexShader() {
		if(COLOUR_EFFECT_VERTEX_SHADER != 0)
			return COLOUR_EFFECT_VERTEX_SHADER;
		
		int shader;
		try {
			shader = compileShader(COLOUR_EFFECT_VERTEX_SHADER_SRC, GL_VERTEX_SHADER);
		} catch(LWJGLException e) {
			Log.critical("Could not compile colour effect vertex shader!", e);
			return 0;
		}
		
		return COLOUR_EFFECT_VERTEX_SHADER = shader;
	}
	
	/**
	 * Gets the colour effect fragment shader, and compiles it if it does not
	 * exist.
	 * 
	 * @return The ID of the colour effect fragment shader, or {@code 0} if it
	 * could not be compiled.
	 */
	private static int getColourEffectFragmentShader() {
		if(COLOUR_EFFECT_FRAGMENT_SHADER != 0)
			return COLOUR_EFFECT_FRAGMENT_SHADER;
		
		int shader;
		try {
			shader = compileShader(COLOUR_EFFECT_FRAGMENT_SHADER_SRC, GL_FRAGMENT_SHADER);
		} catch(LWJGLException e) {
			Log.critical("Could not compile colour effect fragment shader!", e);
			return 0;
		}
		
		return COLOUR_EFFECT_FRAGMENT_SHADER = shader;
	}
	
	/**
	 * Gets the colour effect shader program.
	 * 
	 * @return The colour effect shader program.
	 */
	public static ColourEffectShaderProgram getColourEffectShaderProgram() {
		if(COLOUR_EFFECT_SHADER_PROGRAM != null)
			return COLOUR_EFFECT_SHADER_PROGRAM;
		
		try {
			COLOUR_EFFECT_SHADER_PROGRAM = new ColourEffectShaderProgram();
		} catch(LWJGLException e) {
			Log.critical("Could not compile colour effect shader program: " + e.getMessage());
			return null;
		}
		
		return COLOUR_EFFECT_SHADER_PROGRAM;
	}
	
}
