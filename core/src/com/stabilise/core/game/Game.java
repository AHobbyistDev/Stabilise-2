package com.stabilise.core.game;

import static com.badlogic.gdx.Input.Keys;

import com.badlogic.gdx.InputProcessor;
import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.main.Stabilise;
import com.stabilise.core.state.MainMenuState;
import com.stabilise.entity.Entity;
import com.stabilise.entity.component.controller.PlayerController;
import com.stabilise.input.Controllable;
import com.stabilise.input.Controller;
import com.stabilise.input.Controller.Control;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Log;
import com.stabilise.util.Profiler;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Worlds.WorldBundle;
import com.stabilise.world.multiverse.HostMultiverse;
import com.stabilise.world.multiverse.HostMultiverse.PlayerData;

/**
 * The game itself.
 */
public class Game implements Controllable, InputProcessor {
    
    /** Whether or not the game is currently running. */
    public boolean running = true;
    /** Whether or not the game is currently paused. */
    public boolean paused = false;
    
    /** Tracks the number of 'elapsed' ticks. Increments even if the game is
     * paused. */
    public long ticks = 0;
    /** True if a single game tick should be run despite the game being paused. */
    private boolean advanceTick = false;
    
    private final HostMultiverse provider;
    /** The game's world instance. */
    public final HostWorld world;
    public final PlayerData playerData;
    public final Entity player;
    
    /** The controller. */
    public Controller controller;
    /** The player controller. */
    public PlayerController playerController;
    
    /** The current active menu. */
    //public Menu menu;
    
    /** Holds the message history. */
    public final Messages messages;
    
    /** A reference to the game renderer. TODO: Temporary */
    public WorldRenderer renderer;
    
    /** Whether or not the debug display is active. */
    public boolean debug = false;
    
    /** The game profiler. */
    public Profiler profiler = Application.get().profiler;
    /** The game's logging agent. */
    private final Log log = Log.getAgent("GAME");
    
    
    /**
     * Creates a new Game instance.
     * 
     * @param worldBundle The world and player data.
     * 
     * @throws NullPointerException if {@code worldBundle} is {@code null}.
     */
    public Game(WorldBundle worldBundle) {
        this.provider = worldBundle.getHostMultiverse();
        this.world = worldBundle.getHostWorld();
        this.playerData = worldBundle.getPlayerData();
        this.player = worldBundle.getPlayerEntity();
        
        log.postInfo("Initiating game...");
        
        controller = new Controller(this);
        
        // TODO: Hardcoding this is poor design and should be changed in the future
        playerController = new PlayerController(controller, this);
        player.controller = playerController;
        playerController.init(player);
        
        this.messages = new Messages(this);
    }
    
    /**
     * Updates the game - this should be called by the core game update loop.
     */
    public void update() {
        if(running) {
            try {
                ticks++;
                
                profiler.start("menu"); // root.update.game.menu
                //if(menu != null)
                //    menu.update();
                profiler.next("world"); // root.update.game.world
                if(!paused)
                    provider.update();
                else if(advanceTick) {
                    advanceTick = false;
                    provider.update();
                }
                profiler.end(); // root.update.game
            } catch(Exception e) {
                log.postSevere("Game encountered error!", e);
                profiler.disable();
                Application a = Application.get();
                Log.saveLog(true, "Game crashed by did not shut down.");
                //close();            // Simply calling close() makes the game freeze
                a.setState(new MainMenuState());
                return;
            }
        }
    }
    
    /**
     * Renders anything that isn't handled by a renderer.
     */
    public void render() {
        profiler.start("menu"); // root.render.menu
        //if(menu != null)
        //    menu.render();
        profiler.end(); // root.render
    }
    
    /**
     * Closes the game - that is, shuts down the world.
     */
    public void close() {
        running = false;
        //if(menu != null)
        //    menu.unloadResources();
        provider.close();
    }
    
    /**
     * Gets the game world.
     */
    public HostWorld getWorld() {
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
    /*
    public void setMenu(Menu menu, boolean pause) {
        if(menu == null) {
            closeMenu();
        } else {
            this.menu = menu;
            paused = pause;
        }
    }
    */
    
    /**
     * Closes the current menu, if it is non-null. If the game is paused, it
     * will resume.
     */
    public void closeMenu() {
        //if(menu != null)
        //    menu.unloadResources();
        //menu = null;
        paused = false;
        controller.input.setInputProcessor(controller);
    }
    
    /**
     * Opens the pause menu.
     */
    public void openPauseMenu() {
        //setMenu(new PauseMenu(this), true);
        provider.save();
    }
    
    public void pause() {
        paused = true;
    }
    
    public void unpause() {
        paused = false;
    }
    
    public void togglePause() {
        paused = !paused;
    }
    
    public void advanceTick() {
        advanceTick = true;
    }
    
    @Override
    public boolean handleControlPress(Control control) {
        switch(control) {
            case PAUSE:
                togglePause();
                if(paused)
                    messages.send("Paused");
                else
                    messages.send("Unpaused");
                break;
            case ADVANCE_TICK:
                advanceTick();
                break;
            case DEBUG:
                debug = !debug;
                if(debug)
                    profiler.enable();
                else
                    profiler.disable();
                break;
            case TOG_HITBOX_RENDER:
                renderer.renderHitboxes = !renderer.renderHitboxes;
                if(renderer.renderHitboxes)
                    messages.send("Turning on hitboxes");
                else
                    messages.send("Turning off hitboxes");
                break;
            case TOG_SLICE_BORDERS:
                renderer.renderSliceBorders = !renderer.renderSliceBorders;
                if(renderer.renderSliceBorders)
                    messages.send("Turning on slice borders");
                else
                    messages.send("Turning off slice borders");
                break;
            case ZOOM_IN:
                renderer.setPixelsPerTile(renderer.getPixelsPerTile() * 2, true);
                break;
            case ZOOM_OUT:
                renderer.setPixelsPerTile(renderer.getPixelsPerTile() / 2, true);
                break;
            case PROFILER:
                Log.get().postDebug(profiler.getData().toString());
                break;
            case SAVE_LOG:
                Log.saveLog(false, Stabilise.GAME_NAME + " v" + Constants.VERSION);
                break;
            default:
                return playerController.handleControlPress(control);
        }
        return true;
    }
    
    @Override
    public boolean handleControlRelease(Control control) {
        return playerController.handleControlRelease(control);
    }
    
    @Override
    public boolean keyDown(int keycode) {
        renderer.hudRenderer.setProfilerSection(keyValue(keycode));
        return false;
    }
    
    private int keyValue(int keycode) {
        switch(keycode) {
            case Keys.NUM_0:
            case Keys.NUMPAD_0:
                return 0;
            case Keys.NUM_1:
            case Keys.NUMPAD_1:
                return 1;
            case Keys.NUM_2:
            case Keys.NUMPAD_2:
                return 2;
            case Keys.NUM_3:
            case Keys.NUMPAD_3:
                return 3;
            case Keys.NUM_4:
            case Keys.NUMPAD_4:
                return 4;
            case Keys.NUM_5:
            case Keys.NUMPAD_5:
                return 5;
            case Keys.NUM_6:
            case Keys.NUMPAD_6:
                return 6;
            case Keys.NUM_7:
            case Keys.NUMPAD_7:
                return 7;
            case Keys.NUM_8:
            case Keys.NUMPAD_8:
                return 8;
            case Keys.NUM_9:
            case Keys.NUMPAD_9:
                return 9;
            default:
                return -1;
        }
    }
    
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }
    
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    
    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return playerController.touchDown(x, y, pointer, button);
    }
    
    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return playerController.touchUp(x, y, pointer, button);
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }
    
    @Override
    public boolean scrolled(int amount) {
        return playerController.scrolled(amount);
    }
    
}