package com.stabilise.core.state;

import com.stabilise.screen.menu.MainMenu;

/**
 * The MainMenuState state is the state which will run while the player is
 * navigating the main menu.
 */
public class MainMenuState implements State {
	
	/** The main menu. */
	public MainMenu menu;
	
	
	/**
	 * Creates a new MainMenuState instance.
	 */
	public MainMenuState() {
		super();
	}
	
	@Override
	public void resize(int width, int height) {
		// meh
	}

	@Override
	public void update() {
		menu.update();
	}

	@Override
	public void render(float delta) {
		menu.render();
	}

	@Override
	public void start() {
		menu = new MainMenu();
	}
	
	@Override
	public void pause() {
		// nothing to see here, move along
	}

	@Override
	public void resume() {
		// nothing to see here, move along
	}

	@Override
	public void dispose() {
		menu.unloadResources();
	}
	
}
