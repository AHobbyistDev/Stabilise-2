package com.stabilise.core.state;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.stabilise.core.Application;
import com.stabilise.core.Resources;
import com.stabilise.core.game.Game;
import com.stabilise.render.WorldRenderer;
import com.stabilise.util.Profiler;
import com.stabilise.world.Worlds.WorldBundle;

/**
 * The SingleplayerState is the state which will run while singleplayer
 * adventure mode is running.
 */
public class SingleplayerState implements State {
    
    public final Game game;
    public WorldRenderer renderer;
    
    public Profiler profiler = Application.get().profiler;
    
    /** temporary public sound effect */
    public static Sound pop;
    
    
    /**
     * @param worldBundle The world and player data.
     */
    public SingleplayerState(WorldBundle worldBundle) {
        game = new Game(worldBundle);
    }
    
    @Override
    public void start() {
        InputMultiplexer input = new InputMultiplexer();
        input.addProcessor(game);
        input.addProcessor(game.controller);
        input.addProcessor(game.playerController);
        Gdx.input.setInputProcessor(input);
        
        renderer = new WorldRenderer(game, game.camera, game.playerController);
        game.renderer = renderer;
        
        pop = Gdx.audio.newSound(Resources.DIR_SOUND.child("pop.mp3"));
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
