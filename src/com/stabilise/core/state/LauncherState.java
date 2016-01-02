package com.stabilise.core.state;

import com.stabilise.core.UpdateClient;


public class LauncherState implements State {
    
    private UpdateClient client;
    
    
    public LauncherState() {
        
    }
    
    @Override
    public void start() {
        client = new UpdateClient();
        
        int maxAttempts = 10;
        int attempts = 0;
        do {
            attempts++;
            System.out.println("Connecting to update server... (attempt " + attempts + ")");
            client.connect();
            try {
                if(attempts != 1)
                    Thread.sleep(100L);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        } while(!client.isConnected() && attempts < maxAttempts);
        
        if(!client.isConnected()) {
            System.out.println("Could not connect to update server... aborting");
        }
    }
    
    @Override
    public void predispose() {
        
    }
    
    @Override
    public void dispose() {
        
    }
    
    @Override
    public void resize(int width, int height) {
        
    }
    
    @Override
    public void pause() {
        
    }
    
    @Override
    public void resume() {
        
    }
    
    @Override
    public void update() {
        
    }
    
    @Override
    public void render(float delta) {
        
    }
    
}
