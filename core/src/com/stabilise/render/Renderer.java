package com.stabilise.render;

/**
 * A renderer is a mechanism which performs certain rendering operations.
 */
public interface Renderer {
    
    /**
     * Loads the renderer's resources.
     */
    void loadResources();
    
    /**
     * Unloads the renderer's resources.
     */
    void unloadResources();
    
    /**
     * Invoked when the screen is resized.
     * 
     * @param width The width of the screen, in pixels.
     * @param height The height of the screen, in pixels.
     */
    void resize(int width, int height);
    
    /**
     * Updates the renderer.
     */
    void update();
    
    /**
     * Renders to the screen.
     */
    void render();
    
}
