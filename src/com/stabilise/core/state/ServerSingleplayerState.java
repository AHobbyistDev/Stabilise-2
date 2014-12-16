package com.stabilise.core.state;

import com.stabilise.core.GameClient;
import com.stabilise.core.GameServer;
import com.stabilise.opengl.render.WorldRenderer;
//import com.stabilise.screen.menu.GameMenu;
//import com.stabilise.util.MathUtil;

/**
 * The SingleplayerState is the state which will run while singleplayer
 * adventure mode is running.
 * 
 * @deprecated Due to the removal of networking architecture. 
 */
public class ServerSingleplayerState implements State {
	
	/** The name of the world being played. */
	public String worldName;
	
	/** The game server. */
	public GameServer server;
	/** The game client. */
	public GameClient client;
	
	/** The renderer to use for rendering the world. */
	public WorldRenderer renderer;
	
	/** The in-game HUD menu. */
	//public GameMenu menu;
	
	
	/**
	 * Creates a new SingleplayerState instance.
	 * 
	 * @param worldName The name of the world to play.
	 */
	public ServerSingleplayerState(String worldName) {
		super();
		
		this.worldName = worldName;
	}
	
	@Override
	public void resize(int width, int height) {
		// meh
	}

	@Override
	public void update() {
		client.update();
		
		renderer.update();
		//----renderer.tileRenderer.setPlayerCoords(client.getWorld().player.x, client.getWorld().player.y);
		
		//menu.update();
		
		/*
		if(menu.actionFlag == -1) {
			return;
		} else {
			switch(menu.actionFlag) {
				case GameMenu.ACTION_MENU:
					// As stop() is called to clean up the state when the game
					// state is changed, there is no need to invoke stopGame()
					//stopGame();
					application.setState(new MainMenuState());
					break;
				case GameMenu.ACTION_PAUSE:
					client.togglePause();
					break;
				case GameMenu.ACTION_ZOOM_IN:
					//renderer.changeScale(1.1f);
					break;
				case GameMenu.ACTION_ZOOM_OUT:
					//renderer.changeScale(0.9f);
					break;
				case GameMenu.ACTION_QUIT:
					//stopGame();
					application.stop();
					break;
			}
			menu.actionFlag = -1;
		}
		*/
	}
	
	/**
	 * Stops the game.
	 */
	public void stopGame() {
		client.close();
		server.shutdown();
	}
	
	@Override
	public void render(float delta) {
		renderer.render();
		
		//menu.render();
		
		/*
		debugFont.drawLines(new String[] {
				"FPS: " + screen.getFPS() + " (" + screen.getMaxFPS() + ")",
				"x: " + MathUtil.abbreviateDecimalPlaces(client.getWorld().player.x, 2),
				"y: " + MathUtil.abbreviateDecimalPlaces(client.getWorld().player.y, 2)
		}, 0, screen.getHeight() - debugFont.getSpriteHeight());
		*/
	}
	
	@Override
	public void start() {
		//menu = new GameMenu();
		
		server = new GameServer(worldName, true, 8);
		client = new GameClient();
		client.joinServer();
		
		//renderer = new WorldRenderer(screen, client.getWorld());
	}
	
	@Override
	public void dispose() {
		stopGame();
		renderer.unloadResources();
		renderer = null;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

}
