package com.stabilise.core.main;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;

/**
 * The game installer. To be packaged with a "files" folder that contains
 * everything that needs to be copied to DIR_APP. Also copies a launcher.
 */
public class Installer {
    
    public static void main(String[] args) {
        System.out.println("Copying game files to game directory at " + Resources.DIR_APP);
        new FileHandle("files/").copyTo(Resources.DIR_APP);
        System.out.println("Files successfully copied");
        Resources.DIR_APP.child(Resources.LAUNCHER_JAR).copyTo(
                new FileHandle("Stabilise Launcher.jar")
        );
    }
    
}
