package com.stabilise.core;

import com.stabilise.core.state.MainMenuState;
import com.stabilise.entity.controller.PlayerController;
import com.stabilise.input.Controllable;
import com.stabilise.input.Controller;
import com.stabilise.input.Controller.Control;
import com.stabilise.opengl.render.HUDRenderer;
import com.stabilise.screen.menu.Menu;
import com.stabilise.screen.menu.PauseMenu;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.world.GameWorld;

/**
 * The game itself.
 */
public class Game implements Controllable {
	
	/** Whether or not the game is currently running. */
	public boolean running = true;
	/** Whether or not the game is currently paused. */
	public boolean paused = false;
	
	/** The game's world instance. */
	private final GameWorld world;
	
	/** The controller. */
	public Controller controller;
	/** The player controller. */
	public PlayerController playerController;
	
	/** The current active menu. */
	public Menu menu;
	
	/** A reference to the HUD renderer. TODO: Temporary */
	public HUDRenderer hudRenderer;
	
	/** Whether or not the debug display is active. */
	public boolean debug = false;
	
	/** The game profiler. */
	public Profiler profiler = new Profiler();
	/** The game's logging agent. */
	private final Log log = Log.getAgent("GAME");
	
	
	/**
	 * Creates a new Game instance.
	 * 
	 * @param world The world to run.
	 * 
	 * @throws RuntimeException Thrown if the world could not be loaded.
	 */
	public Game(GameWorld world) {
		this.world = world;
		
		log.postInfo("Initiating game...");
		
		// Handled by a separate thread in the main menu now
		//world.prepare();
		//world.addPlayer(CharacterData.defaultCharacter());
		
		controller = new Controller(this);
		
		// TODO: Hardcoding this is poor design and should be changed in the future
		playerController = new PlayerController(world.player, controller, this);
	}
	
	/**
	 * Updates the game - this should be called by the core game update loop.
	 */
	public void update() {
		/*
		if(world.loading) {
			if(world.regionsLoaded())
				world.loading = false;
			else
				return;
		}
		*/
		
		if(running) {
			try {
				profiler.start("menu");
				if(menu != null)
					menu.update();
				profiler.next("world");
				if(!paused)
					world.update();
				profiler.end();
			} catch(Exception e) {
				log.postSevere("Game encountered error!", e);
				Application a = Application.get();
				a.produceCrashLog();
				//close();			// Simply calling close() makes the game freeze
				a.setState(new MainMenuState());
				return;
			}
		}
	}
	
	/**
	 * Renders anything that isn't handled by a renderer.
	 */
	public void render() {
		profiler.start("menu");
		if(menu != null)
			menu.render();
		profiler.end();
	}
	
	/**
	 * Closes the game - that is, shuts down the world.
	 */
	public void close() {
		running = false;
		if(menu != null)
			menu.unloadResources();
		world.close();
	}
	
	/**
	 * Gets the game world.
	 * 
	 * @return The game world.
	 */
	public GameWorld getWorld() {
		return world;
	}
	
	/**
	 * Sets the current menu. If the {@code menu} parameter is {@code null},
	 * the current menu, if it exists, will be closed and the game will unpause
	 * as per an invocation of {@link #closeMenu()}, though directly invoking
	 * {@link #closeMenu()} is preferable.
	 * 
	 * @param menu The menu.
	 * @param pause Whether or not to pause the game.
	 */
	public void setMenu(Menu menu, boolean pause) {
		if(menu == null) {
			closeMenu();
		} else {
			this.menu = menu;
			paused = pause;
		}
	}
	
	/**
	 * Closes the current menu, if it is non-null. If the game is paused, it
	 * will resume.
	 */
	public void closeMenu() {
		if(menu != null)
			menu.unloadResources();
		menu = null;
		paused = false;
		controller.input.setFocus(controller);
	}
	
	/**
	 * Opens the pause menu.
	 */
	public void openPauseMenu() {
		setMenu(new PauseMenu(this), true);
		world.save();
	}
	
	@Override
	public void handleButtonPress(int button, int x, int y) {
		playerController.handleButtonPress(button, x, y);
	}
	
	@Override
	public void handleButtonRelease(int button, int x, int y) {
		playerController.handleButtonRelease(button, x, y);
	}
	
	@Override
	public void handleKeyPress(int key) {
		hudRenderer.setProfilerSection(InputManager.numericKeyValue(key));
	}
	
	@Override
	public void handleKeyRelease(int key) {
		
	}
	
	@Override
	public void handleControlPress(Control control) {
		switch(control) {
			case PAUSE:
				openPauseMenu();
				break;
			case DEBUG:
				debug = !debug;
				break;
			default:
				playerController.handleControlPress(control);
				break;
		}
	}
	
	@Override
	public void handleControlRelease(Control control) {
		playerController.handleControlRelease(control);
	}
	
	@Override
	public void handleMouseWheelScroll(int scroll) {
		playerController.handleMouseWheelScroll(scroll);
	}
	
}
