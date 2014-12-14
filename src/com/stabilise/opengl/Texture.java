package com.stabilise.opengl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.LWJGLException;

import com.stabilise.core.Resources;
import com.stabilise.util.annotation.LWJGLReliant;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;

/**
 * A Texture is a raw OpenGL image.
 * 
 * <p>This class is only thread-safe in the sense that loaded textures will be
 * visible to other threads; however, most operations (e.g. loading a texture)
 * are non-atomic, and hence it is possible for multiple threads to attempt to
 * load a supposedly-absent texture concurrently. This due to the fact that
 * there is no need for synchronisation in my current implementation and usage
 * of textures; if such an eventuality occurs that it will be required, I will
 * add it.
 */
@LWJGLReliant
public class Texture {
	
	//--------------------==========--------------------
	//------=====Static Constants & Variables=====------
	//--------------------==========--------------------
	
	// Some filters, included here for convenience
	public static final int LINEAR = GL_LINEAR;
	public static final int NEAREST = GL_NEAREST;
	public static final int LINEAR_MIPMAP_LINEAR = GL_LINEAR_MIPMAP_LINEAR;
	public static final int LINEAR_MIPMAP_NEAREST = GL_LINEAR_MIPMAP_NEAREST;
	public static final int NEAREST_MIPMAP_NEAREST = GL_NEAREST_MIPMAP_NEAREST;
	public static final int NEAREST_MIPMAP_LINEAR = GL_NEAREST_MIPMAP_LINEAR;
	
	// Some wrap modes, included here for convenience
	public static final int CLAMP = GL_CLAMP;
	public static final int CLAMP_TO_EDGE = GL_CLAMP_TO_EDGE;
	public static final int REPEAT = GL_REPEAT;
	
	public static final int DEFAULT_FILTER = LINEAR;
	public static final int DEFAULT_WRAP = REPEAT;
	
	
	/** The map of all loaded textures. */
	private static Map<String, Texture> textures = new ConcurrentHashMap<String, Texture>();
	
	/** The ID of the currently bound texture. */
	private static int boundTexture = -1;
	
	/** The texture log. */
	//private static Log log = Log.getAgent("TEXTURES").mute();
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** The source of this texture - used as its name. */
	private final String name;
	/** The internal GL ID for this texture. */
	private final int id;
	/** The list of objects with which the texture is marked. If it is
	 * requested that all textures with a specified marker are to be unloaded,
	 * any textures also anchored by other markers will not be unloaded. */
	private List<Object> markers = Collections.synchronizedList(new ArrayList<Object>(4));
	
	/** The texture's width */
	private int width;
	/** The texture's height */
	private int height;
	/** Whether or not the texture has an alpha. */
	//public boolean alpha;
	
	
	/**
	 * Creates a new Texture.
	 * 
	 * @param name The texture's name.
	 * @param id The texture's OpenGL ID.
	 */
	private Texture(String name, int id) {
		this.name = name;
		this.id = id;
	}
	
	/**
	 * Creates a new Texture.
	 * 
	 * @param name The texture's name.
	 * @param id The texture's OpenGL ID.
	 * @param marker The texture's default marker.
	 */
	private Texture(String name, int id, Object marker) {
		this(name, id);
		addMarker(marker);
	}
	
	/**
	 * Creates a new Texture.
	 * 
	 * @param texture The texture to duplicate.
	 */
	/*
	private Texture(Texture texture) {
		name = texture.name;
		id = texture.id;
		markers = texture.markers;
		width = texture.width;
		height = texture.height;
	}
	*/
	
	/**
	 * Applies a filter to the texture.
	 * 
	 * @param filter The filter to use.
	 */
	public void filter(int filter) {
		filter(filter, filter);
	}
	
	/**
	 * Applies a filter to the texture.
	 * See OpenGL specifications for more information.
	 * 
	 * @param minFilter The min filter.
	 * @param magFilter The mag filter.
	 */
	public void filter(int minFilter, int magFilter) {
		bind();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
		
		if((minFilter != NEAREST && minFilter != LINEAR) || (magFilter != NEAREST && magFilter != LINEAR)) {
			glEnable(GL_TEXTURE_2D);
			//ARBTextureStorage.glTexStorage2D(GL_TEXTURE_2D, 4, GL_RGBA, width, height);
			//glGenerateMipmap(GL_TEXTURE_2D);
			glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
		}
	}
	
	/**
	 * Sets the texture wrap.
	 * 
	 * @param The wrap.
	 */
	public void wrap(int wrap) {
		bind();
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
	}
	
	/**
	 * Sets the texture's unpack alignment.
	 */
	private void setUnpackAlignment() {
		// TODO: why
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
	}
	
	/**
	 * Binds the texture to OpenGL so that operations may be performed on it.
	 */
	public void bind() {
		if(boundTexture != id) {
			boundTexture = id;
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, id);
		}
	}
	
	/**
	 * Marks a texture. If {@code marker} is {@code null}, this method will not
	 * throw an exception, but return immediately.
	 * 
	 * @param marker The marker to add to the texture.
	 * 
	 * @return The texture, for chain construction.
	 */
	public Texture addMarker(Object marker) {
		if(marker != null) {
			synchronized(markers) {
				if(!markers.contains(marker))
					markers.add(marker);
			}
		}
		return this;
	}
	
	/**
	 * Checks for whether or not the texture possesses a specified marker.
	 * 
	 * @param marker The marker.
	 * 
	 * @return {@code true} if the texture possesses the marker;
	 * {@code false} if not.
	 */
	public boolean hasMarker(Object marker) {
		synchronized(markers) {
			return markers.contains(marker);
		}
	}
	
	/**
	 * Removes a specified marker from the texture, if it possesses the marker.
	 * 
	 * @param marker The marker to remove from the texture.
	 * 
	 * @return {@code true} if the texture previously possessed the marker, and
	 * lacks any others; {@code false} otherwise.
	 */
	public boolean removeMarker(Object marker) {
		synchronized(markers) {
			return markers.remove(marker) && markers.size() == 0;
		}
	}
	
	/**
	 * Attempts to unload the texture; the texture will only be unloaded if it
	 * possesses the specified marker, and lacks any others. If the texture
	 * possesses the marker and is not unloaded, it will be unmarked.
	 * 
	 * @param marker The marker.
	 * 
	 * @return {@code true} if the texture was unloaded; {@code false} if not.
	 */
	private boolean unload(Object marker) {
		if(removeMarker(marker)) {
			unload();
			return true;
		}
		return false;
	}
	
	/**
	 * Unloads the texture data from OpenGL memory. All instances of this
	 * texture should be disposed of after this happens.
	 */
	private void unload() {
		synchronized(markers) {
			markers.clear();
		}
		if(boundTexture == id)
			boundTexture = -1;
		glDeleteTextures(id);
	}
	
	//--------------------==========--------------------
	//-----------------=====Getters=====----------------
	//--------------------==========--------------------
	
	/**
	 * Gets the texture's name.
	 * 
	 * @return The texture's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the texture's width.
	 * 
	 * @return The texture's width, in pixels.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the texture's height.
	 * 
	 * @return The texture's height, in pixels.
	 */
	public int getHeight() {
		return height;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Texture:[\"");
		sb.append(name);
		sb.append("\";");
		sb.append(id);
		sb.append(": ");
		
		if(markers.size() == 0) {
			sb.append("---");
		} else {
			int len = markers.size();
			for(Object o : markers) {
				sb.append(o.toString());
				if(--len > 0)
					sb.append(", ");
			}
		}
		
		sb.append(']');
		
		return sb.toString();
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Checks for whether or not a texture currently exists - that is, has been
	 * loaded into memory.
	 * 
	 * @param name The name of the texture. This should be equivalent to the
	 * path of the texture's source image, relative to the application's
	 * {@link Resources#IMAGE_DIR image resource folder}.
	 * 
	 * @return {@code true} if the texture exists; {@code false} otherwise.
	 */
	public static boolean textureExists(String name) {
		return textures.containsKey(name);
	}
	
	/**
	 * Gets a texture.
	 * 
	 * @param name The name of the texture. This should be equivalent to the
	 * path of the texture's source image, relative to the application's
	 * {@link Resources#IMAGE_DIR image resource folder}.
	 * 
	 * @return The texture, or {@code null} if a texture by the given name does
	 * not exist.
	 */
	public static Texture getTexture(String name) {
		return textures.get(name);
	}
	
	/**
	 * Loads a texture into memory and returns it. If the texture has already
	 * been loaded, it will be returned.
	 * 
	 * @param name The name of the texture. This should be equivalent to the
	 * path of the texture's source image, relative to the application's
	 * {@link Resources#IMAGE_DIR image resource folder}.
	 * 
	 * @return The {@code Texture}.
	 * 
	 * @throws IOException if the texture's source image does not exist, or
	 * could not be loaded.
	 * @throws LWJGLException if the image's dimensions exceed the maximum
	 * allowed by the hardware.
	 */
	public static Texture loadTexture(String name) throws IOException, LWJGLException {
		// TODO: Non-atomic
		if(textureExists(name))
			return getTexture(name);
		
		return loadTexture(name, Resources.loadImageFromFileSystem(name));
	}
	
	/**
	 * Loads a texture into memory and returns it.
	 * 
	 * <p>This method does not check as to whether or not a texture with the
	 * given name already exists; use with caution.
	 * 
	 * @param name The name of the texture. This should be equivalent to the
	 * path of the texture's source image, relative to the application's
	 * {@link Resources#IMAGE_DIR image resource folder}.
	 * @param image The texture's source image.
	 * 
	 * @return The {@code Texture}.
	 * 
	 * @throws LWJGLException if the image's dimensions exceed the maximum
	 * allowed by the hardware.
	 */
	public static Texture loadTexture(String name, BufferedImage image) throws LWJGLException {
		//boolean hasAlpha = (image.getRGB(0,0) >> 24) == 0x00;
		return loadTexture(name, null/*BufferUtil.imageToByteBuffer(image)*/, image.getWidth(), image.getHeight());
	}
	
	/**
	 * Loads a texture into memory and returns it.
	 * 
	 * <p> This method does not check as to whether or not a texture with the 
	 * given name already exists; use with caution.
	 * 
	 * @param name The name of the texture. This should be equivalent to the
	 * path of the texture's source image, relative to the application's
	 * {@link Resources#IMAGE_DIR image resource folder}.
	 * @param imageBytes The bytes which constitute the image.
	 * @param width The width of the image, in pixels.
	 * @param height The height of the image, in pixels.
	 * 
	 * @return The {@code Texture}.
	 * 
	 * @throws LWJGLException if the image's dimensions exceed the maximum
	 * allowed by the hardware.
	 */
	public static Texture loadTexture(String name, ByteBuffer imageBytes, int width, int height) throws LWJGLException {
		// Get the texture's OpenGL ID
		int id = glGenTextures();
		
		Texture texture = new Texture(name, id);
		
		texture.width = width;
		texture.height = height;

		int max = glGetInteger(GL_MAX_TEXTURE_SIZE);
		if(texture.width > max || texture.height > max)
			throw new LWJGLException("The loaded texture is too big for the current hardware");
		
		texture.bind();
		
		// Filter the texture
		texture.filter(DEFAULT_FILTER);
		// Wrap the texture
		texture.wrap(DEFAULT_WRAP);
		
		texture.setUnpackAlignment();
		
		//glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		//gluBuild2DMipmaps(GL_TEXTURE_2D, 0, texture.width, texture.height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		// Produce the texture
		glTexImage2D(GL_TEXTURE_2D,
				0,
				GL_RGBA,
				texture.width,
				texture.height,
				0,
				GL_RGBA,
				GL_UNSIGNED_BYTE,
				imageBytes);
		
		textures.put(name, texture);
		
		//log.logMessage("Loaded texture \"" + name + "\"");
		
		return texture;
	}
	
	/**
	 * Unloads a texture.
	 * 
	 * @param texture The texture.
	 * 
	 * @throws NullPointerException if {@code texture} is {@code null}.
	 */
	public static void unloadTexture(Texture texture) {
		unloadTexture(texture.name);
	}
	
	/**
	 * Unloads a texture.
	 * 
	 * @param name The name of the texture.
	 */
	public static void unloadTexture(String name) {
		// TODO: Non-atomic
		if(textures.containsKey(name)) {
			textures.get(name).unload();
			textures.remove(name);
			//log.logMessage("Unloaded texture \"" + name + "\"");
		} else {
			//log.logCritical("Unable to unload texture \"" + name + "\" - texture does not exist.");
		}
	}
	
	/**
	 * Removes a marker from a specified texture, and unloads said texture if
	 * it lacks any other markers.
	 * 
	 * @param name The name of the texture.
	 * @param marker The marker to remove from the texture.
	 */
	public static void unloadTexture(String name, Object marker) {
		// TODO: non-atomic
		if(textures.containsKey(name) && textures.get(name).unload(marker))
			textures.remove(name);
	}
	
	/**
	 * Unloads all textures from memory.
	 */
	public static void unloadTextures() {
		Iterator<Entry<String, Texture>> i = textures.entrySet().iterator();
		while(i.hasNext()) {
			i.next().getValue().unload();
			i.remove();
		}
	}
	
	/**
	 * Unloads all textures with solely the given marker from memory.
	 * 
	 * @param marker The marker.
	 */
	public static void unloadTextures(Object marker) {
		Iterator<Entry<String, Texture>> i = textures.entrySet().iterator();
		while(i.hasNext()) {
			if(i.next().getValue().unload(marker))
				i.remove();
		}
	}
	
	/**
	 * Gets the list of all loaded textures in string form.
	 * 
	 * @return A string containing all loaded textures.
	 */
	public static String texturesToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Textures:[");
		Iterator<Entry<String, Texture>> i = textures.entrySet().iterator();
		while(i.hasNext()) {
			sb.append("\n    ");
			sb.append(i.next().getValue().toString());
		}
		sb.append("\n]");
		return sb.toString();
	}
	
}
