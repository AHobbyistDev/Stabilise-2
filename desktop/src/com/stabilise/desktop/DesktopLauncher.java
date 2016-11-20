package com.stabilise.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.stabilise.core.main.Launcher;
import com.stabilise.core.main.Stabilise;

public class DesktopLauncher {
    
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.backgroundFPS = 30;
        config.foregroundFPS = 60;
        config.title = Stabilise.GAME_NAME + " Launcher";
        config.width = 900;
        config.height = 600;
        config.vSyncEnabled = true;
        config.addIcon("icon128.png", FileType.Classpath);
        config.addIcon("icon32.png", FileType.Classpath);
        config.addIcon("icon16.png", FileType.Classpath);
        new LwjglApplication(new Launcher().getListener(), config);
    }
    
}
