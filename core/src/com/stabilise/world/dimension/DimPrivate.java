package com.stabilise.world.dimension;

import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.WorldGenerator;
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
    public void addGenerators(WorldGenerator g) {
        g.addGenerator(new PrivateTerrainGen());
    }
    
    @Override
    public void addLoaders(WorldLoader wl, WorldInfo info) {
        // no extra stuff to load
    }
    
}
