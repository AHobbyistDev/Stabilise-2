package com.stabilise.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;

/**
 * Holds a bunch of static FileHandles pertaining to application resources.
 */
public class Resources {
    
    public static final FileHandle
            /** The application's root working directory. */
            DIR_APP = new FileHandle(ResourcesRaw.DIR_APP),
            
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
            
            /** Where all dependencies are stored. */
            DIR_LIB = DIR_APP.child("lib/"),
            
            /** Directory where update server files are stored. Used by update
             * server only. */
            DIR_UPDATE_SERVER = DIR_APP.child("updatefiles/"),
            US_GAME_JAR = DIR_UPDATE_SERVER.child("gamedata/Game.jar"),
            US_LAUNCHER_JAR = DIR_UPDATE_SERVER.child("gamedata/Launcher.jar"),
            US_GAMEFILES = DIR_UPDATE_SERVER.child("gamedata/gamefiles.zip");
    
    /** Destination .zip for received gamefiles. */
    public static final FileHandle GAMEFILES_DEST = DIR_GAMEDATA.child("gamefiles.zip");
    /** Directories encapsulated by {@link #US_GAMEFILES} and {@link #GAMEFILES_DEST}. */
    public static final FileHandle[] GAMEFILES_DIRS = { DIR_RESOURCES, DIR_CONFIG };
    
    /** The root application directory ({@link #DIR_APP}) as a URI. */
    private static final URI ROOT_MAIN = DIR_APP.file().toURI();
    private static final URI ROOT_US = DIR_UPDATE_SERVER.file().toURI();
    
    /** Relativised gamefiles paths. */
    public static final String
            GAME_JAR      = "gamedata/Game.jar",
            LAUNCHER_JAR  = "gamedata/Launcher.jar",
            UPDATER_JAR   = "gamedata/Updater.jar",
            GAMEFILES_ZIP = "gamedata/gamefiles.zip",
            ROOT_DIR      = "";
    
    public static final Map<String, String> UNZIP_MAP = new HashMap<>();
    
    static {
        UNZIP_MAP.put(GAMEFILES_ZIP, ROOT_DIR);
    }
    
    
    /**
     * Gets the path of the specified file relative to the {@link #DIR_APP
     * application directory}.
     */
    public static String relativisePath(FileHandle file) {
        return ROOT_MAIN.relativize(file.file().toURI()).getPath();
    }
    
    /**
     * Gets the path of the specified file relative to the {@link
     * #DIR_UPDATE_SERVER update server's directory}.
     */
    public static String relativiseUpdateServer(FileHandle file) {
        return ROOT_US.relativize(file.file().toURI()).getPath();
    }
    
}
