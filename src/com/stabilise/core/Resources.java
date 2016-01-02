package com.stabilise.core;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.main.LauncherLauncher;

/**
 * Holds a bunch of static FileHandles pertaining to application resources.
 */
public class Resources {
    
    public static final FileHandle
            /** The application's root working directory. */
            DIR_APP = getApplicationPath("stabilise"),
            
            /** The config file directory. */
            DIR_CONFIG = DIR_APP.child("config/"),
            
            /** Root directory for save data. */
            DIR_SAVES = DIR_APP.child("saves/"),
            DIR_CHARS = DIR_SAVES.child("chars/"),
            DIR_WORLDS = DIR_SAVES.child("worlds/"),
            
            /** Root directory for application resources e.g. images, sounds. */
            DIR_RESOURCES = DIR_APP.child("res/"),
            DIR_IMG = DIR_RESOURCES.child("img/"),
            DIR_FONT = DIR_RESOURCES.child("fonts/"),
            DIR_SOUND = DIR_RESOURCES.child("sound/"),
            
            /** The file directory for mods. */
            DIR_MODS = DIR_APP.child("mods/"),
            
            /** The directory in which console output logs should be saved. */
            DIR_LOG = DIR_APP.child("log/"),
            
            /** Where the game data is stored. */
            DIR_GAMEDATA = DIR_APP.child("gamedata/"),
            
            /** Directory where update server files are stored. Used by update
             * server only. */
            DIR_UPDATE_SERVER = DIR_APP.child("updatefiles/"),
            US_GAME_JAR = DIR_UPDATE_SERVER.child("Game.jar"),
            US_LAUNCHER_JAR = DIR_UPDATE_SERVER.child("Launcher.jar"),
            US_GAMEFILES = DIR_UPDATE_SERVER.child("gamefiles.zip"),
            
            /** The location of the game .jar */
            GAME_JAR = DIR_GAMEDATA.child("Game.jar"),
            LAUNCHER_JAR = DIR_GAMEDATA.child("Launcher.jar"),
            LAUNCHER_FILES = DIR_GAMEDATA.child(LauncherLauncher.LAUNCHER_FILES);
    
    /** The fully-qualified classpath of the true launcher class. */
    public static final String LAUNCHER_CLASS = LauncherLauncher.LAUNCHER_CLASS;
    
    /** Destination .zip for received gamefiles. */
    public static final FileHandle GAMEFILES_DEST = DIR_GAMEDATA.child("gamefiles.zip");
    /** Directories encapsulated by {@link #US_GAMEFILES} and {@link #GAMEFILES_DEST}. */
    public static final FileHandle[] GAMEFILES_DIRS = { DIR_RESOURCES, DIR_CONFIG };
    
    
    /**
     * Finds and returns the main directory for the application.
     * 
     * @param appName The name of the application.
     * 
     * @return The {@code File} representing the main application directory.
     * @throws NullPointerException if {@code appName} is {@code null}.
     */
    private static FileHandle getApplicationPath(String appName) {
        appName = "." + appName + "/";
        String dir = System.getProperty("user.home", ".");
        String os = System.getProperty("os.name").toLowerCase();
        File appDir = null;
        
        if(os.contains("windows")) {
            String appDataDir = System.getenv("APPDATA");
            if(appDataDir != null)
                appDir = new File(appDataDir, appName);
            else
                appDir = new File(dir, appName);
        } else if(os.contains("mac")) {
            appDir = new File(dir, "Library/Application Support/" + appName);
        } else if(os.contains("linux")) {
            appDir = new File(dir, appName);
        } else {
            throw new InternalError("OS not supported");
        }
        
        //return Gdx.files.external(appDir.getPath());
        return new FileHandle(appDir);
    }
    
    
    // non-instantiable
    private Resources() {}
    
}
