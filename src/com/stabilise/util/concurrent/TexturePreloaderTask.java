package com.stabilise.util.concurrent;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;

import com.stabilise.core.Resources;
import com.stabilise.opengl.Texture;
import com.stabilise.util.Log;

/**
 * A TexturePreloaderTask is a task designed to pre-load texture resources in a
 * separate thread before they are uploaded to OpenGL on the main thread.
 * 
 * <p>A TexturePreloaderTask operates as per any other {@code Task} - however,
 * for it to be of any use, {@link #uploadTextures()} should be invoked once
 * the task completes.
 * 
 * <p>Unlike {@link GLLoadTask}, this class leaves the duty of uploading the
 * texture data to OpenGL for the main thread, as, in some systems, context
 * sharing between threads is not supported but may not necessarily fail-fast
 * which is problematic behaviour.
 */
public class TexturePreloaderTask extends Task {
	
	/** The textures. */
	private List<PreloadedTexture> textures = new ArrayList<PreloadedTexture>();
	
	
	/**
	 * Creates a new TexturePreloaderTask.
	 */
	public TexturePreloaderTask() {
		super();
	}
	
	/**
	 * Creates a new TexturePreloaderTask.
	 * 
	 * @param parts The number of parts in the task.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code parts < 0}.
	 */
	public TexturePreloaderTask(int parts) {
		super(parts);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>A subclass of {@code TexturePreloaderTask} which overrides this
	 * method should remember to invoke {@code super.execute()}.
	 */
	@Override
	protected void execute() throws Exception {
		for(PreloadedTexture t : textures) {
			checkCancel();
			t.load();
			tracker.increment();
		}
	}
	
	/**
	 * Instructs the task to load textures. This may be called multiple times
	 * to append more textures before the task has started.
	 * 
	 * @param textures The names of the textures to load, as if by
	 * {@link Texture#loadTexture(String)}.
	 * 
	 * @return The TexturePreloaderTask, for chaining operations.
	 * @throws IllegalStateException Thrown if the task has been started.
	 * @throws NullPointerException Thrown if {@code textures} or any of its
	 * elements is {@code null}.
	 */
	public TexturePreloaderTask loadTextures(String[] textures) {
		if(!getState().equals(TaskState.UNSTARTED))
			throw new IllegalStateException("Texture preloader task already running; cannot assign more textures!");
		
		for(String t : textures)
			this.textures.add(new PreloadedTexture(t));
		
		tracker.addParts(textures.length);
		
		return this;
	}
	
	/**
	 * Uploads the textures which were loaded by this task to OpenGL so that
	 * they may be used. This should only be invoked on the <i>main thread</i>
	 * (i.e. the one which holds the GL context), and only once this task has
	 * completed.
	 * 
	 * <p>The textures are uploaded as per
	 * {@link Texture#loadTexture(String, ByteBuffer, int, int)}.
	 * 
	 * @throws IllegalStateException Thrown if the task has not started or is
	 * still running.
	 */
	public void uploadTextures() {
		if(!stopped())
			throw new IllegalStateException("Task has not yet run!");
		for(PreloadedTexture t : textures)
			t.uploadToGL();
	}
	
	// ----------Nested class----------
	
	/**
	 * Stores texture data.
	 */
	private static class PreloadedTexture {
		
		private final String name;
		ByteBuffer imgData;
		int width, height;
		private boolean loaded = false;
		
		/**
		 * Creates a new PreloadedTexture.
		 */
		private PreloadedTexture(String name) {
			if(name == null)
				throw new NullPointerException("Texture is null!");
			this.name = name;
		}
		
		/**
		 * Loads the texture data, but doesn't upload it to OpenGL memory.
		 */
		private void load() {
			try {
				BufferedImage img = Resources.loadImageFromFileSystem(name);
				//imgData = BufferUtil.imageToByteBuffer(img);
				width = img.getWidth();
				height = img.getHeight();
				loaded = true;
			} catch(IOException ignored) {}
		}
		
		/**
		 * Uploads the texture data to GL memory. This should be done on the
		 * main thread.
		 */
		private void uploadToGL() {
			if(loaded) {
				try {
					Texture.loadTexture(name, imgData, width, height);
				} catch(LWJGLException e) {
					Log.throwable(e);
				}
			}
		}
	}
	
}
