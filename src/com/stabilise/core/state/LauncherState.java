package com.stabilise.core.state;

import java.io.IOException;

import com.stabilise.core.Resources;
import com.stabilise.core.app.Application;


public class LauncherState implements State {
    
    private static final String javaPath
            = "\"" + System.getProperty("java.home").replace('\\', '/') + "/bin/javaw\"";
    private static final String updater = Resources.DIR_APP.child(Resources.UPDATER_JAR).path();
    private static final String game = Resources.DIR_APP.child(Resources.GAME_JAR).path();
    
    enum State {
        starting, updating, running;
    }
    
    private State state = State.starting;
    private Process curProcess = null;
    
    @Override
    public void start() {}
    
    @Override
    public void predispose() {}
    
    @Override
    public void dispose() {
        if(state != State.running && curProcess!= null && curProcess.isAlive())
            curProcess.destroy();
    }
    
    @Override
    public void resize(int width, int height) {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void update() {
        if(state == State.starting) {
            System.out.println("Running updater...");
            ProcessBuilder pb = new ProcessBuilder(javaPath + " -jar" + " \"" + updater + "\"");
            pb.directory(Resources.DIR_APP.file());
            pb.inheritIO();
            try {
                curProcess = pb.start();
            } catch(IOException e) {
                System.out.println("Failed to start updater!");
                e.printStackTrace();
                Application.get().shutdown();
            }
            state = State.updating;
        } else if(state == State.updating) {
            if(!curProcess.isAlive()) {
                System.out.println("Updater complete");
                System.out.println("Starting game...");
                ProcessBuilder pb = new ProcessBuilder(javaPath + " -jar" + " \"" + game + "\"");
                pb.directory(Resources.DIR_APP.file());
                pb.inheritIO();
                try {
                    curProcess = pb.start();
                } catch(IOException e) {
                    System.out.println("Failed to start game!");
                    e.printStackTrace();
                }
                state = State.running;
                // Game started = launcher no longer needed
                Application.get().shutdown();
            }
        }
    }
    
    @Override
    public void render(float delta) {
        
    }
    
}
