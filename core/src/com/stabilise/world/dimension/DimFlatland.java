package com.stabilise.world.dimension;

import com.stabilise.world.WorldInfo;
import com.stabilise.world.gen.WorldGenerator;
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
    public void addGenerators(WorldGenerator g) {
        g.addGenerator(new FlatlandTerrainGen());
        g.addGenerator(new CaveGen());
        g.addGenerator(new OreGen(2));
        g.addGenerator(new ChestGen());
    }
    
    @Override
    public void addLoaders(WorldLoader wl, WorldInfo info) {
        // no extra stuff to load
    }
    
}
