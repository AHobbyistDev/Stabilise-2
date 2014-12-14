package com.stabilise.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Drawable;
import org.lwjgl.opengl.SharedDrawable;

import com.stabilise.util.annotation.UserThread;

/**
 * A SharedContext is a device which provides a way of sharing the OpenGL
 * context between threads, which allows separate threads to perform GL
 * operations. Depending on the system in which the application is running,
 * however, sharing context may not be allowed, and synchronous contingencies
 * should generally be made if GL operations can not be performed concurrently.
 * 
 * <p>A SharedContext object may be used by multiple threads.
 * 
 * <p>
 * A SharedContext should typically be used as follows:
 * 
 * <pre>
 * SharedContext context = new SharedContext();
 * ...
 * // On another thread:
 * if(context.share()) {
 *     // do something
 *     context.release();
 * } else {
 *     // tell the main thread it needs to do this task instead
 * }
 * </pre>
 * 
 * <!-- TODO: On some systems (e.g. DET laptop) textures load fine, but the JVM
 * crashes when they're unloaded on the main thread. Is there a way to avoid
 * this? -->
 * 
 * @deprecated An incompatible system (i.e. one which may not support shared
 * contexts) may not necessarily fail-fast, thus making this unsuitable for
 * general use.
 */
public final class SharedContext {
	
	/** The current GL drawable object. */
	private Drawable drawable;
	/** Whether or not the drawable and its context were successfully set. */
	private boolean drawableSet;
	
	
	/**
	 * Creates a new SharedContext.
	 */
	@UserThread("SomeThread")
	public SharedContext() {
		try {
			drawable = new SharedDrawable(Display.getDrawable());
			drawableSet = true;
		} catch(LWJGLException e) {
			drawableSet = false;
			//Log.critical("Could not create a shared drawable!", e);
		}
	}
	
	/**
	 * Attempts to obtain the GL context for the current thread. Calls to GL
	 * operations on this thread should only be performed if this method
	 * returns {@code true}.
	 * 
	 * @return {@code true} if the context was successfully shared;
	 * {@code false} otherwise.
	 */
	@UserThread("SomeOtherThread")
	public boolean obtain() {
		if(drawableSet) {
			try {
				drawable.makeCurrent();
				return true;
			} catch(LWJGLException e) {
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Releases the GL context from this thread. Calls to GL operations should
	 * no longer be performed after this method is invoked.
	 */
	@UserThread("SomeOtherThread")
	public void release() {
		if(drawableSet) {
			try {
				drawable.releaseContext();
			} catch(LWJGLException ignored) {}
		}
	}
	
}
