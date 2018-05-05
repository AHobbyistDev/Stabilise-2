package com.stabilise.world.gen.terrain;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.tile.Tiles.air;
import static com.stabilise.world.tile.Tiles.bedrock;
import static com.stabilise.world.tile.Tiles.dirt;
import static com.stabilise.world.tile.Tiles.glowstone;
import static com.stabilise.world.tile.Tiles.grass;
import static com.stabilise.world.tile.Tiles.stone;
import static com.stabilise.world.tile.Tiles.torch;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.entity.Position;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.IWorldGenerator;
import com.stabilise.world.tile.Tile;

/**
 * Basic overworld terrain generation.
 */
@NotThreadSafe
public class OverworldTerrainGen implements IWorldGenerator {
    
    private final Position pos = Position.create();
    private WorldProvider w;
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        this.w = w;
        
        long mix = 0x3ce575a3c1e97863L;
        OctaveNoise landNoise = OctaveNoise.perlin(seed^mix)
                .addOctave(4096, 256)
                .addOctave(128,  64 )
                .addOctave(64,   32 )
                .addOctave(32,   16 )
                .addOctave(16,   8  )
                .addOctave(8,    4  )
                .addOctave(4,    2  );
        
        int tileOffX = r.x() * REGION_SIZE_IN_TILES;
        int tileOffY = r.y() * REGION_SIZE_IN_TILES;
        
        float[] noiseVec = new float[REGION_SIZE_IN_TILES];
        for(int x = 0; x < REGION_SIZE_IN_TILES; x++) {
            noiseVec[x] = landNoise.noise(x+tileOffX) - tileOffY;
        }
        
        for(int y = 0; y < REGION_SIZE_IN_TILES; y++) {
            for(int x = 0; x < REGION_SIZE_IN_TILES; x++) {
            	pos.set(r.offsetX, r.offsetY, x, y).align();
            	
                float noise = noiseVec[x]--;
                
                if(noise <= -1)
                    w.setTileAt(pos, air);
                else if(noise <= 0) {
                    if(w.rnd().nextInt(10) == 0) {
                        w.setTileAt(pos, torch);
                    } else
                        set(air);
                } else if(noise <= 1) {
                    w.setTileAt(pos, grass);
                    w.setWallAt(pos, dirt);
                } else if(noise <= 5.75f)
                    set(dirt);
                else if(noise <= 200f)
                    set(w.chance(30) ? glowstone : stone);
                else if(noise <= 210f)
                    set(w.chance(30) ? glowstone :
                        (w.rnd().nextDouble() > (210-noise)/10 ? bedrock : stone));
                else
                    set(w.chance(30) ? glowstone : bedrock);
            }
        }
    }
    
    /**
     * Sets both the tile and wall at the given position to the specified tile.
     */
    protected void set(Tile t) {
        w.setTileAt(pos, t);
        w.setWallAt(pos, t);
    }
    
}
