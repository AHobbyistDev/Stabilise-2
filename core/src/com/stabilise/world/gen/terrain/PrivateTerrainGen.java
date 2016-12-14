package com.stabilise.world.gen.terrain;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;

import java.util.Random;

import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.InstancedWorldgen;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;

/**
 * Terrain gen for the private dimension.
 */
public class PrivateTerrainGen extends InstancedWorldgen {
    
    private static final float
            guaranteedAir = 15f, // guaranteed air up to this radius
            normalCaveNoise = 45f, // interp from guaranteed air to normal cave noise ends here
            blockTypeSwitchStart = 50f,
            blockTypeSwitchEnd = 90f,
            caveFalloffBegin = 75f,
            caveExtent = 125f, // cave noise stops here
            caveAbove = 0.5f;
    
    private final OctaveNoise caveNoise;
    private final Random rnd;
    
    public PrivateTerrainGen(WorldProvider w, long seed) {
        super(w, seed);
        
        long mix1 = 0x8daa1080e4bef1cdL;
        long mix2 = 0xdef21d21bb94dfc3L;
        
        caveNoise = OctaveNoise.simplex(1, seed^mix1)
                //.addOctave(32,  4)
                .addOctave(16,  2);
        rnd = new Random(seed^mix2);
    }
    
    @Override
    public void generate(Region r) {
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
                    Tile rockType = (d > blockTypeSwitchEnd) || (rnd.nextFloat() <= denseRockProb)
                            ? Tiles.voidRockDense
                            : Tiles.voidRock;
                    
                    if(cave > caveAbove) {
                        w.setTileAt(x, y, Tiles.air);
                    } else {
                        w.setTileAt(x, y, rockType);
                    }
                    w.setWallAt(x, y, rockType);
                }
            }
        }
    }
    
    private float attenuation(float d) {
        if(d < guaranteedAir)
            return caveAbove;
        else if(d < normalCaveNoise)
            return Interpolation.LINEAR.in.apply(caveAbove, 0f, (d - guaranteedAir)/(normalCaveNoise-guaranteedAir));
        else if(d < caveFalloffBegin)
            return 0f;
        else
            return Interpolation.QUADRATIC.in.apply(0f, -caveAbove, (d-caveFalloffBegin)/(caveExtent-caveFalloffBegin));
    }
    
}
