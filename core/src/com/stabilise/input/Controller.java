package com.stabilise.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.IntMap;
import com.stabilise.util.io.data.DataCompound;

import java.util.Arrays;
import java.util.OptionalInt;

/**
 * A Controller translates key input into configurable controls.
 *
 * @see Controllable
 */
public class Controller<T extends Enum<T> & IControl> implements InputProcessor {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    private static final int
            KEYCODE_MOUSE_LEFT = buttonToKeycode(Input.Buttons.LEFT),
            KEYCODE_MOUSE_RIGHT = buttonToKeycode(Input.Buttons.RIGHT),
            KEYCODE_MOUSE_MIDDLE = buttonToKeycode(Input.Buttons.MIDDLE),
            // arbitrary, but these don't clash with other keycodes
            KEYCODE_SCROLL_X = -10,
            KEYCODE_SCROLL_Y = -11;
    
    /** Whether the controller mappings have been set up. */
    private static boolean initialised = false;
    
    
    /** The actual config data. */
    //private static final Config CONFIG = new Config(getDefaults(), Resources.DIR_CONFIG.child("controls.txt"));
    
    /** The key mappings. Maps keycodes to controls. */
    //private static final BiMap<Integer, Control> CONTROL_MAP = HashBiMap.create();
    /** The control mappings. Maps controls to keycodes. The inverse of
     * {@link #CONTROL_MAP}. */
    //private static final BiMap<Control, Integer> KEY_MAP = CONTROL_MAP.inverse();
    
    
    
    
    
    static {
        initialise();
    }
    
    /** Invoking this loads this class into memory. */
    public static void poke() {}
    
    //--------------------==========--------------------
    //------------=====Member Variables=====------------
    //--------------------==========--------------------
    
    /** A reference to the main Input. */
    public final Input input;
    
    /** The focus of this controller. */
    private final Controllable<T> focus;
    
    private final T[] controls;
    
    /** Because controls may be triggered by key combinations (e.g. Shift + H)
     * it is insufficient to respond to key release events (e.g. H released)
     * because regardless of its current held state, it is unclear whether Shift
     * was held when H was first pressed. The solution is to simply record
     * whether a control is pressed. */
    private final boolean[] pressedControls;
    
    /** Maps keycode -> binding. */
    private final IntMap<KeyBinding> bindings;
    
    
    /**
     * Creates a new Controller. It must be manually set as an input processor.
     */
    public Controller(ControlConfig<T> config, Controllable<T> focus) {
        this.controls = config.controls;
    
        bindings = new IntMap<>(controls.length);
        pressedControls = new boolean[controls.length];
        updateBindings(config);
        
        this.focus = focus;
        this.input = Gdx.input;
    }
    
    public void updateBindings(ControlConfig<T> config) {
        // Since the config stores the bindings as a Control -> Keycode-esque
        // map and we store them as a Keycode -> Control-esque map, we have do
        // do some conversions...
        
        bindings.clear();
        for(int i = 0; i < controls.length; i++) {
            KeyMapping mapping = config.mappings[i];
            KeyBinding binding = bindings.get(mapping.keycode);
            if(binding == null) {
                binding = new KeyBinding();
                bindings.put(mapping.keycode, binding);
            }
            
            if(mapping.heldKeys.length == 0) {
                // If the mapping doesn't have any held keys (i.e. it's just a
                // straightforward key press)
                if(!binding.bindCtrl(i))
                    throw new IllegalStateException("Two controls ("
                            + controls[binding.boundCtrl].identifier()  + " and "
                            + controls[i].identifier() + ") bound to the same key: " +
                            keycodeToString(mapping.keycode));
            } else {
                // The mapping has some held keys. This requires a bit more work
                // to bind.
                ConditionalKeyBinding condBinding = new ConditionalKeyBinding(i, mapping.heldKeys);
                binding.growCondBindings();
                if(binding.hasCondBindings()) {
                
                } else {
                    binding.condBindings = new ConditionalKeyBinding[1];
                    binding.condBindings[0] = condBinding;
                }
            }
        }
    }
    
    /**
     * Gets the state of the given control.
     * 
     * @return {@code true} if the key bound to the control is pressed,
     * {@code false} otherwise.
     * 
     * @throws NullPointerException if {@code control} is {@code null}.
     */
    public boolean isControlPressed(T control) {
        return pressedControls[control.ordinal()];
    }
    
    // Helper methods to pretty up and avoid code duplication in keyDown() and
    // keyUp()
    
    private boolean tryPress(int controlIndex) {
        if(pressedControls[controlIndex])
            return false;
        pressedControls[controlIndex] = true;
        return true;
    }
    
    private boolean tryRelease(int controlIndex) {
        if(!pressedControls[controlIndex])
            return false;
        pressedControls[controlIndex] = false;
        return true;
    }
    
    private boolean keyOrButtonPress(int keycode, int screenX, int screenY, float amount) {
        KeyBinding binding = bindings.get(keycode);
        
        // If the key isn't bound to any control, goodbye
        if(binding == null)
            return false;
        
        // We have a binding, and it doesn't require any other keys to be held
        // down -- easy, we trigger the control.
        if(!binding.hasCondBindings()) {
            // Only do anything if the control isn't already pressed
            if(tryPress(binding.boundCtrl))
                return focus.handleControlPress(controls[binding.boundCtrl], screenX, screenY, amount);
            // else fall through to the return false;
        } else {
            // We have one or more "held-down" bindings -- we iterate through
            // them and trigger the first one that's active.
            outerLoop:
            for(ConditionalKeyBinding condBinding : binding.condBindings) {
                // Check that all the required held keys are indeed held
                for(int key : condBinding.heldKeys) {
                    if(!input.isKeyPressed(key))
                        continue outerLoop;
                }
            
                if(tryPress(condBinding.boundCtrl))
                    return focus.handleControlPress(controls[condBinding.boundCtrl], screenX, screenY, amount);
                else
                    // Dunno if this is necessary, but let's keep it for now.
                    break outerLoop;
            }
            
            // If we make it to here then none of the "held-down" combinations
            // are inputted, so we go for the non-held-down option, if it exists
            if(binding.hasBoundCtrl() && tryPress(binding.boundCtrl))
                return focus.handleControlPress(controls[binding.boundCtrl], screenX, screenY, amount);
        }
    
        // If we make it this far then the key didn't correspond to any valid
        // controls
        return false;
    }
    
    private boolean keyOrButtonRelease(int keycode, int screenX, int screenY) {
        KeyBinding binding = bindings.get(keycode);
    
        // If the key isn't bound to any control, goodbye
        if(binding == null)
            return false;
    
        // If focus.handleControlRelease() returns true at any point we don't
        // want to return from this method immediately, so we store the result
        // here and return it.
        boolean processed = false;
    
        // The release is triggered regardless of whether the held keys of a
        // key combination (e.g. the Shift in Shift + H) are held, so just try
        // to release any control bound to this key, whether in a held
        // combination or not.
    
        if(binding.hasBoundCtrl() && tryRelease(binding.boundCtrl))
            processed = focus.handleControlRelease(controls[binding.boundCtrl], screenX, screenY);
    
        if(binding.hasCondBindings()) {
            for(ConditionalKeyBinding condBinding : binding.condBindings) {
                if(tryRelease(condBinding.boundCtrl))
                    processed |= focus.handleControlRelease(controls[binding.boundCtrl], screenX, screenY);
            }
        }
    
        return processed;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        return keyOrButtonPress(keycode, 0, 0, 0f);
    }
    
    @Override
    public boolean keyUp(int keycode) {
        return keyOrButtonRelease(keycode, 0, 0);
    }
    
    @Override
    public boolean keyTyped(char character) {
        // Note to self: by looking in DefaultLwjgl3Input it turns out this is
        // called when a key on the keyboard is pressed, and also when it is
        // held down long enough for the key-press event to retrigger (upon
        // which this is repeatedly called so long as the key is held down).
        // ...
        // Of course, we're not going to do anything with this anyway.
        return false;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return keyOrButtonPress(buttonToKeycode(button), screenX, screenY, 0f);
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return keyOrButtonRelease(buttonToKeycode(button), screenX, screenY);
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Note to self: this is basically the same as mouseMoved(), but when
        // a button is held down.
        return false; // we don't use this
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false; // we don't use this
    }
    
    @Override
    public boolean scrolled(float amountX, float amountY) {
        boolean processed = false;
        if(amountX != 0f)
            processed = keyOrButtonPress(KEYCODE_SCROLL_X, 0, 0, amountX);
        if(amountY != 0f)
            processed |= keyOrButtonPress(KEYCODE_SCROLL_Y, 0, 0, amountY);
        return processed;
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Maps a button id to a "keycode" for it. Convert back using {@link
     * #keycodeToButton(int)}. This will return a negative value. We do this so
     * that a keycode could refer to either a key or a button, depending on
     * whether it's positive or negative.
     *
     * @see com.badlogic.gdx.Input.Buttons
     */
    private static int buttonToKeycode(int button) {
        return -button - 1;
    }
    
    /**
     * Maps a keycode to the equivalent button id. The keycode must be negative.
     * This is the inverse of {@link #buttonToKeycode(int)}.
     *
     * @see com.badlogic.gdx.Input.Buttons
     */
    private static int keycodeToButton(int keycode) {
        if(keycode >= 0)
            throw new IllegalArgumentException("Button keycode must be negative!");
        return -keycode - 1; // = -(keycode + 1)
    }
    
    /**
     * Converts a keycode to string representation, or returns {@code null} if
     * this does not represent a valid keycode.
     *
     * @see #stringToKeycode(String)
     */
    public static String keycodeToString(int keycode) {
        if(keycode > 0)
            return Input.Keys.toString(keycode); // can return null
        // Can't switch on these since java doesn't recognise them as
        // compile-time constants...
        else if(keycode == KEYCODE_MOUSE_LEFT)
            return "Mouse Left";
        else if(keycode == KEYCODE_MOUSE_RIGHT)
            return "Mouse Right";
        else if(keycode == KEYCODE_MOUSE_MIDDLE)
            return "Mouse Middle";
        else if(keycode == KEYCODE_SCROLL_Y)
            return "Scroll";
        else if(keycode == KEYCODE_SCROLL_X)
            return "Horizontal Scroll";
        return null;
    }
    
    /**
     * Converts the string representation of a keycode to a keycode. An empty
     * optional is returned if the given string does not represent a valid
     * keycode.
     *
     * @see #keycodeToString(int)
     */
    public static OptionalInt stringToKeycode(String s) {
        int keycode = Input.Keys.valueOf(s);
        if(keycode != -1)
            return OptionalInt.of(keycode);
        else switch(s) {
            case "Mouse Left":
                return OptionalInt.of(KEYCODE_MOUSE_LEFT);
            case "Mouse Right":
                return OptionalInt.of(KEYCODE_MOUSE_RIGHT);
            case "Mouse Middle":
                return OptionalInt.of(KEYCODE_MOUSE_MIDDLE);
            case "Scroll":
                return OptionalInt.of(KEYCODE_SCROLL_Y);
            case "Horizontal Scroll":
                return OptionalInt.of(KEYCODE_SCROLL_X);
        }
        return OptionalInt.empty();
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
     * Loads the key controls config.
     */
    public static void loadConfig() {
        /*
        boolean changes;
        
        try {
            changes = CONFIG.load();
        } catch(IOException e) {
            changes = true;
            Log.get().postWarning("Could not load controls config: " + e.getMessage());
        }
        
        changes |= updateMaps();
        
        if(changes)
            saveConfig();
        */
    }
    
    private static boolean updateMaps() {
        /*
        boolean changes = false;
        
        CONTROL_MAP.clear();
        
        Control[] controls = Control.values();
        for(Control c : controls) {
            if(!c.valid)
                continue;
            int key = CONFIG.values.getI32(c.fieldName);
            // If key has already been bound to another control, reset this
            // control to the default value.
            if(CONTROL_MAP.containsKey(key)) {
                CONFIG.reset(c.fieldName);
                key = CONFIG.values.getI32(c.fieldName);
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
        */
        return false;
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * Represents the control(s) bound to the press of a specific key (or click
     * of the mouse).
     */
    private static class KeyBinding {
        
        /** The (ordinal of the) control to be triggered if the key is pressed
         * while no other keys are held. Can be -1 (meaning no bound control) if
         * {@link #condBindings} is non-null. */
        int boundCtrl = -1;
        /** Lists conditional control triggers, i.e. controls that are triggered
         * if the key is pressed while certain other keys are held. There can
         * be multiple of these, covering different held key configurations.
         * Can be null if {@link #boundCtrl} is non-null. */
        ConditionalKeyBinding[] condBindings = null;
    
        boolean bindCtrl(int ctrl) {
            if(hasBoundCtrl())
                return false;
            boundCtrl = ctrl;
            return true;
        }
        
        boolean hasBoundCtrl() {
            return boundCtrl != -1;
        }
        
        boolean hasCondBindings() {
            return condBindings != null && condBindings[0] != null;
        }
        
        void growCondBindings() {
            if(condBindings == null)
                condBindings = new ConditionalKeyBinding[1];
            else
                condBindings = Arrays.copyOf(condBindings, condBindings.length + 1);
        }
        
    }
    
    private static class ConditionalKeyBinding {
        
        int boundCtrl;
        int[] heldKeys;
        
        ConditionalKeyBinding(int boundCtrl, int[] heldKeys) {
            this.boundCtrl = boundCtrl;
            this.heldKeys = heldKeys.clone();
        }
        
        boolean matches(ConditionalKeyBinding other) {
            return Arrays.equals(heldKeys, other.heldKeys);
        }
        
    }
    
}
