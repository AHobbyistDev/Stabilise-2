package com.stabilise.world.gen;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;

import java.util.Random;

import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;


public class PrivateGenerator implements IWorldGenerator {
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        new RegionGenerator(w, seed).generate(r);
    }
    
    private static class RegionGenerator {
        
        private static final float
                guaranteedAir = 20f, // guaranteed air up to this radius
                normalCaveNoise = 30f, // interp from guaranteed air to normal cave noise ends here
                blockTypeSwitchStart = 50f,
                blockTypeSwitchEnd = 90f,
                caveExtent = 200f; // cave noise stops here
        
        private final WorldProvider w;
        
        private final OctaveNoise caveNoise;
        private final Random random;
        
        private RegionGenerator(WorldProvider w, long seed) {
            this.w = w;
            
            long mix1 = 0x8daa1080e4bef1cdL;
            long mix2 = 0xdef21d21bb94dfc3L;
            
            caveNoise = OctaveNoise.simplex(1, seed^mix1)
                    //.addOctave(32,  4)
                    .addOctave(16,  2);
            random = new Random(seed^mix2);
        }
        
        private void generate(Region r) {
            int offsetX = r.x() * REGION_SIZE_IN_TILES;
            int offsetY = r.y() * REGION_SIZE_IN_TILES;
            
            // x,y, and relative x,y
            for(int ry = 0, y = offsetY; ry < REGION_SIZE_IN_TILES; ry++, y++) {
                for(int rx = 0, x = offsetX; rx < REGION_SIZE_IN_TILES; rx++, x++) {
                    if(Math.abs(x) > caveExtent || Math.abs(y) > caveExtent) {
                        w.setTileAt(x, y, Tiles.voidRockDense);
                        w.setWallAt(x, y, Tiles.voidRockDense);
                    } else {
                        float d = (float)Math.sqrt(x*x + y*y); // 0 to caveExtent
                        float cave = caveNoise.noise(x, y) + attenuation(d);
                        
                        float denseRockProb = d > blockTypeSwitchStart
                                ? (d - blockTypeSwitchStart)/(blockTypeSwitchEnd - blockTypeSwitchStart)
                                : 0f;
                        Tile rockType = (d > blockTypeSwitchEnd) || (random.nextFloat() <= denseRockProb)
                                ? Tiles.voidRockDense
                                : Tiles.voidRock;
                        
                        if(cave > 0.5) {
                            w.setTileAt(x, y, Tiles.air);
                        } else {
                            w.setTileAt(x, y, rockType);
                        }
                        w.setWallAt(x, y, rockType);
                    }
                }
            }
            
            r.forEachSlice(s -> s.buildLight());
        }
        
        private float attenuation(float d) {
            if(d < guaranteedAir)
                return 1f;
            else if(d < normalCaveNoise)
                return Interpolation.lerp(1f, 0f, (d - guaranteedAir)/(normalCaveNoise-guaranteedAir));
            else
                return Interpolation.CUBIC.apply(0f, -1f, (d-normalCaveNoise)/(caveExtent-normalCaveNoise));
        }
        
    }
    
}
