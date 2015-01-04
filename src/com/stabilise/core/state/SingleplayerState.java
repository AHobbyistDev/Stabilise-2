package com.stabilise.core.state;

import com.stabilise.core.Application;
import com.stabilise.core.Game;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Profiler;
import com.stabilise.world.GameWorld;

/**
 * The SingleplayerState is the state which will run while singleplayer
 * adventure mode is running.
 */
public class SingleplayerState implements State {
	
	/** The name of the world being played. */
	public String worldName;
	
	/** The game. */
	public Game game;
	
	/** The renderer to use for rendering the world. */
	public WorldRenderer renderer;
	
	/** The profiler. */
	public Profiler profiler = Application.get().profiler;
	
	
	/**
	 * Creates a new SingleplayerState instance.
	 * 
	 * @param worldInfo The info of the world to play.
	 */
	public SingleplayerState(GameWorld world) {
		super();
		
		game = new Game(world);
	}
	
	@Override
	public void resize(int width, int height) {
		// meh
	}

	@Override
	public void update() {
		profiler.start("game");
		game.update();
		
		// Safety net to prevent an NPE from the renderer if the game shuts
		// down this tick
		if(!game.running) {
			profiler.end();
			return;
		}
		
		profiler.next("renderer");
		renderer.update();
		profiler.end();
	}
	
	@Override
	public void render(float delta) {
		renderer.render();
		game.render();
	}
	
	@Override
	public void start() {
		profiler.enable();
		renderer = new WorldRenderer(game, game.getWorld());
		game.hudRenderer = renderer.hudRenderer;
	}
	
	@Override
	public void pause() {
		if(game.menu == null)
			game.openPauseMenu();
	}
	
	@Override
	public void resume() {
		// nothing to see here, move along
	}
	
	@Override
	public void dispose() {
		game.close();
		renderer.unloadResources();
		renderer = null;
		
		profiler.disable();
		
		// Try to garbage collect everything which has been unloaded
		//game = null;		// <-- No-can-do, crashes in update() at if(!game.running)
		System.gc();
	}

	@Override
	public void predispose() {
		// TODO Auto-generated method stub
		
	}

}
