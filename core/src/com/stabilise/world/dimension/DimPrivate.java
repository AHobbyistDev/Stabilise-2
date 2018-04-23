package com.stabilise.world.dimension;

import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.GeneratorRegistrant;
import com.stabilise.world.gen.terrain.PrivateTerrainGen;
import com.stabilise.world.loader.WorldLoader;

/**
 * The private player-local client-only dimension.
 */
public class DimPrivate extends Dimension {
    
    public DimPrivate(Info info) {
        super(info);
    }
    
    @Override
    protected void addGenerators(GeneratorRegistrant gr) {
        gr.add((w,s) -> new PrivateTerrainGen(w,s));
    }
    
    @Override
    public void addLoaders(WorldLoader wl, WorldInfo info) {
        // no extra stuff to load
    }
    
}
