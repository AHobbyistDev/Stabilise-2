package com.stabilise.opengl.render;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.stabilise.core.Constants;
import com.stabilise.core.Game;
import com.stabilise.core.app.Application;
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
    
    private final Entity player;
    
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
        
        player = game.player;
        
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
            wr.font.setColor(Color.BLACK);
            wr.font.draw(wr.batch,
                    "Stabilise II v" + Constants.VERSION + "\n" +
                    //"FPS: " + screen.getFPS() + " (" + screen.getFPSCap() + ")",
                    "x: " + String.format("%1.2f", player.x) + "\n" +
                    "y: " + String.format("%1.2f", player.y) + "\n" +
                    "Entities:  " + wr.world.getEntities().size() + "/" + wr.world.entityCount + "\n" +
                    "Hitboxes:  " + wr.world.getHitboxes().size() + "/" + wr.world.hitboxCount + "\n" +
                    "Particles: " + wr.world.getParticles().size() + "/" + wr.world.particleCount + "\n" +
                    "\n" +
                    "Slices rendered: " + wr.tileRenderer.slicesRendered + "\n" +
                    //"World seed: " + wr.world.info.seed + "\n" +
                    //"World age: " + wr.world.info.age + "\n" +
                    "\n\n\n" +
                    getProfilerStrings(),
                -Gdx.graphics.getWidth() / 2 + 5, // x
                Gdx.graphics.getHeight() / 2 - 5, // y
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
                if(data.name == s) {
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
