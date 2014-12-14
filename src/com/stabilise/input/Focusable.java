package com.stabilise.input;

/**
 * Defines the methods required by any component capable of being focused on
 * and directly receiving user input.
 * 
 * @see InputManager
 */
public interface Focusable {
	
	/**
	 * Handles the press of a mouse button.
	 * 
	 * <p>This is invoked by the InputManager when a mouse button press event
	 * is detected.
	 * 
	 * @param button The ID of the button pressed. A value of {@code 0} refers
	 * to the left mouse button, a value of {@code 1} refers to the right
	 * mouse button, and a value of {@code 2} refers to the middle mouse
	 * button.
	 * @param x The x-coordinate of the mouse when the button was pressed, in
	 * pixels, such that the bottom-left corner of the screen is (0,0).
	 * @param y The y-coordinate at the mouse when the button was pressed, in
	 * pixels, such that the bottom-left corner of the screen is (0,0).
	 */
	void handleButtonPress(int button, int x, int y);
	
	/**
	 * Handles the release of a mouse button.
	 * 
	 * <p>This is invoked by the InputManager when a mouse button release event
	 * is detected.
	 * 
	 * @param button The ID of the button released. A value of {@code 0}
	 * refers to the left mouse button, a value of {@code 1} refers to the
	 * right mouse button, and a value of {@code 2} refers to the middle mouse
	 * button.
	 * @param x The x-coordinate of the mouse when the button was released, in
	 * pixels, such that the bottom-left corner of the screen is (0,0).
	 * @param y The y-coordinate at the mouse when the button was released, in
	 * pixels, such that the bottom-left corner of the screen is (0,0).
	 */
	void handleButtonRelease(int button, int x, int y);
	
	/**
	 * Handles the press of a key.
	 * 
	 * <p>This is invoked by the InputManager when a keyboard key press event
	 * is detected.
	 * 
	 * @param key The ID of the key pressed. Refer to
	 * {@link org.lwjgl.input.Keyboard Keyboard} for keycodes.
	 */
	void handleKeyPress(int key);
	
	/**
	 * Handles the release of a key.
	 * 
	 * <p>This is invoked by the InputManager when a keyboard key release event
	 * is detected.
	 * 
	 * @param key The ID of the key released. Refer to
	 * {@link org.lwjgl.input.Keyboard Keyboard} for keycodes.
	 */
	void handleKeyRelease(int key);
	
	/**
	 * Handles the movement of the mouse cursor.
	 * 
	 * <p>This is invoked by the InputManager when the mouse cursor is
	 * translated over the screen.
	 * 
	 * @param dx The change in the cursor's x-coordinate, in pixels, where a
	 * positive value indicates the mouse moved right, and a negative value
	 * indicates that the mouse moved left.
	 * @param dy The change in the cursor's y-coordinate, in pixels, where a
	 * positive value indicates the mouse moved up, and a negative value
	 * indicates that the mouse moved down.
	 */
	void handleMouseMove(int dx, int dy);
	
	/**
	 * Handles the scroll of the mouse wheel.
	 * 
	 * <p>This is invoked by the InputManager when the mouse wheel is scrolled.
	 * 
	 * @param scroll The quantitative value of the scroll.
	 */
	void handleMouseWheelScroll(int scroll);
}
