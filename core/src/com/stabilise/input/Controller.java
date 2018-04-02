package com.stabilise.input;

import static com.badlogic.gdx.Input.*;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.stabilise.core.Constants;
import com.stabilise.core.Resources;
import com.stabilise.util.ArrayUtil;
import com.stabilise.util.Config;
import com.stabilise.util.Log;
import com.stabilise.util.io.data.DataCompound;

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
        LEFT            ("left",         Keys.LEFT),
        RIGHT           ("right",        Keys.RIGHT),
        UP              ("up",           Keys.UP),
        DOWN            ("down",         Keys.DOWN),
        JUMP            ("jump",         Keys.SPACE),
        ATTACK          ("attack",       Keys.F),
        SPECIAL         ("special",      Keys.G),
        
        PAUSE           ("pause",        Keys.ESCAPE),
        DEBUG           ("debug",        Keys.F3),
        
        // Dev controls
        
        SAVE_LOG        ("savelog",      Keys.L,       true),
        RESTORE         ("restore",      Keys.R,       true),
        SUMMON          ("summon",       Keys.T,       true),
        SUMMON_SWARM    ("summonswarm",  Keys.Y,       true),
        KILL_MOBS       ("killallmobs",  Keys.K,       true),
        FLYLEFT         ("flyleft",      Keys.A,       true),
        FLYRIGHT        ("flyright",     Keys.D,       true),
        FLYUP           ("flyup",        Keys.W,       true),
        FLYDOWN         ("flydown",      Keys.S,       true),
        ZOOM_OUT        ("zoomout",      Keys.MINUS,   true),
        ZOOM_IN         ("zoomin",       Keys.EQUALS,  true),
        INTERACT        ("interact",     Keys.E,       true),
        TEST_RANDOM     ("testrandthing",Keys.F1,      true),
        ADVANCE_TICK    ("forceTick",    Keys.F2,      true),
        TOG_HITBOX_RENDER("togHbRender", Keys.H,       true),
        TOG_SLICE_BORDERS("sliceBorders",Keys.B,       true),
        TOG_REGION_TINT ("regionTint",   Keys.N,       true),
        NEXT_TILE       ("nextTile",     Keys.PERIOD,  true),
        PREV_TILE       ("prevTile",     Keys.COMMA,   true),
        CLEAR_INVENTORY ("clearInv",     Keys.C,       true),
        PRINT_INVENTORY ("printInv",     Keys.I,       true),
        PROFILER        ("printProfiler",Keys.P,       true),
        PORTAL          ("portal",       Keys.ENTER,   true);
        
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
    
    /** The actual config data. */
    private static final Config CONFIG = new Config(getDefaults(), Resources.DIR_CONFIG.child("controls.txt"));
    
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
        /*
        if(key < 0 || (CONTROL_MAP.containsValue(control) && KEY_MAP.get(control) != key))
            return false;
        
        CONTROL_MAP.put(key, control);
        
        return true;
        */
        
        Log.get().postWarning("Attempted to bind a key -- functionality is currently NYI.");
        return false;
    }
    
    /**
     * Initialises the Controller class. This is invoked when this class is
     * loaded into memory.
     */
    private static void initialise() {
        if(!initialised) {
            initialised = true;
            loadConfig();
        }
    }
    
    /**
     * Gets the DataCompound of default config values to feed into the
     * constructor of {@link #CONFIG}.
     */
    private static DataCompound getDefaults() {
        DataCompound defaults = Config.CONFIG_FORMAT.newCompound();
        ArrayUtil.forEach(Control.values(), c -> {
            if(c.valid)
                defaults.put(c.fieldName, c.defaultKey);
        });
        return defaults;
    }
    
    /**
     * Loads the key controls config.
     */
    public static void loadConfig() {
        boolean changes = false;
        try {
            changes = CONFIG.load();
        } catch(IOException e) {
            changes = true;
            Log.get().postWarning("Could not load controls config: " + e.getMessage());
        }
        
        changes |= updateMaps();
        
        if(changes)
            saveConfig();
    }
    
    private static boolean updateMaps() {
        boolean changes = false;
        
        CONTROL_MAP.clear();
        
        Control[] controls = Control.values();
        for(int i = 0; i < controls.length; i++) {
            Control c = controls[i];
            if(!c.valid)
                continue;
            int key = CONFIG.values.getInt(c.fieldName);
            // If key has already been bound to another control, reset this
            // control to the default value.
            if(CONTROL_MAP.containsKey(key)) {
                CONFIG.reset(c.fieldName);
                key = CONFIG.values.getInt(c.fieldName);
                changes = true;
                // If however the default value is also taken by something,
                // then we give up on trying to make things work and reset
                // everything to the defaults.
                if(CONTROL_MAP.containsKey(key)) {
                    Log.get().postWarning("Incompatible control config loaded --"
                            + " resetting to default values.");
                    resetToDefaults();
                    return true;
                }
            }
            
            // Update the entry in CONTROL_MAP. We don't need to update KEY_MAP
            // since it's backed by CONTROL_MAP.
            CONTROL_MAP.put(key, c);
        }
        
        return changes;
    }
    
    /**
     * Resets all the controls to their default values.
     */
    private static void resetToDefaults() {
        // Reset true config
        CONFIG.resetToDefaults();
        
        // Fill up CONTROL_MAP
        CONTROL_MAP.clear();
        ArrayUtil.forEach(Control.values(), c -> {
            if(c.valid)
                CONTROL_MAP.put(CONFIG.values.getInt(c.fieldName), c);
        });
        
    }
    
    /**
     * Saves the key controls config.
     */
    public static void saveConfig() {
        try {
            CONFIG.save();
        } catch(IOException e) {
            Log.get().postWarning("Could not save controls config!", e);
        }
    }
    
}
