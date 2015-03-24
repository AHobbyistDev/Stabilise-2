package com.stabilise.input;

import static com.badlogic.gdx.Input.*;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.stabilise.core.Constants;
import com.stabilise.util.ConfigFile;
import com.stabilise.util.Log;

/**
 * A Controller translates key input into configurable controls.
 * 
 * @see InputManager
 */
public class Controller implements InputProcessor {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** Game controls. */
	public static enum Control {
		LEFT("left", Keys.LEFT),
		RIGHT("right", Keys.RIGHT),
		UP("up", Keys.UP),
		DOWN("down", Keys.DOWN),
		JUMP("jump", Keys.SPACE),
		ATTACK("attack", Keys.F),
		SPECIAL("special", Keys.G),
		
		PAUSE("pause", Keys.ESCAPE),
		DEBUG("debug", Keys.F3),
		
		// Dev controls
		
		SAVE_LOG("savelog", Keys.L, true),
		RESTORE("restore", Keys.R, true),
		SUMMON("summon", Keys.T, true),
		SUMMON_SWARM("summonswarm", Keys.Y, true),
		KILL_MOBS("killallmobs", Keys.K, true),
		FLYLEFT("flyleft", Keys.A, true),
		FLYRIGHT("flyright", Keys.D, true),
		FLYUP("flyup", Keys.W, true),
		FLYDOWN("flydown", Keys.S, true),
		DESTROY_TILES("destroytiles", Keys.Q, true),
		ZOOM_OUT("zoomout", Keys.MINUS, true),
		ZOOM_IN("zoomin", Keys.EQUALS, true),
		INTERACT("interact", Keys.I, true),
		TEST_RANDOM_THING("testrandomthing", Keys.NUM_0, true);
		
		/** The field's name in the config file. */
		public final String fieldName;
		/** The default key. */
		public final int defaultKey;
		/** Whether or not the control is a developer control to be used for
		 * testing. If {@code true}, the control will be inaccessible in
		 * non-dev versions. */
		public final boolean devControl;
		/** Whether this is a valid control. An invalid control should not be
		 * used. */
		public final boolean valid;
		
		
		/**
		 * Creates a new non-dev Control.
		 * 
		 * @param fieldName The Control's field name in the config file. It is
		 * implicitly trusted that this value is unique.
		 * @param defaultKey The default key bound to the Control. It is
		 * implicitly trusted that this value is unique.
		 */
		private Control(String fieldName, int defaultKey) {
			this(fieldName, defaultKey, false);
		}
		
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
			
			valid = !devControl || Constants.DEV_VERSION;
		}
		
	};
	
	/** The key mappings. Maps keycodes to controls. */
	private static final BiMap<Integer, Control> CONTROL_MAP = HashBiMap.create();
	/** The control mappings. Maps controls to keycodes. The inverse of
	 * {@link #CONTROL_MAP}. */
	private static final BiMap<Control, Integer> KEY_MAP = CONTROL_MAP.inverse();
	
	/** Whether or not the controller mappings have been set up. */
	private static boolean initialised = false;
	
	static {
		initialise();
	}
	
	/** Invoking this loads this class into memory. */
	public static void poke() {}
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The main Input. */
	public final Input input;
	
	/** The focus of the controller. */
	private Controllable focus;
	
	
	/**
	 * Creates a new Controller. It must be manually set as an input processor.
	 */
	public Controller(Controllable focus) {
		this.focus = focus;
		
		input = Gdx.input;
	}
	
	/**
	 * Gets the state of the given control.
	 * 
	 * @param control The control.
	 * 
	 * @return {@code true} if the key bound to the control is pressed,
	 * {@code false} otherwise.
	 * 
	 * @throws NullPointerException if {@code control} is {@code null}.
	 */
	public boolean isControlPressed(Control control) {
		return control.valid && input.isKeyPressed(KEY_MAP.get(control));
	}
	
	@Override
	public boolean keyDown(int keycode) {
		Control control = CONTROL_MAP.get(keycode);
		if(control != null && control.valid)
			return focus.handleControlPress(control);
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		Control control = CONTROL_MAP.get(keycode);
		if(control != null && control.valid)
			return focus.handleControlRelease(control);
		return false;
	}
	
	@Override
	public boolean keyTyped(char character) {
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	
	@Override
	public boolean scrolled(int amount) {
		return false;
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
		if(key < 0 || (CONTROL_MAP.containsValue(control) && KEY_MAP.get(control) != key))
			return false;
		
		CONTROL_MAP.put(key, control);
		
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
		if(key < 0 || CONTROL_MAP.containsKey(key) || CONTROL_MAP.containsValue(control))
			return false;
		
		CONTROL_MAP.put(key, control);
		
		return true;
	}
	
	/**
	 * Initialises the Controller class. This is invoked when this class is
	 * loaded into memory.
	 */
	private static void initialise() {
		if(!initialised) {
			initialised = true;
			setupConfig();
		}
	}
	
	/**
	 * Sets up the control configuration. If the configuration is already set
	 * up, it will be reset.
	 */
	public static void setupConfig() {
		if(!loadConfig()) {
			Log.get().postWarning("Controls config could not be loaded - resetting to default values.");
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
			Log.get().postWarning("Could not load controls config (" + e.getClass().getSimpleName() + ")!");
			return false;
		}
		
		Control[] controls = Control.values();
		// If there are any missing values, we'll need to save the config after loading it
		boolean missingConfigs = false;
		
		// Attempt to bind every control, whether it was stored or not
		for(Control c : controls) {
			if(!bindKey(config.getInteger(c.fieldName), c))
				return false;
		}
		
		// Check to see if any configs are missing from the config file, and
		// bind them to the default value if so
		for(Control c : controls) {
			if(!CONTROL_MAP.containsValue(c) && c.valid) {
				bindKey(c.defaultKey, c);
				missingConfigs = true;
			}
		}
		
		// Lastly, weakly bind any unbound controls (e.g., if a new control was
		// added since last the config was saved, it will need to be added)
		for(Control c : controls) {
			if(c.valid && bindKeyWeak(c.defaultKey, c))
				missingConfigs = true;
		}
		
		// Finally, check to see if there remain any unbound configs
		for(Control c : controls) {
			if(!CONTROL_MAP.containsValue(c) && c.valid)
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
		for(Control c : controls) {
			if(c.valid)
				config.addInteger(c.fieldName, KEY_MAP.get(c));
		}
		
		try {
			config.safeSave();
		} catch(IOException e) {
			Log.get().postWarning("Could not save controls config!", e);
		}
	}
	
}
