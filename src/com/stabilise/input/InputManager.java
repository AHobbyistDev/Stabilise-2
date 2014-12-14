package com.stabilise.input;

import java.util.Arrays;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.LWJGLReliant;

/**
 * Manages keyboard and mouse input events so that other classes have a
 * convenient way of detecting user input.
 */
@LWJGLReliant
public class InputManager {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The number of ticks within which a button must be pressed successively
	 * to be considered a double-click.
	 * TODO: Unused */
	@SuppressWarnings("unused")
	private static final int DOUBLE_CLICK_SENSITIVITY = 20;
	/** The number of ticks within which a key must be pressed successively to
	 * be considered a double-tap.
	 * TODO: Unused */
	@SuppressWarnings("unused")
	private static final int DOUBLE_TAP_SENSITIVITY = 20;
	
	/** The InputManager instance. */
	private static final InputManager INSTANCE = new InputManager();
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The component to post mouse and key events to. */
	private Focusable focus;
	
	/** True if the focus should receive mouse event notifications. */
	private boolean mouseEnabled = true;
	/** True if the focus should receive keyboard event notifications. */
	private boolean keyboardEnabled = true;
	
	/** Stores the state of each mouse button. */
	private boolean[] buttonStates = new boolean[3];
	/** Stores the state of each key. */
	private boolean[] keyStates = new boolean[256];
	
	/** True if the operating system being used is a mac - this is because macs
	 * have different keycodes for the ctrl (or command) key. */
	private boolean isMac;
	
	/** The input manager's logging agent. */
	private Log log = Log.getAgent("INPUT");
	
	
	/**
	 * Creates new InputManager.
	 */
	private InputManager() {
		// Initialise all values in the buttonStates and keyStates arrays as false
		Arrays.fill(buttonStates, false);
		Arrays.fill(keyStates, false);
		
		// For interpretation of mac inputs
		isMac = System.getProperty("os.name").toLowerCase().contains("mac");
	}
	
	/**
	 * Sets the focus of the InputManager.
	 * 
	 * @param focus The new focus.
	 */
	public void setFocus(Focusable focus) {
		setFocus(focus, true);
	}
	
	/**
	 * Sets the focus of the InputManager.
	 * 
	 * @param focus The new focus.
	 * @param postInputReleaseToPreviousFocus Whether or not the previous focus
	 * should be sent release events for every button and key currently down.
	 */
	public void setFocus(Focusable focus, boolean postInputReleaseToPreviousFocus) {
		if(postInputReleaseToPreviousFocus && this.focus != null) {
			for(int i = 0; i < keyStates.length; i++) {
				if(keyStates[i] && keyboardEnabled)
					this.focus.handleKeyRelease(i);
			}
			for(int ii = 0; ii < buttonStates.length; ii++) {
				if(buttonStates[ii] && mouseEnabled)
					this.focus.handleButtonRelease(ii, Mouse.getX(), Mouse.getY());
			}
		}
		
		this.focus = focus;
	}
	
	/**
	 * Updates the input manager; executes any queued input events.
	 */
	public void update() {
		while(Mouse.next()) {
			if(Mouse.getEventButton() != -1) {
				buttonStates[Mouse.getEventButton()] = Mouse.getEventButtonState();
							
				if(mouseEnabled && focus != null) {
					if(Mouse.getEventButtonState())
						focus.handleButtonPress(Mouse.getEventButton(), Mouse.getEventX(), Mouse.getEventY());
					else
						focus.handleButtonRelease(Mouse.getEventButton(), Mouse.getEventX(), Mouse.getEventY());
				}
			} else if(mouseEnabled && focus != null) {
				if(Mouse.getEventDX() != 0 || Mouse.getEventDY() != 0)
					focus.handleMouseMove(Mouse.getEventDX(), Mouse.getEventDY());
				else if(Mouse.getEventDWheel() != 0)
					focus.handleMouseWheelScroll(Mouse.getEventDWheel());
			}
		}
		
		while(Keyboard.next()) {
			keyStates[Keyboard.getEventKey()] = Keyboard.getEventKeyState();
			
			if(keyboardEnabled && focus != null) {
				if(Keyboard.getEventKeyState()) 
					focus.handleKeyPress(Keyboard.getEventKey());
				else
					focus.handleKeyRelease(Keyboard.getEventKey());
			}
		}
	}
	
	/**
	 * Enables the posting of all input events, and allows retrieval of all
	 * input-related values.
	 */
	public void enableAllInputs() {
		enableMouseInput();
		enableKeyboardInput();
	}
	
	/**
	 * Disables the posting of all input events, and disallows retrieval of all
	 * input-related values.
	 */
	public void disableAllInputs() {
		disableMouseInput();
		disableKeyboardInput();
	}
	
	/**
	 * Enables the posting of mouse-related input events, and allows retrieval
	 * of button states and other mouse-related values.
	 */
	public void enableMouseInput() {
		mouseEnabled = true;
	}
	
	/**
	 * Disables the posting of mouse-related input events, and disallows
	 * retrieval of button states and other mouse-related values.
	 */
	public void disableMouseInput() {
		unpressButtons();
		mouseEnabled = false;
	}
	
	/**
	 * Enables the posting of keyboard-related input events, and allows
	 * retrieval of key states.
	 */
	public void enableKeyboardInput() {
		keyboardEnabled = true;
	}
	
	/**
	 * Disables the posting of keyboard-related input events, and disables
	 * retrieval of key values.
	 */
	public void disableKeyboardInput() {
		unpressKeys();
		keyboardEnabled = false;
	}
	
	/**
	 * Forcefully registers all buttons as unpressed and informs the focus.
	 */
	public void unpressButtons() {
		for(int i = 0; i < buttonStates.length; i++) {
			if(buttonStates[i] && mouseEnabled && focus != null)
				focus.handleButtonRelease(i, Mouse.getX(), Mouse.getY());
			buttonStates[i] = false;
		}
	}
	
	/**
	 * Forcefully registers all keys as unpressed and informs the focus.
	  */
	public void unpressKeys() {
		for(int i = 0; i < keyStates.length; i++) {
			if(keyStates[i] && keyboardEnabled && focus != null)
				focus.handleKeyRelease(i);
			keyStates[i] = false;
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Getter Wrappers=====-------------
	//--------------------==========--------------------
	
	/** 
	 * Gets the x-coordinate of the mouse. Note that the bottom-left corner of
	 * the screen is treated as (0,0).
	 * 
	 * @return The x-coordinate of the mouse, or -1 if mouse input is disabled.
	 */
	public int getMouseX() {
		return mouseEnabled ? Mouse.getX() : -1;
	}
	
	/**
	 * Gets the y-coordinate of the mouse. Note that the bottom-left corner of
	 * the screen is treated as (0,0).
	 * 
	 * @return The y-coordinate of the mouse, or -1 if mouse input is disabled.
	 */
	public int getMouseY() {
		return mouseEnabled ? Mouse.getY() : -1;
	
	}
	
	/**
	 * Gets the state of the queried button.
	 * 
	 * @param button The button to query (note that 0 = left button, 1 = right
	 * button, 2 = middle button).
	 * 
	 * @return The state of the queried button, or {@code false} if mouse input
	 * is disabled.
	 */
	public boolean isButtonDown(int button) {
		if(button >= buttonStates.length) {
			log.logCritical("The queried button ID (" + button + ") is out of bounds");
			return false;
		}
		return mouseEnabled ? buttonStates[button] : false;
	}
	
	/**
	 * Gets the state of the queried button. See 
	 * {@link org.lwjgl.input.Keyboard Keyboard} for key codes.
	 * 
	 * @param key The key to query.
	 * 
	 * @return The state of the queried key, or {@code false} if keyboard input
	 * is disabled.
	 */
	public boolean isKeyDown(int key) {
		if(key >= keyStates.length) {
			log.logCritical("The queried key ID (" + key + ") is out of bounds");
			return false;
		}
		return keyboardEnabled ? keyStates[key] : false;
	}
	
	/**
	 * Checks for whether or not a control key is being held down.
	 * 
	 * @return {@code true} if a control key is down; {@code false} otherwise.
	 */
	public boolean isCtrlKeyDown() {
		return isKeyDown(Keyboard.KEY_LCONTROL) || isKeyDown(Keyboard.KEY_RCONTROL) || (isMac && (isKeyDown(Keyboard.KEY_RETURN) || isKeyDown(Keyboard.KEY_LMETA) || isKeyDown(Keyboard.KEY_RMETA)));
	}
	
	/**
	 * Checks for whether or not a shift key is being held down.
	 * 
	 * @return {@code true} if a shift key is down; {@code false} otherwise.
	 */
	public boolean isShiftKeyDown() {
		return isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(Keyboard.KEY_RSHIFT);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets a reference to the input manager.
	 * 
	 * @return The InputManager instance.
	 */
	public static InputManager get() {
		return INSTANCE;
	}
	
	/**
	 * Gets the numeric value of a key, given a valid numeric keycode. For
	 * example, given {@code 5} - which is the keycode for {@code 4} - {@code
	 * 4} will be returned.
	 * 
	 * @param key The keycode.
	 * 
	 * @return The key's numeric value, or {@code -1} if the given key lacks
	 * such a value.
	 */
	public static int numericKeyValue(int key) {
		// see http://legacy.lwjgl.org/javadoc/constant-values.html
		// if these values are changed, just use a switch() junction
		//if(key > 1 && key < 11)
		//	return key - 1;
		//else if(key == 1)
		//	return 0;
		
		switch(key) {
			case Keyboard.KEY_0:
			case Keyboard.KEY_NUMPAD0:
				return 0;
			case Keyboard.KEY_1:
			case Keyboard.KEY_NUMPAD1:
				return 1;
			case Keyboard.KEY_2:
			case Keyboard.KEY_NUMPAD2:
				return 2;
			case Keyboard.KEY_3:
			case Keyboard.KEY_NUMPAD3:
				return 3;
			case Keyboard.KEY_4:
			case Keyboard.KEY_NUMPAD4:
				return 4;
			case Keyboard.KEY_5:
			case Keyboard.KEY_NUMPAD5:
				return 5;
			case Keyboard.KEY_6:
			case Keyboard.KEY_NUMPAD6:
				return 6;
			case Keyboard.KEY_7:
			case Keyboard.KEY_NUMPAD7:
				return 7;
			case Keyboard.KEY_8:
			case Keyboard.KEY_NUMPAD8:
				return 8;
			case Keyboard.KEY_9:
			case Keyboard.KEY_NUMPAD9:
				return 9;
		}
		
		return -1;
	}
	
}
