package com.stabilise.input;

import java.io.IOException;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.lwjgl.input.Keyboard;

import com.stabilise.core.Constants;
import com.stabilise.util.ConfigFile;
import com.stabilise.util.Log;

/**
 * The Controller class manages mouse and keyboard inputs, and directs them in
 * accordance with the key config.
 * 
 * <p>Before interacting with this class, it it recommended that
 * {@link #initialise()} is invoked.
 * 
 * @see InputManager
 */
public class Controller implements Focusable {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Game controls. */
	public static enum Control {
		LEFT("left", Keyboard.KEY_LEFT, false),
		RIGHT("right", Keyboard.KEY_RIGHT, false),
		UP("up", Keyboard.KEY_UP, false),
		DOWN("down", Keyboard.KEY_DOWN, false),
		JUMP("jump", Keyboard.KEY_SPACE, false),
		ATTACK("attack", Keyboard.KEY_F, false),
		SPECIAL("special", Keyboard.KEY_G, false),
		
		PAUSE("pause", Keyboard.KEY_ESCAPE, false),
		DEBUG("debug", Keyboard.KEY_F3, false),
		
		// Dev controls
		
		SAVE_LOG("savelog", Keyboard.KEY_L, true),
		RESTORE("restore", Keyboard.KEY_R, true),
		SUMMON("summon", Keyboard.KEY_T, true),
		SUMMON_SWARM("summonswarm", Keyboard.KEY_Y, true),
		KILL_MOBS("killallmobs", Keyboard.KEY_K, true),
		FLYLEFT("flyleft", Keyboard.KEY_A, true),
		FLYRIGHT("flyright", Keyboard.KEY_D, true),
		FLYUP("flyup", Keyboard.KEY_W, true),
		FLYDOWN("flydown", Keyboard.KEY_S, true),
		DESTROY_TILES("destroytiles", Keyboard.KEY_Q, true),
		ZOOM_OUT("zoomout", Keyboard.KEY_MINUS, true),
		ZOOM_IN("zoomin", Keyboard.KEY_EQUALS, true),
		PLACE_TILE("placetile", Keyboard.KEY_P, true),
		INTERACT("interact", Keyboard.KEY_I, true),
		TEST_RANDOM_THING("testrandomthing", Keyboard.KEY_0, true),
		
		SOUND_LOAD("loadsound", Keyboard.KEY_1, true),
		SOUND_LOAD_STREAM("loadsoundasstream", Keyboard.KEY_2, true),
		SOUND_UNLOAD("unloadsound", Keyboard.KEY_3, true),
		SOUND_UNLOAD_INSTANCE("unloadsoundinstance", Keyboard.KEY_4, true),
		SOUND_PLAY("playsound", Keyboard.KEY_5, true),
		SOUND_PAUSE("pausesound", Keyboard.KEY_6, true),
		SOUND_STOP("stopsound", Keyboard.KEY_7, true),
		SOUND_LOOP("togglesoundloop", Keyboard.KEY_8, true);
		
		/** The field's name in the config file. */
		public final String fieldName;
		/** The default key. */
		public final int defaultKey;
		/** Whether or not the control is a developer control to be used for
		 * testing. If {@code true}, the control will be inaccessible in
		 * non-dev versions. */
		public final boolean devControl;
		
		
		/**
		 * Creates a new Control.
		 * 
		 * @param fieldName The Control's field name in the config file. It is
		 * implicitly trusted that this value is unique.
		 * @param defaultKey The default key bound to the Control. It is
		 * implicitly trusted that this value is unique.
		 * @param devControl Whether or not the control is a control exclusive
		 * to developer versions of the application.
		 */
		private Control(String fieldName, int defaultKey, boolean devControl) {
			this.fieldName = fieldName;
			this.defaultKey = defaultKey;
			this.devControl = devControl;
		}
		
		/**
		 * Checks for whether or not the control is a valid and usable one.
		 * 
		 * @return {@code true} if the game is a dev version or the control
		 * is not a dev control; {@code false} otherwise.
		 */
		private boolean isValid() {
			return !devControl || Constants.DEV_VERSION;
		}
	};
	
	/** The key mappings. */
	private static DualHashBidiMap<Integer, Control> KEY_MAP = new DualHashBidiMap<Integer, Control>();
	
	/** Whether or not the controller mappings have been set up. */
	private static boolean initialised = false;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The input manager. */
	public InputManager input;
	
	/** The focus of the controller. */
	public Controllable focus;
	
	
	/**
	 * Creates a new Controller. This will set itself as the focus of the
	 * InputManager.
	 */
	public Controller(Controllable focus) {
		this.focus = focus;
		
		input = InputManager.get();
		input.setFocus(this);
	}
	
	/**
	 * Gets the state of the given control.
	 * 
	 * @param control The control.
	 * 
	 * @return {@code true} if the key bound to the control is pressed,
	 * {@code false} otherwise.
	 */
	public boolean isControlPressed(Control control) {
		// TODO: possible NPE
		return control.isValid() && input.isKeyDown(KEY_MAP.getKey(control));
	}
	
	@Override
	public void handleButtonPress(int button, int x, int y) {
		focus.handleButtonPress(button, x, y);
	}
	
	@Override
	public void handleButtonRelease(int button, int x, int y) {
		focus.handleButtonRelease(button, x, y);
	}
	
	@Override
	public void handleKeyPress(int key) {
		Control control = KEY_MAP.get(key);
		if(control != null && control.isValid())
			focus.handleControlPress(control);
		focus.handleKeyPress(key);
	}
	
	@Override
	public void handleKeyRelease(int key) {
		Control control = KEY_MAP.get(key);
		if(control != null && control.isValid())
			focus.handleControlRelease(control);
		focus.handleKeyRelease(key);
	}
	
	@Override
	public void handleMouseMove(int dx, int dy) {
		// nothing to see here, move along
	}
	
	@Override
	public void handleMouseWheelScroll(int scroll) {
		focus.handleMouseWheelScroll(scroll);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Binds a key to a control.
	 * 
	 * @param key The key.
	 * @param control The control.
	 * 
	 * @return {@code true} if the key was successfully bound, or {@code false}
	 * if the binding failed due to the control already possessing a mapped
	 * key.
	 */
	public static boolean bindKey(int key, Control control) {
		if(key < 0 || (KEY_MAP.containsValue(control) && KEY_MAP.getKey(control) != key))
			return false;
		
		KEY_MAP.put(key, control);
		
		return true;
	}
	
	/**
	 * Weakly binds a key to a control - that is, only binds it if the control
	 * lacks a mapped key and the key is not in use.
	 * 
	 * @param key The key.
	 * @param control The control.
	 * 
	 * @return {@code true} if the control was successfully bound;
	 * {@code false} otherwise.
	 */
	private static boolean bindKeyWeak(int key, Control control) {
		if(key < 0 || KEY_MAP.containsKey(key) || KEY_MAP.containsValue(control))
			return false;
		
		KEY_MAP.put(key, control);
		
		return true;
	}
	
	/**
	 * Initialises the Controller class.
	 */
	public static void initialise() {
		if(!initialised) {
			setupConfig();
			initialised = true;
		}
	}
	
	/**
	 * Sets up the control configuration.
	 */
	public static void setupConfig() {
		if(!loadConfig()) {
			Log.message("Controls config could not be loaded - resetting to default values.");
			defaultConfig();
			saveConfig();
		}
	}
	
	/**
	 * Sets each config value to its default.
	 */
	public static void defaultConfig() {
		Control[] controls = Control.values();
		for(int i = 0; i < controls.length; i++) {
			bindKey(controls[i].defaultKey, controls[i]);
		}
	}
	
	/**
	 * Loads the key controls config.
	 * 
	 * @return {@code true} if the controls were successfully loaded and set,
	 * {@code false} otherwise.
	 */
	public static boolean loadConfig() {
		ConfigFile config = new ConfigFile("controls");
		try {
			config.load();
		} catch(IOException e) {
			Log.critical("Could not load controls config!");
			return false;
		}
		
		Control[] controls = Control.values();
		// If there are any missing values, we'll need to save the config after loading it
		boolean missingConfigs = false;
		
		// Attempt to bind every control, whether it was stored or not
		for(int i = 0; i < controls.length; i++) {
			if(!bindKey(config.getInteger(controls[i].fieldName), controls[i]))
				return false;
		}
		
		// Check to see if any configs are missing from the config file, and
		// bind them to the default value if so
		for(int i = 0; i < controls.length; i++) {
			if(!KEY_MAP.containsValue(controls[i]) && controls[i].isValid()) {
				bindKey(controls[i].defaultKey, controls[i]);
				missingConfigs = true;
			}
		}
		
		// Lastly, weakly bind any unbound controls (e.g., if a new control was
		// added since last the config was saved, it will need to be added)
		for(int i = 0; i < controls.length; i++) {
			if(controls[i].isValid() && bindKeyWeak(controls[i].defaultKey, controls[i]))
				missingConfigs = true;
		}
		
		// Finally, check to see if there remain any unbound configs
		for(int i = 0; i < controls.length; i++) {
			if(!KEY_MAP.containsValue(controls[i]) && controls[i].isValid())
				return false;
		}
		
		if(missingConfigs)
			saveConfig();
		
		return true;
	}
	
	/**
	 * Saves the key controls config.
	 */
	public static void saveConfig() {
		ConfigFile config = new ConfigFile("controls");
		
		Control[] controls = Control.values();
		for(int i = 0; i < controls.length; i++) {
			if(controls[i].isValid())
				config.addInteger(controls[i].fieldName, KEY_MAP.getKey(controls[i]));
		}
		
		try {
			config.safeSave();
		} catch(IOException e) {
			Log.critical("Could not save controls config!", e);
		}
	}
	
}
