package com.stabilise.core.state;

import com.stabilise.screen.menugdx.MainMenu;

/**
 * The MainMenuState state is the state which will run while the player is
 * navigating the main menu.
 */
public class MainMenuState implements State {
    
    private MainMenu menu;
    
    /**
     * Creates a new MainMenuState instance.
     */
    public MainMenuState() {
        super();
    }
    
    @Override
    public void start() {
        menu = new MainMenu();
        menu.show();
    }
    
    @Override
    public void predispose() {
        
    }
    
    @Override
    public void dispose() {
        menu.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        menu.resize(width, height);
    }
    
    @Override
    public void pause() {
        menu.pause();
    }
    
    @Override
    public void resume() {
        menu.resume();
    }
    
    @Override
    public void update() {
        
    }
    
    @Override
    public void render(float delta) {
        menu.render(delta);
    }
    
}
