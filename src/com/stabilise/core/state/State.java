package com.stabilise.core.state;

import com.stabilise.core.Application;
import com.stabilise.util.annotation.UsedByApplication;
import com.stabilise.util.annotation.UsesApplication;

/**
 * States allow for the circumstantial execution of the application's logic.
 * 
 * <p>
 * The current application state determines the manner of logic to be executed.
 * </p>
 */
@UsedByApplication
@UsesApplication
public interface State {
	
	/**
	 * Called when the state is started.
	 */
	void start();
	
	/**
	 * Called when the state is stopped and disposed.
	 * 
	 * <p>Any used resources should be appropriately disposed of and any
	 * necessary cleanups should be performed here. This operation should
	 * ideally be performed swiftly.
	 */
	void dispose();
	
	/**
	 * Called when the application is paused.
	 */
	void pause();
	
	/**
	 * Called when the application is resumed.
	 */
	void resume();
	
	/**
	 * Called when the application is resized.
	 * 
	 * @param width The new application width, in pixels.
	 * @param height The new application height, in pixels.
	 */
	void resize(int width, int height);
	
	/**
	 * Updates the state. If this state is the application's current state,
	 * this method will be invoked a number of times per second equivalent to
	 * {@link Application#ticksPerSecond}.
	 * 
	 * <p>Implementors of {@code State} should make appropriate calls to the
	 * update methods of objects here.
	 */
	void update();
	
	/**
	 * Renders the state. If this state is the application's current state,
	 * this method will be invoked as many times per second as the system will
	 * allow, or as many times equivalent to the FPS cap, whichever is smaller.
	 * 
	 * <p>Implementors of {@code State} should perform appropriate rendering
	 * operations here.
	 */
	void render();
	
}
