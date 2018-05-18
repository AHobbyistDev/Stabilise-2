package com.stabilise.core.main;

import java.util.Arrays;

import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.Settings;
import com.stabilise.core.state.State;
import com.stabilise.entity.component.Components;
import com.stabilise.input.Controller;
import com.stabilise.item.Item;
import com.stabilise.util.ArrayUtil;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.dimension.Dimension;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * The game.
 */
public class Stabilise extends Application {
    
    /** The game name. */
    public static final String GAME_NAME = "Stabilise II";
    
    /** Whether or not the game has bootstrapped. */
    private static boolean bootstrapped = false;
    
    
    /**
     * Creates the game.
     * 
     * @param args Command-line arguments.
     */
    public Stabilise(String[] args) {
        super(Constants.TICKS_PER_SECOND);
        Log.setLogLevel(Constants.DEV_VERSION ? Log.Level.ALL : Log.Level.INFO);
        profiler.setResetOnFlush(true);
        driver.setTicksPerProfilerFlush(2 * Constants.TICKS_PER_SECOND);
        
        Log.get().postInfo("Program args: " + Arrays.toString(args));
    }
    
    @Override
    protected void init() {
        Log.get().postInfo("Starting game: " + GAME_NAME + " " + Constants.VERSION);
    }
    
    @Override
    protected State getInitialState() {
        //return new com.stabilise.core.state.MenuTestState();
        return new com.stabilise.core.state.LoadingState();
        //return new com.stabilise.core.state.MainMenuState();
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
        return ArrayUtil.random(
                Constants.VERSION.toString(),
                "Coming soon to a computer near you!",
                "II esilibatS",
                "20% less bugs!",
                "20% more bugs!",
                "[Insert witty subtitle here]",
                "May contain traces of nuts",
                "Not suitable for children under the age of 84",
                "Take twice daily for fast, effective results!",
                "Recommended by 9/10 doctors",
                "Batteries not included!",
                "Now with excavators!",
                "No refunds!",
                "Not even in early access yet",
                "In perpetual development",
                "Has no content!",
                "Barely functional"
        );
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
                
                Settings.initialise(); // needs to go before dimensions for now
                
                Tile.registerTiles();
                Item.registerItems();
                Dimension.registerDimensions();
                Components.registerComponentTypes();
                TileEntity.poke();
                
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
