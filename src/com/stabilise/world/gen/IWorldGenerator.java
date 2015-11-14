package com.stabilise.world.gen;

import com.stabilise.world.Region;
import com.stabilise.world.tile.Tiles;

public interface IWorldGenerator {
    
    /**
     * Generates a region.
     * 
     * <p>The general contract of this method is that it may modify the
     * contents of any slice in the given region - namely, it may set tiles and
     * tile entities, and add schematics. Leaving this method blank is
     * equivalent to setting every tile in the region to {@link Tiles#AIR}.
     * 
     * <p>Implementors should note that this method may be invoked by various
     * worker threads concurrently, and it is hence the responsibility of
     * implementors to ensure correct thread safety techniques are observed.
     * 
     * @param r The region to generate.
     * @param seed The world seed.
     */
    void generate(Region r, long seed);
    
}
