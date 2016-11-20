package com.stabilise.core;

import java.io.IOException;

import com.stabilise.util.ConfigFile;
import com.stabilise.util.Log;

/**
 * This class manages the game settings.
 * 
 * <p>Before interacting with this class, it it recommended that
 * {@link #initialise()} is invoked.
 */
public class Settings {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The value for the GUI scale setting to indicate automatic GUI scaling. */
    public static final int GUI_SCALE_AUTO = 0;
    /** The value for the GUI scale setting to indicate medium GUI scaling. */
    public static final int GUI_SCALE_SMALL = 1;
    /** The value for the GUI scale setting to indicate medium GUI scaling. */
    public static final int GUI_SCALE_MEDIUM = 2;
    /** The value for the GUI scale setting to indicate large GUI scaling. */
    public static final int GUI_SCALE_LARGE = 3;
    
    /** Whether or not the effective GUI scale was changed since the last tick. */
    private static boolean GUI_SCALE_CHANGED = false;
    
    /** The minimum screen width required for the large GUI scale on the auto
     * setting. */
    @SuppressWarnings("unused")
    private static final int GUI_SCALE_LARGE_THRESHOLD_WIDTH = 1000;
    /** The minimum screen height required for the large GUI scale on the auto
     * setting. */
    @SuppressWarnings("unused")
    private static final int GUI_SCALE_LARGE_THRESHOLD_HEIGHT = 800;
    /** The minimum screen width required for the medium GUI scale on the auto
     * setting. */
    @SuppressWarnings("unused")
    private static final int GUI_SCALE_MEDIUM_THRESHOLD_WIDTH = 800;
    /** The minimum screen height required for the medium GUI scale on the auto
     * setting. */
    @SuppressWarnings("unused")
    private static final int GUI_SCALE_MEDIUM_THRESHOLD_HEIGHT = 600;
    
    /** The value for the particles setting to indicate all particles. */
    public static final int PARTICLES_ALL = 0;
    /** The value for the particles setting to indicate reduced particles. */
    public static final int PARTICLES_REDUCED = 1;
    /** The value for the particles setting to indicate no particles. */
    public static final int PARTICLES_NONE = 2;
    
    // --------The settings themselves--------
    
    // GUI scale
    /** The GUI scale setting. */
    private static int GUI_SCALE;
    /** The GUI scale config field name. */
    private static final String GUI_SCALE_NAME = "guiscale";
    /** The GUI scale default value. */
    private static final int GUI_SCALE_DEFAULT = GUI_SCALE_AUTO;
    /** The effective GUI scale. */
    private static int GUI_SCALE_EFFECTIVE;
    
    // Particles
    /** The particles setting. */
    private static int PARTICLES;
    /** The particles setting config field name. */
    private static final String PARTICLES_NAME = "particles";
    /** The default particles setting. */
    private static final int PARTICLES_DEFAULT = PARTICLES_ALL;
    
    // --------Misc--------
    
    /** Whether or not the settings have been set up. */
    private static boolean initialised = false;
    

    // non-instantiable
    private Settings() {}
    
    
    /**
     * Updates the settings.
     */
    public static void update() {
        GUI_SCALE_CHANGED = false;
        
        /*
        if(SCREEN.wasResized()) {
            int prevScale = GUI_SCALE_EFFECTIVE;
            GUI_SCALE_EFFECTIVE = getEffectiveGUIScale(GUI_SCALE);
            GUI_SCALE_CHANGED = GUI_SCALE_EFFECTIVE != prevScale;
        }
        */
    }
    
    /**
     * Initialises the settings.
     */
    public static void initialise() {
        if(!initialised) {
            setup();
            initialised = true;
        }
    }
    
    /**
     * Sets up the settings.
     */
    private static void setup() {
        if(!load()) {
            Log.get().postInfo("Game settings could not be loaded - resetting to default values!");
            setDefaults();
            save();
        }
    }
    
    /**
     * Sets all the settings to their default values.
     */
    public static void setDefaults() {
        GUI_SCALE = GUI_SCALE_DEFAULT;
        GUI_SCALE_EFFECTIVE = getEffectiveGUIScale(GUI_SCALE);
    }
    
    /**
     * Loads the settings config.
     * 
     * @return {@code true} if the settings were successfully loaded;
     * {@code false} otherwise.
     */
    public static boolean load() {
        ConfigFile config = new ConfigFile("settings");
        try {
            config.load();
        } catch(IOException e) {
            Log.get().postWarning("Could not load settings config!");
            return false;
        }
        
        boolean settingsChanged = false;
        boolean hasTag = true;
        
        // GUI scale setting
        if(config.hasTag(GUI_SCALE_NAME)) {
            GUI_SCALE = config.getInteger(GUI_SCALE_NAME);
            if(GUI_SCALE != GUI_SCALE_AUTO && GUI_SCALE != GUI_SCALE_SMALL && GUI_SCALE != GUI_SCALE_MEDIUM && GUI_SCALE != GUI_SCALE_LARGE)
                hasTag = false;
        } else {
            hasTag = false;
        }
        
        if(!hasTag) {
            GUI_SCALE = GUI_SCALE_DEFAULT;
            config.addInteger(GUI_SCALE_NAME, GUI_SCALE);
            settingsChanged = true;
        }
        GUI_SCALE_EFFECTIVE = getEffectiveGUIScale(GUI_SCALE);
        
        // Particles setting
        hasTag = true;
        if(config.hasTag(PARTICLES_NAME)) {
            PARTICLES = config.getInteger(PARTICLES_NAME);
            if(PARTICLES != PARTICLES_ALL && PARTICLES != PARTICLES_REDUCED && PARTICLES != PARTICLES_NONE)
                hasTag = false;
        } else {
            hasTag = false;
        }
        
        if(!hasTag) {
            PARTICLES = PARTICLES_DEFAULT;
            config.addInteger(PARTICLES_NAME, PARTICLES);
            settingsChanged = true;
        }
        
        // TODO: Load more settings here
        
        if(settingsChanged) {
            try {
                config.safeSave();
            } catch(IOException e) {
                Log.get().postSevere("Could not save settings config file after fixing loaded settings!");
            }
        }
        
        return true;
    }
    
    /**
     * Saves the settings config.
     */
    public static void save() {
        ConfigFile config = new ConfigFile("settings");
        
        config.addInteger(GUI_SCALE_NAME, GUI_SCALE);
        config.addInteger(PARTICLES_NAME, PARTICLES);
        
        try {
            config.safeSave();
        } catch(IOException e) {
            Log.get().postSevere("Could not save settings config file!");
        }
    }
    
    //--------------------==========--------------------
    //---------=====Getter/Setter Wrappers=====---------
    //--------------------==========--------------------
    
    /**
     * Gets a {@code Settings} instance.
     * 
     * @return A {@code Settings} instance.
     */
    public static Settings get() {
        return new Settings();
    }
    
    /**
     * Checks for whether or not the GUI scale was changed since the last tick.
     * 
     * @return {@code true} if the GUI scale was changed sine the last tick.
     */
    public static boolean wasGUIScaleChanged() {
        return GUI_SCALE_CHANGED;
    }
    
    /**
     * Gets the GUI scale setting.
     * 
     * @return The GUI scale setting.
     */
    public static int getSettingGUIScale() {
        return GUI_SCALE;
    }
    
    /**
     * Gets the effective GUI setting.
     * 
     * @return The effective GUI setting.
     */
    public static int getEffectiveGUIScale() {
        return GUI_SCALE_EFFECTIVE;
    }
    
    /**
     * Gets the effective GUI scale setting were the GUI scale setting the
     * given value.
     * 
     * @param guiScale The hypothetical value for the GUI scale setting.
     * 
     * @return The effective GUI scale setting, were the GUI scale setting the
     * given value.
     * @throws IllegalArgumentException Thrown if the given value is not a
     * valid scale option.
     */
    private static int getEffectiveGUIScale(int guiScale) {
        if(guiScale != GUI_SCALE_AUTO && guiScale != GUI_SCALE_SMALL && guiScale != GUI_SCALE_MEDIUM && guiScale != GUI_SCALE_LARGE)
            throw new IllegalArgumentException("Illegal GUI scale value!");
        
        if(guiScale != GUI_SCALE_AUTO)
            return guiScale;
        
        /*
        Screen screen = Screen.get();
        if(screen.getWidth() >= GUI_SCALE_LARGE_THRESHOLD_WIDTH && screen.getHeight() >= GUI_SCALE_LARGE_THRESHOLD_HEIGHT)
            return GUI_SCALE_LARGE;
        else if(screen.getWidth() >= GUI_SCALE_MEDIUM_THRESHOLD_WIDTH && screen.getHeight() >= GUI_SCALE_MEDIUM_THRESHOLD_HEIGHT)
            return GUI_SCALE_MEDIUM;
        else
            return GUI_SCALE_SMALL;
        */
        return 0;
    }
    
    /**
     * Sets the GUI scale setting.
     * 
     * @param guiScale The new GUI scaling setting.
     */
    public static void setSettingGUIScale(int guiScale) {
        if(guiScale != GUI_SCALE_AUTO && guiScale != GUI_SCALE_SMALL && guiScale != GUI_SCALE_MEDIUM && guiScale != GUI_SCALE_LARGE) {
            Log.get().postWarning("Attempting to set the GUI scaling setting to an invalid value!");
            return;
        }
        
        GUI_SCALE = guiScale;
        GUI_SCALE_CHANGED = true;
    }
    
    /**
     * Gets the particles setting. The returned value will either be
     * {@link #PARTICLES_ALL}, {@link #PARTICLES_REDUCED} or
     * {@link #PARTICLES_NONE}.
     * 
     * @return The particles setting.
     */
    public static int getSettingParticles() {
        return PARTICLES;
    }
    
    /**
     * Checks for whether or not the particles setting is that of the 'all
     * particles' value. An invocation of this is equivalent to:
     * </pre>
     * {@code Settings.getSettingParticles() == Settings.PARTICLES_ALL}
     * </pre>
     * 
     * @return {@code true} if the particles setting is set to 'all';
     * {@code false} otherwise.
     */
    public static boolean settingParticlesAll() {
        return PARTICLES == PARTICLES_ALL;
    }
    
    /**
     * Checks for whether or not the particles setting is that of the 'reduced
     * particles' value. An invocation of this is equivalent to:
     * </pre>
     * {@code Settings.getSettingParticles() == Settings.PARTICLES_REDUCED}
     * </pre>
     * 
     * @return {@code true} if the particles setting is set to 'reduced';
     * {@code false} otherwise.
     */
    public static boolean settingParticlesReduced() {
        return PARTICLES == PARTICLES_REDUCED;
    }
    
    /**
     * Checks for whether or not the particles setting is that of the 'no
     * particles' value. An invocation of this is equivalent to:
     * </pre>
     * {@code Settings.getSettingParticles() == Settings.PARTICLES_NONE}
     * </pre>
     * 
     * @return {@code true} if the particles setting is set to 'none';
     * {@code false} otherwise.
     */
    public static boolean settingParticlesNone() {
        return PARTICLES == PARTICLES_NONE;
    }
    
    /**
     * Sets the particles setting. If the given values is not equivalent to
     * either {@link #PARTICLES_ALL}, {@link #PARTICLES_REDUCED} or
     * {@link #PARTICLES_NONE}, the particles setting will not be changed.
     * 
     * @param particles The setting value.
     */
    public static void setSettingParticles(int particles) {
        if(particles != PARTICLES_ALL && particles != PARTICLES_REDUCED && particles != PARTICLES_NONE)
            Log.get().postWarning("Attempting to set the particles setting to an invalid value!");
        else
            PARTICLES = particles;
    }
    
}
