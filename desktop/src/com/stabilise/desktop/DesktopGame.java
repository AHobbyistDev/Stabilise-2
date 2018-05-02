package com.stabilise.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.stabilise.core.ResourcesRaw;
import com.stabilise.core.main.Stabilise;

public class DesktopGame {
    
    public static void main(String[] args) {
        ResourcesRaw.loadAllDependencies();
        
        // startLwjgl2(args);
        startLwjgl3(args);
    }
    
    public static void startLwjgl2(String[] args) {
        /*
    	LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.backgroundFPS = 5;
        config.foregroundFPS = 60;
        config.title = Stabilise.GAME_NAME + " - " + Stabilise.getGameSubtitle();
        config.width = 900;
        config.height = 600;
        config.vSyncEnabled = true;
        
        //LwjglApplicationConfiguration.disableAudio = true;
        
        config.addIcon("icon128.png", FileType.Classpath);
        config.addIcon("icon32.png", FileType.Classpath);
        config.addIcon("icon16.png", FileType.Classpath);
        new LwjglApplication(new Stabilise(args).getListener(), config);
        */
    }
    
    public static void startLwjgl3(String[] args) {
    	Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    	config.setTitle(Stabilise.GAME_NAME + " - " + Stabilise.getGameSubtitle());
    	config.setWindowedMode(900, 600);
    	//config.useVsync(true);
    	config.setIdleFPS(5);
    	
    	// Can't set max fps here; use the AppDriver in Stabilie instead.
    	
    	config.setWindowIcon(FileType.Classpath, "icon128.png", "icon32.png", "icon16.png");
    	new Lwjgl3Application(new Stabilise(args).getListener(), config);
    }
    
}
