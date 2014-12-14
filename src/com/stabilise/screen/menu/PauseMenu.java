package com.stabilise.screen.menu;

import com.stabilise.core.Application;
import com.stabilise.core.Game;
import com.stabilise.core.state.MainMenuState;
import com.stabilise.opengl.Rectangle;
import com.stabilise.screen.menu.submenu.HowToPlayMenu;
import com.stabilise.screen.menu.submenu.PauseMenuMain;
import com.stabilise.util.Colour;
import com.stabilise.util.annotation.UsesApplication;

/**
 * The in-game pause menu.
 */
@UsesApplication
public class PauseMenu extends SubMenuBasedMenu {
	
	/** A reference to the game. */
	private Game game;
	
	/** The background. */
	private Rectangle background;
	
	
	/**
	 * Creates a new PauseMenu.
	 */
	public PauseMenu(Game game) {
		super();
		
		this.game = game;
		
		setSubMenu(new PauseMenuMain(this));
		//setSubMenu(PauseMenuMain.class);
	}
	
	@Override
	protected void loadResources() {
		background = new Rectangle();
		background.fill(Colour.BLACK, 0.5f);
	}
	
	@Override
	public void update() {
		if(submenu.action == PauseMenuMain.ACTION_RETURN_TO_GAME) {
			game.closeMenu();
		} else if(submenu.action == PauseMenuMain.ACTION_HOW_TO_PLAY) {
			setSubMenu(new HowToPlayMenu(this));
		} else if(submenu.action == PauseMenuMain.ACTION_RETURN_TO_MENU) {
			Application.get().setState(new MainMenuState());
		} else if(submenu.action == PauseMenuMain.ACTION_QUIT_GAME) {
			Application.get().shutdown();
		} else if(submenu.action == HowToPlayMenu.ACTION_EXIT) {
			setSubMenu(new PauseMenuMain(this, new Integer(1)));
		}
		
		super.update();
	}
	
	@Override
	protected void rescale(int width, int height) {
		super.rescale(width, height);
		
		background.setSize(width, height);
	}
	
	@Override
	public void render() {
		background.draw();
		
		super.render();
	}
	
	@Override
	public void unloadResources() {
		super.unloadResources();
		
		background.destroy();
	}
	
}
