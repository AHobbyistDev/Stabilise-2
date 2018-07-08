package com.stabilise.render;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.stabilise.core.Application;
import com.stabilise.core.Constants;
import com.stabilise.core.game.Game;
import com.stabilise.entity.Entity;
import com.stabilise.util.Profiler;

/**
 * Renders the in-game HUD.
 */
public class HUDRenderer implements Renderer {
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    private final WorldRenderer wr;
    private final Game game;
    
    // Profiler stuff
    private final Profiler profiler = Application.get().profiler;
    private Profiler.SectionData profilerData;
    /** The current level of profiler data being viewed. */
    private Profiler.SectionData profilerLevel;
    /** The current view stack of the profiler data. */
    private final Deque<String> profilerStack = new ArrayDeque<>();
    
    
    /**
     * Creates a new HUD renderer.
     * 
     * @param game The game.
     * @param worldRenderer The world renderer.
     */
    public HUDRenderer(Game game, WorldRenderer worldRenderer) {
        this.game = game;
        this.wr = worldRenderer;
        
        profilerData = profiler.getData();
        profilerLevel = profilerData;
    }
    
    @Override
    public void update() {
        if(game.debug) {
            Profiler.SectionData d = profiler.getData();
            if(profilerData == d)
                return;
            
            profilerData = d;
            refreshProfiler();
        }
    }
    
    @Override
    public void render() {
        if(game.debug) {
            Entity player = game.camera.entity;
            
            wr.debugFont.setColor(Color.WHITE);
            wr.debugFont.draw(wr.batch,
                    "Stabilise II v" + Constants.VERSION + "\n" +
                    "FPS: " + Application.get().getFPS() + "\n" +
                    "x: " + String.format("%1.2f", player.pos.gx()) + " (" + player.pos.sx() + ")\n" +
                    "y: " + String.format("%1.2f", player.pos.gy()) + " (" + player.pos.sy() + ")\n" +
                    "Entities:  " + wr.world.getEntities().size() + "/" + wr.world.multiverse().getTotalEntityCount() + "\n" +
                    "Hitboxes:  " + wr.world.getHitboxes().size() + "/" + wr.world.hitboxCount + "\n" +
                    "Particles: " + wr.world.getParticles().size() + "/" + wr.world.particleCount + "\n" +
                    "Tile Entities: " + wr.world.getTileEntities().size() + "\n" +
                    "\n" +
                    "Slices rendered: " + wr.tileRenderer.slicesRendered + "\n" +
                    "\n\n\n" +
                    getProfilerStrings(),
                -Gdx.graphics.getWidth() / 2 + 5, // x
                Gdx.graphics.getHeight() / 2 - 5, // y
                Gdx.graphics.getWidth(), // targetWidth
                Align.left, // align
                true // wrap
            );
        }
        
        if(game.ticks - game.messages.getLastMsgTick() < 5*Constants.TICKS_PER_SECOND) {
            wr.msgFont.setColor(Color.WHITE);
            wr.msgFont.draw(wr.batch,
                game.messages.getLastMsg(), // str
                -Gdx.graphics.getWidth() / 2 + 5, // x
                -Gdx.graphics.getHeight() / 2 + 5 + wr.msgFont.getCapHeight(), // y
                Gdx.graphics.getWidth(), // targetWidth
                Align.left, // align
                true // wrap
            );
        }
    }
    
    /**
     * Gets the array of strings representing the profiler results.
     * 
     * @return The profiler results.
     */
    private String getProfilerStrings() {
        StringBuilder sb = new StringBuilder();
        sb.append(profilerLevel.absoluteName);
        sb.append("    ");
        sb.append(String.format("%1.2f", profilerLevel.totalPercent));
        sb.append("% (");
        sb.append(TimeUnit.NANOSECONDS.toMillis(profilerLevel.duration));
        sb.append(" millis)\n");
        int num = 1;
        for(Profiler.SectionData data : profilerLevel.getConstituents()) {
            sb.append('[');
            sb.append(num++);
            sb.append("] ");
            sb.append(String.format("%1.2f", data.totalPercent)).append("% ");
            sb.append(String.format("%1.2f", data.localPercent)).append("% ");
            sb.append(data.name);
            sb.append(" (").append(TimeUnit.NANOSECONDS.toMillis(data.duration)).append(" millis)");
            sb.append('\n');
        }
        return sb.toString();
    }
    
    /**
     * Sets the currently-displayed profiler section.
     * 
     * @param section The section number. Negative values are ignored.
     */
    public void setProfilerSection(int section) {
        if(section == 0) {
            // go up a level
            profilerStack.pollLast();
            refreshProfiler();
        } else if(section > 0 && section <= profilerLevel.getConstituents().length) {
            Profiler.SectionData level = profilerLevel.getConstituents()[section - 1];
            if(level.hasConstituents()) {
                profilerStack.add(level.name);
                profilerLevel = level;
            }
        }
    }
    
    private void refreshProfiler() {
        profilerLevel = profilerData;
        
        outer:
        for(String s : profilerStack) {
            for(Profiler.SectionData data : profilerLevel.getConstituents()) {
                if(data.name.equals(s)) {
                    profilerLevel = data;
                    continue outer;
                }
            }
            // No matching thing found -> cutoff the stack
            break;
        }
    }
    
    @Override
    public void loadResources() {
        
    }
    
    @Override
    public void resize(int width, int height) {
        
    }
    
    @Override
    public void unloadResources() {
        
    }
    
    @Override
    public String toString() {
        return "HUDRenderer";
    }
    
}
