package com.stabilise.input;

/**
 * Defines the methods required by any component capable of receiving
 * configurable input from a Controller.
 * 
 * @see Controller
 */
public interface Controllable {
	
	/**
	 * Handles the press of a mouse button.
	 * 
	 * <p>This is invoked by a Controller when a mouse button press event is
	 * detected.
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
	 * <p>This is invoked by a Controller when a mouse button release event is
	 * detected.
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
	 * Handles the press of a key. Note, however, that it is generally
	 * preferable to respond to registered controls through {@link
	 * #handleControlPress(com.stabilise.input.Controller.Control)
	 * handleControlPress} instead.
	 * 
	 * <p>This is invoked by the Controller when a keyboard key press event
	 * is detected.
	 * 
	 * @param key The ID of the key pressed. Refer to
	 * {@link org.lwjgl.input.Keyboard Keyboard} for keycodes.
	 */
	void handleKeyPress(int key);
	
	/**
	 * Handles the release of a key. Note, however, that it is generally
	 * preferable to respond to registered controls through {@link
	 * #handleControlRelease(com.stabilise.input.Controller.Control)
	 * handleControlRelease} instead.
	 * 
	 * <p>This is invoked by the Controller when a keyboard key release event
	 * is detected.
	 * 
	 * @param key The ID of the key released. Refer to
	 * {@link org.lwjgl.input.Keyboard Keyboard} for keycodes.
	 */
	void handleKeyRelease(int key);
	
	/**
	 * Handles the press of a control.
	 * 
	 * <p>This is invoked by a Controller when a control press is detected.
	 * 
	 * @param control The control pressed.
	 */
	void handleControlPress(Controller.Control control);
	
	/**
	 * Handles the release of a control.
	 * 
	 * <p>This is invoked by a Controller when a control release is detected.
	 * 
	 * @param control The control released.
	 */
	void handleControlRelease(Controller.Control control);
	
	/**
	 * Handles the scroll of the mouse wheel.
	 * 
	 * <p>This is invoked by a Controller when the mouse wheel is scrolled.
	 * 
	 * @param scroll The quantitative value of the scroll.
	 */
	void handleMouseWheelScroll(int scroll);
	
}
