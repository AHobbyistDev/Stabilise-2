package com.stabilise.core.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.stabilise.core.Game;
import com.stabilise.core.Resources;
import com.stabilise.core.app.Application;
import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.util.Profiler;
import com.stabilise.world.Worlds.WorldBundle;

/**
 * The SingleplayerState is the state which will run while singleplayer
 * adventure mode is running.
 */
public class SingleplayerState implements State {
    
    /** The game. */
    public Game game;
    
    /** The renderer to use for rendering the world. */
    public WorldRenderer renderer;
    
    /** The profiler. */
    public Profiler profiler = Application.get().profiler;
    
    /** temporary public sound effect */
    public static Sound pop;
    
    
    /**
     * Creates a new SingleplayerState instance.
     * 
     * @param worldBundle The world and player data.
     * 
     * @throws NullPointerException if {@code bundle} is {@code null}.
     */
    public SingleplayerState(WorldBundle worldBundle) {
        super();
        
        game = new Game(worldBundle);
    }
    
    @Override
    public void start() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(game);
        input.addProcessor(game.controller);
        input.addProcessor(game.playerController);
        Gdx.input.setInputProcessor(input);
        
        renderer = new WorldRenderer(game, game.getWorld(), game.player, game.playerController);
        
        pop = Gdx.audio.newSound(Resources.SOUND_DIR.child("pop.mp3"));
        
        profiler.enable();
    }
    
    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }
    
    @Override
    public void predispose() {
        game.close();
        profiler.disable();
    }
    
    @Override
    public void dispose() {
        //((InputMultiplexer)Gdx.input).removeProcessor(game);
        
        renderer.unloadResources();
        renderer = null;
        
        pop.dispose();
        
        // Try to garbage collect everything which has been unloaded
        //game = null;        // <-- No-can-do, crashes in update() at if(!game.running)
        System.gc();
    }
    
    @Override
    public void pause() {
        //if(game.menu == null)
        //    game.openPauseMenu();
    }
    
    @Override
    public void resume() {
        // nothing to see here, move along
    }
    
    @Override
    public void update() {
        profiler.start("game"); // root.update.game
        game.update();
        
        // Safety net to prevent an NPE from the renderer if the game shuts
        // down this tick
        if(!game.running) {
            profiler.end(); // root.update
            return;
        }
        
        profiler.next("renderer"); // root.update.renderer
        renderer.update();
        profiler.end(); // root.update
    }
    
    @Override
    public void render(float delta) {
        renderer.render();
        game.render();
    }
    
}
