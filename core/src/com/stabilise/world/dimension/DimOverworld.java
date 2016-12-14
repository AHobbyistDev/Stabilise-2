package com.stabilise.world.dimension;

import com.stabilise.world.gen.WorldGenerator.GeneratorRegistrant;
import com.stabilise.world.gen.misc.ChestGen;
import com.stabilise.world.gen.misc.OreGen;
import com.stabilise.world.gen.terrain.OverworldTerrainGen;

/**
 * The Overworld is the default world dimension.
 */
public class DimOverworld extends Dimension {
    
    public DimOverworld(Info info) {
        super(info);
    }
    
    @Override
    protected void addGenerators(GeneratorRegistrant gr) {
        //gr.add(new OverworldTerrainGenOld());
        gr.add((w,s) -> new OverworldTerrainGen(w,s));
        gr.add(new OreGen(2));
        gr.add(new ChestGen());
    }
    
}
