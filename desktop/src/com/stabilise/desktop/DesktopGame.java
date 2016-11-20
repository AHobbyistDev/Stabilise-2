package com.stabilise.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.stabilise.core.ResourcesRaw;
import com.stabilise.core.main.Stabilise;

public class DesktopGame {
    
    public static void main(String[] args) {
        ResourcesRaw.loadAllDependencies();
        
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.backgroundFPS = 5;
        config.foregroundFPS = 60;
        config.title = Stabilise.GAME_NAME + " - " + Stabilise.getGameSubtitle();
        config.width = 900;
        config.height = 600;
        config.vSyncEnabled = true;
        config.addIcon("icon128.png", FileType.Classpath);
        config.addIcon("icon32.png", FileType.Classpath);
        config.addIcon("icon16.png", FileType.Classpath);
        new LwjglApplication(new Stabilise(args).getListener(), config);
    }
    
}
