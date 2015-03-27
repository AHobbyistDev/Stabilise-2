package com.stabilise.core.main;

import java.util.Random;

import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.Settings;
import com.stabilise.core.state.State;
import com.stabilise.input.Controller;
import com.stabilise.item.Item;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.tile.Tile;

/**
 * The game.
 */
public class Stabilise extends Application {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The game name. */
	public static final String GAME_NAME = "Stabilise II";
	
	/** Whether or not the game has bootstrapped. */
	private static boolean bootstrapped = false;
	
	
	/**
	 * Creates the game.
	 */
	public Stabilise() {
		super(Constants.TICKS_PER_SECOND);
		Log.setLogLevel(Constants.DEV_VERSION ? Log.Level.ALL : Log.Level.INFO);
		profiler.setResetOnFlush(false);
		driver.setTicksPerProfilerFlush(2);
	}
	
	@Override
	protected State getInitialState() {
		//return new com.stabilise.core.state.GDXTestState();
		return new com.stabilise.core.state.LoadingState();
	}
	
	@Override
	public void produceCrashLog() {
		Log.saveLog(true, GAME_NAME + " v" + Constants.VERSION);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets a randomised subtitle for the game window.
	 */
	public static String getGameSubtitle() {
		final String[] titles = {
				Constants.VERSION.toString(),
				"Coming soon to a computer near you!",
				"Look, a distraction!",
				"II esilibatS",
				"20% less bugs!",
				"20% more bugs!",
				"Stable release builds? Hah!",
				"[Insert witty title here]",
				"May contain traces of nuts",
				"Not suitable for children under the age of 84",
				"Take twice daily for fast, effective results!",
				"Batteries not included!",
				//"Made in China",
				"Now with excavators!",
				"(I lied about the excavators)",
				"No refunds!"
		};
		
		return titles[new Random().nextInt(titles.length)];
	}
	
	/**
	 * Loads into memory or otherwise sets up core components of the game. This
	 * should be invoked as the game is displaying its splash screen - i.e., as
	 * a {@link LoadingState} is the current state.
	 * 
	 * <p>Note that this should be invoked <i>as the application starts</i>, or
	 * a variety of problems may result if the application attempts to work
	 * with things which are yet to be initialised.
	 */
	@UserThread("LoaderThread")
	public static void bootstrap() {
		if(!bootstrapped) {
			try {
				Log.get().postDebug("Bootstrapping...");
				
				Tile.registerTiles();
				Item.registerItems();
				Dimension.registerDimensions();
				
				Settings.initialise();
				Controller.poke();
				
				bootstrapped = true;
				
				Log.get().postDebug("Bootstrap completed.");
			} catch(Throwable t) {
				Log.get().postSevere("Bootstrap failed!", t);
				throw new AssertionError(t);
			}
		} else {
			Log.get().postWarning("Already bootstrapped!");
		}
	}
	
}
