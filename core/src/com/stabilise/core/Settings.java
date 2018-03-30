package com.stabilise.core;

import java.io.IOException;

import com.stabilise.util.Config;
import com.stabilise.util.Log;
import com.stabilise.util.io.data.CompoundBuilder;

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
    
    /** The value for the particles setting to indicate all particles. */
    public static final int PARTICLES_ALL = 0;
    /** The value for the particles setting to indicate reduced particles. */
    public static final int PARTICLES_REDUCED = 1;
    /** The value for the particles setting to indicate no particles. */
    public static final int PARTICLES_NONE = 2;
    
    
    /** Whether or not the settings have been set up. */
    private static boolean initialised = false;
    
    
    private static final Config config = new Config(
            new CompoundBuilder(Config.CONFIG_FORMAT)
                .put("particles", PARTICLES_ALL)
                .get(),
            Resources.DIR_CONFIG.child("settings.txt")
    );
    

    // non-instantiable
    private Settings() {}
    
    
    /**
     * Initialises the settings.
     */
    public static void initialise() {
        if(!initialised) {
            load();
            initialised = true;
        }
    }
    
    /**
     * Resets all the settings to their default values.
     */
    public static void resetToDefaults() {
        config.resetToDefaults();
    }
    
    /**
     * Loads the settings config.
     */
    public static void load() {
        boolean changes = false;
        try {
            changes = config.load();
        } catch(IOException e) {
            changes = true;
            Log.get().postWarning("Could not load settings config: " + e.getMessage());
        }
        
        int particles = config.values.getInt("particles");
        if(particles < PARTICLES_ALL || particles > PARTICLES_NONE) {
            config.reset("particles");
            changes = true;
        }
        
        if(changes) {
            try {
                config.save();
            } catch(IOException e) {
                Log.get().postSevere("Could not save settings config file after "
                        + "fixing loaded settings: " + e.getMessage());
            }
        }
    }
    
    /**
     * Saves the settings config.
     */
    public static void save() {
        try {
            config.save();
        } catch(IOException e) {
            Log.get().postSevere("Could not save settings config file!");
        }
    }
    
    //--------------------==========--------------------
    //---------=====Getter/Setter Wrappers=====---------
    //--------------------==========--------------------

    
    /**
     * Gets the particles setting. The returned value will either be
     * {@link #PARTICLES_ALL}, {@link #PARTICLES_REDUCED} or
     * {@link #PARTICLES_NONE}.
     * 
     * @return The particles setting.
     */
    public static int getSettingParticles() {
        return config.values.getInt("particles");
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
        return getSettingParticles() == PARTICLES_ALL;
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
        return getSettingParticles() == PARTICLES_REDUCED;
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
        return getSettingParticles() == PARTICLES_NONE;
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
            config.values.put("particles", particles);
    }
    
    /**
     * True if overworld should be the default dimension; false otherwise.
     */
    public static boolean getOverworldDefault() {
        return config.values.getBool("overworldDefault");
    }
    
}
