package com.stabilise.core.state;

import java.util.concurrent.ExecutionException;

import com.stabilise.core.Application;
import com.stabilise.core.main.Stabilise;
import com.stabilise.opengl.Font;
import com.stabilise.opengl.FontStyle;
import com.stabilise.opengl.Sprite;
import com.stabilise.util.Colour;
import com.stabilise.util.Log;
import com.stabilise.util.concurrent.TaskThread;
import com.stabilise.util.concurrent.TexturePreloaderTask;

/**
 * A LoadingState is the state which runs as the game loads all preparatory
 * resources. It displays a simple loading screen while the resources are being
 * loaded on a separate thread. As the thread will die before the application
 * moves onto anything else, all setup processes should sync up with the main
 * thread, and hence concurrency errors should not emerge from this.
 */
public class LoadingState implements State {
	
	/** A reference to the application. */
	private Application application;
	/** A reference to the screen. */
	//private Screen screen;
	
	/** The splash-screen sprite. */
	private Sprite splash;
	/** The loading text font. */
	private Font font1;
	/** The loading text font style. */
	@SuppressWarnings("unused")
	private FontStyle style;
	
	/** The last percentage which was displayed. */
	private int lastPercent = 0;
	/** The current loading string. */
	@SuppressWarnings("unused")
	private String text;
	
	/** The loader thread. */
	private TaskThread taskThread;
	
	
	/**
	 * Creates the loading state.
	 */
	public LoadingState() {
		application = Application.get();
		//screen = Screen.get();
	}
	
	@Override
	public void resize(int width, int height) {
		// meh
	}
	
	@Override
	public void update() {
		/*
		if(screen.wasResized()) {
			splash.x = screen.getCentreX();
			splash.y = screen.getCentreY();
		}
		*/
		
		int percent = (int)(100*taskThread.percentComplete());
		if(lastPercent != percent) {
			lastPercent = percent;
			text = taskThread.getTaskName() + "... " + percent + "%";
		}
		
		if(taskThread.stopped()) {
			Throwable t = taskThread.getThrowable();
			if(!taskThread.completed())
				Application.crashApplication(t != null ? t : new AssertionError("Bootstrap failed!"));
			((TexturePreloaderTask)taskThread.task).uploadTextures();
			application.setState(new MainMenuState());
		}
	}
	
	@Override
	public void render(float delta) {
		splash.draw();
		
		//font1.drawLine(text, screen.getCentreX(), screen.getCentreY() - 200, style);
	}
	
	@Override
	public void start() {
		splash = new Sprite("loading");
		splash.setPivot(splash.getWidth() / 2, splash.getHeight() / 2);
		//splash.x = screen.getCentreX();
		//splash.y = screen.getCentreY();
		
		font1 = new Font("sheets/font1", this);
		style = new FontStyle(16, Colour.BLACK, FontStyle.Alignment.CENTRE, 1, 0);
		
		text = "Loading... 0%";
		
		TexturePreloaderTask task = new TexturePreloaderTask(1) {
			@Override
			protected void execute() throws Exception {
				//setName("Bootstrapping");
				Stabilise.bootstrap();
				tracker.increment();
				//setName("Loading");
				super.execute();
			}
		};
		task.loadTextures(new String[] {"mainbg", "mainbgtile", "stickfigure", "sheets/cloak", "head", "button", "sheets/font1"});
		
		taskThread = new TaskThread(task);
		taskThread.setName("Preloader Thread");
		taskThread.start();
	}
	
	@Override
	public void pause() {
		// We're not going to be halting the startup screen, so no
	}
	
	@Override
	public void resume() {
		// See halt()
	}
	
	@Override
	public void dispose() {
		splash.destroy();
		font1.destroy();
		
		taskThread.cancel();
		try {
			taskThread.waitUninterruptibly();
		} catch(ExecutionException e) {
			Log.get().postSevere("Load task is a derp", e);
		}
	}

	@Override
	public void predispose() {
		// TODO Auto-generated method stub
		
	}
	
}
