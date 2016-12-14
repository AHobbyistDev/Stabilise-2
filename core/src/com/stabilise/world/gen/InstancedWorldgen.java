package com.stabilise.world.gen;

import com.stabilise.util.Log;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.tile.Tile;

/**
 * This class provides a basic template for an {@code IWorldGenerator} that is
 * newly-instantiated every time a region needs to be generated. The strategy
 * of creating a new instance each time is done as to avoid any concurrency
 * problems which might otherwise arise if a single {@code IWorldGenerator}
 * object were to generate multiple regions concurrently. 
 */
public abstract class InstancedWorldgen implements IWorldGenerator {
    
    protected final WorldProvider w;
    protected final long seed;
    
    
    /**
     * @param w See {@link GenProvider}.
     * @param seed The world seed.
     */
    public InstancedWorldgen(WorldProvider w, long seed) {
        this.w = w;
        this.seed = seed;
    }
    
    @Override
    public final void generate(Region r, WorldProvider w, long seed) {
        Log.getAgent("InstancedWorldgen")
                .postWarning("The base version of generate() shouldn't be used!");
        generate(r);
    }
    
    /**
     * Generates a region. See {@link #generate(Region, WorldProvider, long)}
     * for the full contract.
     */
    public abstract void generate(Region r);
    
    /**
     * Sets both the tile and wall at the given (absolute) coordinates to the
     * specified tile. Equivalent to the following:
     * 
     * <pre>
     * w.setTileAt(x, y, t);
     * w.setWallAt(x, y, t);
     * </pre>
     */
    protected void set(int x, int y, Tile t) {
        w.setTileAt(x, y, t);
        w.setWallAt(x, y, t);
    }
    
    
    /**
     * This interface exists to allow users to pass constructors for
     * InstancedWorldgen objects over to {@link
     * WorldGenerator#addGenerator(InstancedWorldgenSupplier)}. For example,
     * 
     * <pre>
     * generator.addGenerator((w,s) -> new MyWorldGen(w,s));
     * </pre>
     */
    public static interface InstancedWorldgenSupplier {
        InstancedWorldgen get(WorldProvider w, long seed);
    }
    
}
