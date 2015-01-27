package com.stabilise.core.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.stabilise.core.Application;
import com.stabilise.core.Game;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Profiler;
import com.stabilise.world.ClientWorld;
import com.stabilise.world.SingleplayerWorld;

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
	 * @param world The world on which to play.
	 */
	public SingleplayerState(ClientWorld<SingleplayerWorld> world) {
		super();
		
		game = new Game(world);
	}
	
	@Override
	public void start() {
		InputMultiplexer input = new InputMultiplexer();
		input.addProcessor(game);
		Gdx.input.setInputProcessor(input);
		
		renderer = new WorldRenderer(game, game.getWorld());
		//game.hudRenderer = renderer.hudRenderer;
		
		profiler.enable();
	}
	
	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);
	}
	
	@Override
	public void predispose() {
		
	}
	
	@Override
	public void dispose() {
		//((InputMultiplexer)Gdx.input).removeProcessor(game);
		
		game.close();
		renderer.unloadResources();
		renderer = null;
		
		profiler.disable();
		
		// Try to garbage collect everything which has been unloaded
		//game = null;		// <-- No-can-do, crashes in update() at if(!game.running)
		System.gc();
	}
	
	@Override
	public void pause() {
		//if(game.menu == null)
		//	game.openPauseMenu();
	}
	
	@Override
	public void resume() {
		// nothing to see here, move along
	}
	
	@Override
	public void update() {
		profiler.verify(2, "root.update");
		
		profiler.start("game"); // root.update.game
		game.update();
		
		profiler.verify(3, "root.update.game");
		
		// Safety net to prevent an NPE from the renderer if the game shuts
		// down this tick
		if(!game.running) {
			profiler.end(); // root.update
			return;
		}
		
		profiler.next("renderer"); // root.update.renderer
		renderer.update();
		profiler.end(); // root.update
		
		profiler.verify(2, "root.update");
	}
	
	@Override
	public void render(float delta) {
		renderer.render();
		game.render();
	}
	
}
