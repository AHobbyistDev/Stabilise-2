package com.stabilise.world.dimension;

import com.stabilise.world.gen.WorldGenerator.GeneratorRegistrant;
import com.stabilise.world.gen.terrain.PrivateTerrainGen;

/**
 * The private player-local client-only dimension.
 */
public class DimPrivate extends Dimension {
    
    public DimPrivate(Info info) {
        super(info);
    }
    
    @Override
    protected void addGenerators(GeneratorRegistrant gr) {
        //gr.add(new PrivateTerrainGenOld());
        gr.add((w,s) -> new PrivateTerrainGen(w,s));
    }
    
}
