package com.stabilise.world.gen.terrain;

import java.util.Random;

import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.InstancedWorldgen;
import com.stabilise.world.tile.Tiles;


/**
 * Terrain gen for a flat world
 */
public class FlatlandTerrainGen extends InstancedWorldgen {
    
    private final Random rnd;
    
    public FlatlandTerrainGen(WorldProvider w, long seed) {
        super(w, seed);
        
        long mix = 0x59c5180355b14d9bL;
        rnd = new Random(seed ^ mix);
    }
    
    @Override
    public void generate(Region r) {
        r.forEachSlice(s -> genSlice(s));
    }
    
    private void genSlice(Slice s) {
        if(s.y >= 0)
            return; // leave the slice as just air
        
        for(int y = 0; y < Slice.SLICE_SIZE; y++) {
            for(int x = 0; x < Slice.SLICE_SIZE; x++) {
                int n = rnd.nextInt(16);
                if(n < 6)
                    s.setTileAt(x, y, Tiles.stone);
                else if(n < 12)
                    s.setTileAt(x, y, Tiles.stoneBrick);
                else if(n < 15)
                    s.setTileAt(x, y, Tiles.ice);
                else
                    s.setTileAt(x, y, Tiles.glowstone);
                s.setWallAt(x, y, Tiles.stone);
            }
        }
    }
    
}
