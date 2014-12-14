package com.stabilise.core.main;

import java.util.Random;

import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.Settings;
import com.stabilise.core.state.LoadingState;
import com.stabilise.core.state.State;
import com.stabilise.input.Controller;
import com.stabilise.item.Item;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
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
	}
	
	@Override
	protected State getInitialState() {
		return new LoadingState();
	}
	
	@Override
	public void produceCrashLog() {
		Log.saveLog(true, GAME_NAME + " v" + Constants.VERSION);
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets a randomised subtitle for the application window.
	 */
	@SuppressWarnings("unused")
	private static String getApplicationSubtitle() {
		final String[] titles = {
				Constants.VERSION,
				"Brought to you by Java!",
				"Coming soon to a computer near you!",
				//"All of the renderings!",
				//"(Immediate rendering not included)",
				"Look, a distraction!",
				"II esilibatS",
				//"Now with menus!",
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
				//"Ignore the middle bar",
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
				Tile.registerTiles();
				Item.registerItems();
				
				// Initialises these classes - that is, loads them into memory
				// and performs any static blocks of code
				Class.forName("com.stabilise.world.tile.Tiles");		
				Class.forName("com.stabilise.item.Items");
				
				Settings.initialise();
				Controller.initialise();
				
				bootstrapped = true;
			} catch(Throwable t) {
				// JRE 6 doesn't like the arguments (String, Throwable)
				throw new AssertionError(t);
			}
		}
	}
	
}
