package com.stabilise.core.state;

/**
 * States allow for the circumstantial execution of the application's logic.
 * 
 * <p>
 * The current application state determines the manner of logic to be executed.
 * </p>
 */
public interface State {
	
	/**
	 * Called when the state is started.
	 * 
	 * <p>If there is an old state, this method is invoked before {@link
	 * #dispose()} is invoked on the old state.
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
	 * 
	 * <p>This is also invoked before {@link #dispose()} when the application
	 * is shut down.
	 */
	void pause();
	
	/**
	 * Called when the application is resumed.
	 */
	void resume();
	
	/**
	 * Called when the application is resized. This is also called when a state
	 * is set as the application's current state, immediately after {@link
	 * #start()}.
	 * 
	 * @param width The new application width, in pixels.
	 * @param height The new application height, in pixels.
	 */
	void resize(int width, int height);
	
	/**
	 * Called when the state should update.
	 */
	void update();
	
	/**
	 * Called when the state should render.
	 * 
	 * @param delta The time since the last render, in seconds.
	 */
	void render(float delta);
	
}
