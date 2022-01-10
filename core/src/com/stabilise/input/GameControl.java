package com.stabilise.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Constants;
import com.stabilise.core.Resources;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game controls.
 */
public enum GameControl implements IControl {
    
    LEFT            ("left",         Keys.LEFT),
    RIGHT           ("right",        Keys.RIGHT),
    UP              ("up",           Keys.UP),
    DOWN            ("down",         Keys.DOWN),
    JUMP            ("jump",         Keys.SPACE),
    ATTACK          ("attack",       Keys.F),
    SPECIAL         ("special",      Keys.G),
    
    PAUSE           ("pause",        Keys.ESCAPE),
    DEBUG           ("debug",        Keys.F3),
    
    // Rotation controls
    
    ROTATE_LEFT     ("rotate_left",  Keys.LEFT, Keys.SHIFT_LEFT),
    ROTATE_RIGHT    ("rotate_right", Keys.RIGHT, Keys.SHIFT_LEFT),
    ROTATE_UP       ("rotate_up",    Keys.UP, Keys.SHIFT_LEFT),
    
    // Dev controls
    
    SAVE_LOG        (true, "savelog",      Keys.L       ),
    RESTORE         (true, "restore",      Keys.R       ),
    SUMMON          (true, "summon",       Keys.T       ),
    SUMMON_SWARM    (true, "summonswarm",  Keys.Y       ),
    KILL_MOBS       (true, "killallmobs",  Keys.K       ),
    FLYLEFT         (true, "flyleft",      Keys.A       ),
    FLYRIGHT        (true, "flyright",     Keys.D       ),
    FLYUP           (true, "flyup",        Keys.W       ),
    FLYDOWN         (true, "flydown",      Keys.S       ),
    ZOOM_OUT        (true, "zoomout",      Keys.MINUS   ),
    ZOOM_IN         (true, "zoomin",       Keys.EQUALS  ),
    INTERACT        (true, "interact",     Keys.E       ),
    TEST_RANDOM     (true, "testrandthing",Keys.F1      ),
    ADVANCE_TICK    (true, "forceTick",    Keys.F2      ),
    TOG_DEBUG_FLAG  (true, "togDebugFlag", Keys.F12     ),
    TOG_HITBOX_RENDER(true, "togHbRender", Keys.H       ),
    TOG_SLICE_BORDERS(true, "sliceBorders",Keys.B       ),
    TOG_REGION_TINT (true, "regionTint",   Keys.N       ),
    NEXT_TILE       (true, "nextTile",     Keys.PERIOD  ),
    CLEAR_INVENTORY (true, "clearInv",     Keys.C       ),
    PREV_TILE       (true, "prevTile",     Keys.COMMA   ),
    PRINT_INVENTORY (true, "printInv",     Keys.I       ),
    PROFILER        (true, "printProfiler",Keys.P       ),
    PORTAL          (true, "portal",       Keys.ENTER   );
    
    /** The control's internal name, i.e. the one that will identify it in the
     * config. */
    public final String name;
    
    private final boolean valid;
    
    
    GameControl(String name, int defaultKey, int... heldKeys) {
        this(false, name, defaultKey, heldKeys);
    }
    
    GameControl(boolean devControl, String name, int defaultKey, int... heldKeys) {
        this.name = name;
        
        valid = !devControl || Constants.DEV_VERSION;
        
        Defaults.defaults.add(new KeyMapping(defaultKey, heldKeys));
    }
    
    @Override
    public String identifier() {
        return this.name;
    }
    
    @Override
    public KeyMapping defaultMapping() {
        return Defaults.defaults.get(this.ordinal());
    }
    
    @Override
    public boolean isEnabled() {
        return valid;
    }
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    public static FileHandle CONFIG_FILE = Resources.DIR_CONFIG.child("controls.txt");
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    private static class Defaults {
        // Maps control ID (i.e. its enum ordinal) -> key mapping.
        //
        // Accessed by the GameControl constructor to insert default values,
        // and after creating the ControlConfig this will be nulled as it is no
        // longer needed. Since enum constructors are called in order, using
        // List.add() to add entries should result in
        //
        // We need an inner class to store this for it to be accessible from
        // the enum constructor.
        //
        // The initial capacity must unavoidably be entered manually as
        // GameControl.values().length can't be queried during construction of
        // the enum entries.
        private static List<KeyMapping> defaults = new ArrayList<>(35);
    }
    
}
