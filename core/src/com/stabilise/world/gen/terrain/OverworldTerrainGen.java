package com.stabilise.world.gen.terrain;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.tile.Tiles.air;
import static com.stabilise.world.tile.Tiles.bedrock;
import static com.stabilise.world.tile.Tiles.dirt;
import static com.stabilise.world.tile.Tiles.glowstone;
import static com.stabilise.world.tile.Tiles.grass;
import static com.stabilise.world.tile.Tiles.stone;
import static com.stabilise.world.tile.Tiles.torch;

import java.util.Random;

import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.InstancedWorldgen;

/**
 * Basic overworld terrain generation.
 */
public class OverworldTerrainGen extends InstancedWorldgen {
    
    private final OctaveNoise landNoise;
    
    public OverworldTerrainGen(WorldProvider w, long seed) {
        super(w, seed);
        
        long mix = 0x3ce575a3c1e97863L;
        landNoise = OctaveNoise.perlin(7, seed^mix)
                .doNotNormalise()
                .addOctave(4096, 256)
                .addOctave(128,  64 )
                .addOctave(64,   32 )
                .addOctave(32,   16 )
                .addOctave(16,   8  )
                .addOctave(8,    4  )
                .addOctave(4,    2  );
    }
    
    @Override
    public void generate(Region r) {
        int offsetX = r.x() * REGION_SIZE_IN_TILES;
        int offsetY = r.y() * REGION_SIZE_IN_TILES;
        
        int n = r.x() + r.y() * 57;
        n = (n<<13) ^ n;
        n = n * (n * n * 15731 + 789221) + 1376312589;
        Random rnd = new Random(seed ^ n);
        
        float[] noiseVec = new float[REGION_SIZE_IN_TILES];
        for(int x = 0; x < REGION_SIZE_IN_TILES; x++) {
            noiseVec[x] = landNoise.noise(x+offsetX) - offsetY;
        }
        
        for(int y = 0, ty = offsetY; y < REGION_SIZE_IN_TILES; y++, ty++) {
            for(int x = 0, tx = offsetX; x < REGION_SIZE_IN_TILES; x++, tx++) {
            	tmpPos.set(tx, ty);
            	
                float noise = noiseVec[x]--;
                
                if(noise <= -1)
                    w.setTileAt(tmpPos, air);
                else if(noise <= 0) {
                    if(rnd.nextInt(10) == 0) {
                        w.setTileAt(tmpPos.set(tx, ty).align(), torch);
                    } else
                        set(tmpPos, air);
                } else if(noise <= 1) {
                    w.setTileAt(tmpPos, grass);
                    w.setWallAt(tmpPos, dirt);
                } else if(noise <= 5.75f)
                    set(tmpPos, dirt);
                else if(noise <= 200f)
                    set(tmpPos, w.chance(30) ? glowstone : stone);
                else if(noise <= 210f)
                    set(tmpPos, w.chance(30) ? glowstone :
                        (w.rnd().nextDouble() > (210-noise)/10 ? bedrock : stone));
                else
                    set(tmpPos, w.chance(30) ? glowstone : bedrock);
            }
        }
    }
    
}
