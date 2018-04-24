package com.stabilise.core;

import java.io.File;

import com.stabilise.util.io.Dependencies;

/**
 * Dependency-less lightweight alternative to the Resource class.
 */
public class ResourcesRaw {
    
    public static final File DIR_APP = getApplicationPath("stabilise");
    public static final File DIR_LIB = new File(DIR_APP, "lib/");
    
    
    
    /** true if all dependencies are packaged in this .jar; false if they need
     * to be loaded. */
    private static final boolean PACKAGED_DEPENDENCIES = true;
    
    /**
     * Finds and returns the main directory for the application.
     * 
     * @param appName The name of the application.
     * 
     * @return The {@code File} representing the main application directory.
     * @throws NullPointerException if {@code appName} is {@code null}.
     */
    private static File getApplicationPath(String appName) {
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
        return appDir;
    }
    
    /**
     * Loads all dependencies (.jar files located in {@link #DIR_LIB}).
     */
    public static void loadAllDependencies() {
        if(!PACKAGED_DEPENDENCIES)
            Dependencies.loadAllDependencies(DIR_LIB);
    }
    
    
    private ResourcesRaw() {} // non-instantiable
    
}
