package com.stabilise.opengl.render;

/**
 * A renderer is a mechanism which performs certain rendering operations.
 */
public interface Renderer {
	
	/**
	 * Updates the renderer.
	 */
	void update();
	
	/**
	 * Renders to the screen.
	 */
	void render();
	
	/**
	 * Loads the renderer's resources.
	 */
	void loadResources();
	
	/**
	 * Unloads the renderer's resources.
	 */
	void unloadResources();

}
