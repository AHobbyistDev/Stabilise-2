package com.stabilise.world.dimension;

import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.GeneratorRegistrant;
import com.stabilise.world.gen.misc.ChestGen;
import com.stabilise.world.gen.misc.OreGen;
import com.stabilise.world.gen.terrain.CaveGen;
import com.stabilise.world.gen.terrain.FlatlandTerrainGen;
import com.stabilise.world.loader.WorldLoader;


public class DimFlatland extends Dimension {
    
    public DimFlatland(Info info) {
        super(info);
    }
    
    @Override
    protected void addGenerators(GeneratorRegistrant gr) {
        gr.add((w,s) -> new FlatlandTerrainGen(w,s));
        gr.add((w,s) -> new CaveGen(w,s));
        gr.add(new OreGen(2));
        gr.add(new ChestGen());
    }
    
    @Override
    public void addLoaders(WorldLoader wl, WorldInfo info) {
        // no extra stuff to load
    }
    
}
