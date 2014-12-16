package com.stabilise.opengl.shader;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import org.lwjgl.LWJGLException;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.Incomplete;

/**
 * incomplete; see:
 * http://s22.postimg.org/ezch3tdkh/B3rm_ZGBIAAAQQA0.png
 */
@Incomplete
@Deprecated
public class ColouriseShaderProgram extends ShaderProgram {

	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The vertex shader for the colourise shader program. */
	private static final String COLOURISE_VERTEX_SHADER_SRC = "" +
			"attribute vec2 " + ATTRIBUTE_POSITION_NAME + ";\n" +
			"attribute vec4 " + ATTRIBUTE_COLOUR_NAME + ";\n" +
			"attribute vec2 " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
			"varying vec2 vTexCoord;\n" +
			"void main() {\n" +
			" vTexCoord = " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
			" gl_Position = ftransform();\n" +
			"}";
	/** The colourise vertex shader ID. */
	private static int COLOURISE_VERTEX_SHADER = 0;
	/** The fragment shader for the colourise shader program. */
	private static final String COLOURISE_FRAGMENT_SHADER_SRC = "" +
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
	/** The colourise fragment shader ID. */
	private static int COLOURISE_FRAGMENT_SHADER = 0;
	/** The colourise shader program. */
	private static ColouriseShaderProgram COLOURISE_SHADER_PROGRAM;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The ID of the alpha uniform. */
	@SuppressWarnings("unused")
	private int alphaID;
	/** The ID of the opacity uniform. */
	@SuppressWarnings("unused")
	private int opacityID;
	/** The ID of the tint uniform. */
	@SuppressWarnings("unused")
	private int tintID;
	
	
	/**
	 * Creates the colourise shader program.
	 * 
	 * @throws LWJGLException Thrown if the program could not be created.
	 */
	public ColouriseShaderProgram() throws LWJGLException {
		super(getColouriseVertexShader(), getColouriseFragmentShader(), SPRITE_ATTRIBUTES);
		
		unloadable = false;
		
		alphaID = getUniformID("alpha");
		opacityID = getUniformID("opacity");
		tintID = getUniformID("tint");
	}
	
	/**
	 * Gets the colourise vertex shader, and compiles it if it does not
	 * exist.
	 * 
	 * @return The ID of the colourise vertex shader, or {@code 0} if it
	 * could not be compiled.
	 */
	private static int getColouriseVertexShader() {
		if(COLOURISE_VERTEX_SHADER != 0)
			return COLOURISE_VERTEX_SHADER;
		
		int shader;
		try {
			shader = compileShader(COLOURISE_VERTEX_SHADER_SRC, GL_VERTEX_SHADER);
		} catch(LWJGLException e) {
			Log.critical("Could not compile colourise vertex shader!", e);
			return 0;
		}
		
		return COLOURISE_VERTEX_SHADER = shader;
	}
	
	/**
	 * Gets the colourise fragment shader, and compiles it if it does not
	 * exist.
	 * 
	 * @return The ID of the colourise fragment shader, or {@code 0} if it
	 * could not be compiled.
	 */
	private static int getColouriseFragmentShader() {
		if(COLOURISE_FRAGMENT_SHADER != 0)
			return COLOURISE_FRAGMENT_SHADER;
		
		int shader;
		try {
			shader = compileShader(COLOURISE_FRAGMENT_SHADER_SRC, GL_FRAGMENT_SHADER);
		} catch(LWJGLException e) {
			Log.critical("Could not compile colourise fragment shader!", e);
			return 0;
		}
		
		return COLOURISE_FRAGMENT_SHADER = shader;
	}
	
	/**
	 * Gets the colourise shader program.
	 * 
	 * @return The colourise shader program.
	 */
	public static ColouriseShaderProgram getColouriseShaderProgram() {
		if(COLOURISE_SHADER_PROGRAM != null)
			return COLOURISE_SHADER_PROGRAM;
		
		try {
			COLOURISE_SHADER_PROGRAM = new ColouriseShaderProgram();
		} catch(LWJGLException e) {
			Log.critical("Could not compile colourise shader program: " + e.getMessage());
			return null;
		}
		
		return COLOURISE_SHADER_PROGRAM;
	}

}
