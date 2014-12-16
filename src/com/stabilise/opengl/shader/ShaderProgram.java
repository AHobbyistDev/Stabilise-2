package com.stabilise.opengl.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

import com.google.common.base.Joiner;
import com.stabilise.core.Resources;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * A shader program is used to produce graphical alterations.
 * 
 * <p>Some code is borrowed from mattdesl's
 * <a href=https://github.com/mattdesl/lwjgl-basics>lwjgl-basics library</a>.
 */
@LWJGLReliant
@Deprecated
public class ShaderProgram {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The file extension for vertex shaders. */
	private static final String VERTEX_SHADER_FILE_EXTENSION = "vert";
	/** The file extension for fragment shaders. */
	private static final String FRAGMENT_SHADER_FILE_EXTENSION = "frag";
	
	/** Whether or not shaders are supported. */
	public static final boolean shadersSupported = getShadersSupported();
	
	/** The GLSL name for the texture uniform. */
	public static final String UNIFORM_TEXTURE_NAME = "u_texture";
	
	/** The GLSL name for the colour attribute. */
	public static final String ATTRIBUTE_COLOUR_NAME = "Color";
	/** The GLSL name for the position attribute. */
	public static final String ATTRIBUTE_POSITION_NAME = "Position";
	/** The GLSL name for the TexCoord attribute. */
	public static final String ATTRIBUTE_TEXCOORD_NAME = "TexCoord";
	
	/** The vertex shader for the default shader program. */
	private static final String DEFAULT_VERTEX_SHADER_SRC = "" +
			"attribute vec2 " + ATTRIBUTE_POSITION_NAME + ";\n" +
			"attribute vec4 " + ATTRIBUTE_COLOUR_NAME + ";\n" +
			"attribute vec2 " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
			"varying vec2 vTexCoord;\n" +
			"void main() {\n" +
			" vTexCoord = " + ATTRIBUTE_TEXCOORD_NAME + ";\n" +
			" gl_Position = ftransform();\n" +
			"}";
	/** The default vertex shader ID. */
	private static int DEFAULT_VERTEX_SHADER = 0;
	/** The fragment shader for the default shader program. */
	private static final String DEFAULT_FRAGMENT_SHADER_SRC = "" +
			"uniform sampler2D " + UNIFORM_TEXTURE_NAME + ";\n" +
			"varying vec2 vTexCoord;\n" +
			"void main() {\n" +
			" gl_FragColor = texture2D(" + UNIFORM_TEXTURE_NAME + ", vTexCoord);\n" +
			"}";
	/** The default fragment shader ID. */
	private static int DEFAULT_FRAGMENT_SHADER = 0;
	/** The default shader program. */
	private static ShaderProgram DEFAULT_SHADER_PROGRAM;
	
	/** The position attribute. */
	protected static final Attribute ATTRIBUTE_POSITION = new Attribute(0, ATTRIBUTE_POSITION_NAME, 2);
	/** The colour attribute. */
	protected static final Attribute ATTRIBUTE_COLOUR = new Attribute(1, ATTRIBUTE_COLOUR_NAME, 4);
	/** The texture coordinate attribute. */
	protected static final Attribute ATTRIBUTE_TEXCOORD = new Attribute(2, ATTRIBUTE_TEXCOORD_NAME, 2);
	
	/** The attributes to be used by a Sprite. */
	public static final List<Attribute> SPRITE_ATTRIBUTES = Arrays.asList(
			ATTRIBUTE_POSITION,
			ATTRIBUTE_COLOUR,
			ATTRIBUTE_TEXCOORD
	);
	
	/**
	 * This class represents a shader attribute.
	 */
	public static class Attribute {
		
		/** The attribute's ID. */
		public final int id;
		/** The attribute's name. */
		public final String name;
		/** The attribute's size, for array attributes. */
		public final int size;
		
		/**
		 * Creates a new Attribute.
		 * 
		 * @param id The attribute's ID.
		 * @param name The attribute's name.
		 * @param size The attribute's size.
		 */
		public Attribute(int id, String name, int size) {
			this.id = id;
			this.name = name;
			this.size = size;
		}
	}
	
	/** Used for temporarily storing matrices. */
	private static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The program's internal ID. */
	protected int id;
	
	/** The map of uniforms used in the shader. */
	protected HashMap<String, Integer> uniforms = new HashMap<String, Integer>();
	/** The array of attributes used in the shader. */
	protected Attribute[] attributes;
	
	/** The vertex shader ID. */
	protected int vertexShader;
	/** The fragment shader ID. */
	protected int fragmentShader;
	
	/** Whether or not the program is capable of being unloaded. */
	protected boolean unloadable = true;
	
	
	/**
	 * Creates a new ShaderProgram.
	 * 
	 * @param name The name of the program.
	 * @param attributes Attributes within the program.
	 * 
	 * @throws IOException Thrown if an I/O exception is encountered while
	 * reading the shader files.
	 * @throws LWJGLException if the program could not be created.
	 */
	public ShaderProgram(String name, List<Attribute> attributes) throws IOException, LWJGLException {
		this(
				Joiner.on('\n').join(Resources.loadTextFileFromFileSystem(new File(Resources.SHADER_DIR, name + "." + VERTEX_SHADER_FILE_EXTENSION)), "\n"),
				Joiner.on('\n').join(Resources.loadTextFileFromFileSystem(new File(Resources.SHADER_DIR, name + "." + FRAGMENT_SHADER_FILE_EXTENSION)), "\n"),
				attributes
		);
	}
	
	/**
	 * Creates a new ShaderProgram. The separate shaders will be unloaded
	 * after creating the program.
	 * 
	 * @param vertexShaderSrc The vertex shader source.
	 * @param fragmentShaderSrc The fragment shader source.
	 * @param attributes Attributes within the program.
	 * 
	 * @throws LWJGLException if the program could not be created.
	 */
	public ShaderProgram(CharSequence vertexShaderSrc, CharSequence fragmentShaderSrc, List<Attribute> attributes) throws LWJGLException {
		this(compileShader(vertexShaderSrc, GL_VERTEX_SHADER), compileShader(fragmentShaderSrc, GL_FRAGMENT_SHADER), attributes);
		
		// If we load the shader sources into a constructor they're clearly
		// temporary, so we'll unload them right after
		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);
		vertexShader = 0;
		fragmentShader = 0;
	}
	
	/**
	 * Creates a new ShaderProgram.
	 * 
	 * @param vertexShader The vertex shader id.
	 * @param fragmentShader The fragment shader id.
	 * @param attributes Attributes within the shader.
	 * 
	 * @throws LWJGLException if the program could not be created.
	 */
	public ShaderProgram(int vertexShader, int fragmentShader, List<Attribute> attributes) throws LWJGLException {
		if(!shadersSupported)
			throw new LWJGLException("Shaders are not supported!");
		
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		
		// Create the program
		id = glCreateProgram();
		
		// Bind the defined attributes
		if(attributes != null) {
			for(Attribute a : attributes) {
				if(a != null)
					glBindAttribLocation(id, a.id, a.name);
			}
		}
		
		// Compile the program
		glAttachShader(id, vertexShader);
		glAttachShader(id, fragmentShader);
		glLinkProgram(id);
		glValidateProgram(id);
		
		if(glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE)
			Log.critical("Could not set up shader program: " + glGetShaderInfoLog(id, glGetShaderi(id, GL_INFO_LOG_LENGTH)));
		
		// Get the program uniforms
		int len = glGetProgrami(id, GL_ACTIVE_UNIFORMS);
		int strLen = glGetProgrami(id, GL_ACTIVE_UNIFORM_MAX_LENGTH);

		for(int i = 0; i < len; i++) {
			String name = glGetActiveUniform(id, i, strLen);
			int uid = glGetUniformLocation(id, name);
			uniforms.put(name, uid);
		}
		
		// Get the program attributes
		len = glGetProgrami(id, GL_ACTIVE_ATTRIBUTES);
		strLen = glGetProgrami(id, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH);

		this.attributes = new Attribute[len];
		for(int i = 0; i < len; i++) {
			String name = glGetActiveAttrib(id, i, strLen);
			int size = glGetActiveAttribSize(id, i);
			//int type = glGetActiveAttribType(id, i);
			int attributeID = glGetAttribLocation(id, name);
			this.attributes[i] = new Attribute(attributeID, name, size);
		}
	}
	
	/**
	 * Uploads a bound texture's texture coordinates to the shader for use.
	 */
	public void uploadTexCoords() {
		glEnableVertexAttribArray(ATTRIBUTE_TEXCOORD.id);
		glVertexAttribPointer(ATTRIBUTE_TEXCOORD.id, ATTRIBUTE_TEXCOORD.size, GL_FLOAT, false, 0, 0);
	}
	
	/**
	 * Binds the shader program for use.
	 */
	public void bind() {
		glUseProgram(id);
		
		// Explicitly send the texcoords
		uploadTexCoords();
		
		/*
		for(int i = 0; i < attributes.length; i++) {
			Attribute a = attributes[i];
			glEnableVertexAttribArray(a.id);
			glVertexAttribPointer(a.id, a.size, GL_FLOAT, false, 0, 0);
		}
		*/
	}
	
	/**
	 * Unbinds the shader program.
	 */
	public void unbind() {
		glDisableVertexAttribArray(ATTRIBUTE_TEXCOORD.id);
		glUseProgram(0);
	}
	
	/**
	 * Unloads the shader program.
	 */
	public void unload() {
		if(!unloadable)
			return;
		glDeleteProgram(id);
		uniforms.clear();
	}
	
	/**
	 * Gets the ID of a given uniform.
	 * 
	 * @param name The name of the uniform.
	 * 
	 * @return The ID of the uniform.
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected int getUniformID(String name) throws LWJGLException {
		int uniform = -1;
		Integer i = uniforms.get(name);
		
		if(i == null) {
			uniform = glGetUniformLocation(id, name);
			if(uniform == -1)
				throw new LWJGLException("Uniform does not exist: " + name);
			uniforms.put(name, uniform);
			return uniform;
		}
		
		return i.intValue();
	}
	
	/**
	 * Sets a float uniform.
	 * 
	 * @param name The name of the uniform.
	 * @param value The value to set.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformFloat(String name, float value) throws LWJGLException {
		setUniformFloat(getUniformID(name), value);
	}
	
	/**
	 * Sets a float uniform.
	 * 
	 * @param id The id of the uniform.
	 * @param value The value to set.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformFloat(int id, float value) throws LWJGLException {
		glUniform1f(id, value);
	}
	
	/**
	 * Sets a vec2f uniform. Suitable for x/y values.
	 * 
	 * @param name The name of the uniform.
	 * @param a The first float value.
	 * @param b The second float value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformVec2f(String name, float a, float b) throws LWJGLException {
		setUniformVec2f(getUniformID(name), a, b);
	}
	
	/**
	 * Sets a vec2f uniform. Suitable for x/y values.
	 * 
	 * @param id The id of the uniform.
	 * @param a The first float value.
	 * @param b The second float value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformVec2f(int id, float a, float b) throws LWJGLException {
		glUniform2f(id, a, b);
	}
	
	/**
	 * Sets a vec3f uniform. Suitable for x/y/z and r/g/b values.
	 * 
	 * @param name The name of the uniform.
	 * @param a The first float value.
	 * @param b The second float value.
	 * @param c The third float value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformVec3f(String name, float a, float b, float c) throws LWJGLException {
		setUniformVec3f(getUniformID(name), a, b, c);
	}
	
	/**
	 * Sets a vec3f uniform. Suitable for x/y/z and r/g/b values.
	 * 
	 * @param id The id of the uniform.
	 * @param a The first float value.
	 * @param b The second float value.
	 * @param c The third float value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformVec3f(int id, float a, float b, float c) throws LWJGLException {
		glUniform3f(id, a, b, c);
	}

	/**
	 * Sets a vec3f uniform. Suitable for x/y/z/w and r/g/b/a values.
	 * 
	 * @param name The name of the uniform.
	 * @param a The first float value.
	 * @param b The second float value.
	 * @param c The third float value.
	 * @param d The fourth float value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformVec4f(String name, float a, float b, float c, float d) throws LWJGLException {
		setUniformVec4f(getUniformID(name), a, b, c, d);
	}
	
	/**
	 * Sets a vec3f uniform. Suitable for x/y/z/w and r/g/b/a values.
	 * 
	 * @param id The id of the uniform.
	 * @param a The first float value.
	 * @param b The second float value.
	 * @param c The third float value.
	 * @param d The fourth float value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformVec4f(int id, float a, float b, float c, float d) throws LWJGLException {
		glUniform4f(id, a, b, c, d);
	}

	/**
	 * Sets an integer uniform.
	 * 
	 * @param name The name of the uniform.
	 * @param value The value to set.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformInt(String name, int value) throws LWJGLException {
		setUniformInt(getUniformID(name), value);
	}
	
	/**
	 * Sets an integer uniform.
	 * 
	 * @param id The id of the uniform.
	 * @param value The value to set.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformInt(int id, int value) throws LWJGLException {
		glUniform1i(id, value);
	}
	
	/**
	 * Sets a vec2i uniform. Suitable for x/y values.
	 * 
	 * @param name The name of the uniform.
	 * @param a The first integer value.
	 * @param b The second integer value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformVec2i(String name, int a, int b) throws LWJGLException {
		setUniformVec2i(getUniformID(name), a, b);
	}
	
	/**
	 * Sets a vec2i uniform. Suitable for x/y values.
	 * 
	 * @param id The id of the uniform.
	 * @param a The first integer value.
	 * @param b The second integer value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformVec2i(int id, int a, int b) throws LWJGLException {
		glUniform2i(id, a, b);
	}

	/**
	 * Sets a vec3i uniform. Suitable for x/y/z and r/g/b values.
	 * 
	 * @param name The name of the uniform.
	 * @param a The first integer value.
	 * @param b The second integer value.
	 * @param c The third integer value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformVec3i(String name, int a, int b, int c) throws LWJGLException {
		setUniformVec3i(getUniformID(name), a, b, c);
	}
	
	/**
	 * Sets a vec3i uniform. Suitable for x/y/z and r/g/b values.
	 * 
	 * @param id The id of the uniform.
	 * @param a The first integer value.
	 * @param b The second integer value.
	 * @param c The third integer value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformVec3i(int id, int a, int b, int c) throws LWJGLException {
		glUniform3i(id, a, b, c);
	}
	
	/**
	 * Sets a vec4i uniform. Suitable for x/y/z/w and r/g/b/a values.
	 * 
	 * @param name The name of the uniform.
	 * @param a The first integer value.
	 * @param b The second integer value.
	 * @param c The third integer value.
	 * @param d The fourth integer value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformVec4i(String name, int a, int b, int c, int d) throws LWJGLException {
		setUniformVec4i(getUniformID(name), a, b, c, d);
	}
	
	/**
	 * Sets a vec4i uniform. Suitable for x/y/z/w and r/g/b/a values.
	 * 
	 * @param id The id of the uniform.
	 * @param a The first integer value.
	 * @param b The second integer value.
	 * @param c The third integer value.
	 * @param d The fourth integer value.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformVec4i(int id, int a, int b, int c, int d) throws LWJGLException {
		glUniform4i(id, a, b, c, d);
	}
	
	/**
	 * Sets a mat3f uniform.
	 * 
	 * @param name The name of the uniform.
	 * @param m The 3x3 matrix.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformMat3f(String name, Matrix3f m) throws LWJGLException {
		setUniformMat3f(getUniformID(name), m);
	}
	
	/**
	 * Sets a mat3f uniform.
	 * 
	 * @param id The id of the uniform.
	 * @param m The 3x3 matrix.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformMat3f(int id, Matrix3f m) throws LWJGLException {
		floatBuffer.clear();
		m.store(floatBuffer);
		floatBuffer.flip();
		glUniformMatrix3(id, false, floatBuffer);		// uniformID, transpose, matrix buffer
	}
	
	/**
	 * Sets a mat4f uniform.
	 * 
	 * @param name The name of the uniform.
	 * @param m The 4x4 matrix.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	public void setUniformMat4f(String name, Matrix4f m) throws LWJGLException {
		setUniformMat4f(getUniformID(name), m);
	}
	
	/**
	 * Sets a mat4f uniform.
	 * 
	 * @param id The id of the uniform.
	 * @param m The 4x4 matrix.
	 * 
	 * @throws LWJGLException if the uniform does not exist.
	 */
	protected void setUniformMat4f(int id, Matrix4f m) throws LWJGLException {
		floatBuffer.clear();
		m.store(floatBuffer);
		floatBuffer.flip();
		glUniformMatrix4(id, false, floatBuffer);		// uniformID, transpose, matrix buffer
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Checks for whether or not shaders are supported.
	 * 
	 * @return Whether or not shaders are supported.
	 */
	private static boolean getShadersSupported() {
		ContextCapabilities c = GLContext.getCapabilities();
		return c.GL_ARB_shader_objects && c.GL_ARB_vertex_shader && c.GL_ARB_fragment_shader;
	}
	
	/**
	 * Gets a shader type as a string, for more readable output messages.
	 * 
	 * @param shaderType The shader type.
	 * 
	 * @return The shader type as a string.
	 */
	private static String getShaderTypeAsString(int shaderType) {
		if(shaderType == GL_VERTEX_SHADER)
			return "vertex";
		else if(shaderType == GL_FRAGMENT_SHADER)
			return "fragment";
		return "undefined";
	}
	
	/**
	 * Loads and compiles a shader.
	 * 
	 * @param shaderSrc The shader source.
	 * @param shaderType The shader type.
	 * 
	 * @return The shader ID.
	 * @throws LWJGLException if the shader could not be compiled.
	 * @see org.lwjgl.opengl.GL20#GL_VERTEX_SHADER
	 * @see org.lwjgl.opengl.GL20#GL_FRAGMENT_SHADER
	 */
	protected static int compileShader(CharSequence shaderSrc, int shaderType) throws LWJGLException {
		if(!shadersSupported)
			throw new LWJGLException("Shaders are not supported!");
		
		if(shaderType != GL_VERTEX_SHADER && shaderType != GL_FRAGMENT_SHADER)
			throw new LWJGLException("Invalid shader type!");
		
		int shader = glCreateShader(shaderType);
		if(shader == 0)
			throw new LWJGLException("Could not create " + getShaderTypeAsString(shaderType) + " shader!");
		
		glShaderSource(shader, shaderSrc);
		glCompileShader(shader);
		
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
			throw new LWJGLException("Could not compile " + getShaderTypeAsString(shaderType) + " shader: " + glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH)));
		
		return shader;
	}
	
	/**
	 * Gets the default vertex shader, and compiles it if it does not exist.
	 * 
	 * @return The ID of the default vertex shader.
	 */
	private static int getDefaultVertexShader() {
		if(DEFAULT_VERTEX_SHADER != 0)
			return DEFAULT_VERTEX_SHADER;
		
		int shader;
		try {
			shader = compileShader(DEFAULT_VERTEX_SHADER_SRC, GL_VERTEX_SHADER);
		} catch(LWJGLException e) {
			Log.critical("Could not compile default vertex shader!", e);
			return 0;
		}
		
		return DEFAULT_VERTEX_SHADER = shader;
	}
	
	/**
	 * Gets the default fragment shader, and compiles it if it does not exist.
	 * 
	 * @return The ID of the default fragment shader.
	 */
	private static int getDefaultFragmentShader() {
		if(DEFAULT_FRAGMENT_SHADER != 0)
			return DEFAULT_FRAGMENT_SHADER;
		
		int shader;
		try {
			shader = compileShader(DEFAULT_FRAGMENT_SHADER_SRC, GL_FRAGMENT_SHADER);
		} catch(LWJGLException e) {
			Log.critical("Could not compile default fragment shader!", e);
			return 0;
		}
		
		return DEFAULT_FRAGMENT_SHADER = shader;
	}
	
	/**
	 * Gets the default shader program.
	 * 
	 * @return The default shader program.
	 */
	public static ShaderProgram getDefaultShaderProgram() {
		if(DEFAULT_SHADER_PROGRAM != null)
			return DEFAULT_SHADER_PROGRAM;
		
		try {
			DEFAULT_SHADER_PROGRAM = new ShaderProgram(
					getDefaultVertexShader(),
					getDefaultFragmentShader(),
					SPRITE_ATTRIBUTES
			);
			DEFAULT_SHADER_PROGRAM.unloadable = false;
		} catch(LWJGLException e) {
			Log.critical("Could not compile default shader program: " + e.getMessage());
			return null;
		}
		
		return DEFAULT_SHADER_PROGRAM;
	}
	
}