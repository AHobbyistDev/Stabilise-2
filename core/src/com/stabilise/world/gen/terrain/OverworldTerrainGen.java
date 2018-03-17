package com.stabilise.world.gen.terrain;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.tile.Tiles.air;
import static com.stabilise.world.tile.Tiles.bedrock;
import static com.stabilise.world.tile.Tiles.dirt;
import static com.stabilise.world.tile.Tiles.glowstone;
import static com.stabilise.world.tile.Tiles.grass;
import static com.stabilise.world.tile.Tiles.lava;
import static com.stabilise.world.tile.Tiles.stone;
import static com.stabilise.world.tile.Tiles.torch;

import java.util.Random;

import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.InstancedWorldgen;
import com.stabilise.world.tile.Tile;

/**
 * Basic overworld terrain generation.
 */
public class OverworldTerrainGen extends InstancedWorldgen {
    
    private final OctaveNoise landNoise;
    private final OctaveNoise caveNoise;
    private final OctaveNoise maskNoise;
    
    private boolean isCave;
    
    public OverworldTerrainGen(WorldProvider w, long seed) {
        super(w, seed);
        
        long mix1 = 0x3ce575a3c1e97863L;
        long mix2 = 0xd74a9ad1417d79a0L;
        long mix3 = 0x7227bebc43323e77L;

        landNoise = OctaveNoise.perlin(7, seed^mix1)
                .doNotNormalise()
                .addOctave(4096, 256)
                .addOctave(128,  64 )
                .addOctave(64,   32 )
                .addOctave(32,   16 )
                .addOctave(16,   8  )
                .addOctave(8,    4  )
                .addOctave(4,    2  );
        caveNoise = OctaveNoise.simplex(4, seed^mix2)
                .addOctave(128, 2)
                .addOctave(64,  8)
                .addOctave(32,  4)
                .addOctave(16,  1);
        maskNoise = OctaveNoise.simplex(1, seed^mix3).addOctave(512, 1);
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
            	tmpPos.set(tx, ty).realign();
            	
                isCave = false;
                float noise = noiseVec[x]--;
                float cave = caveNoise.noise(tx, ty);
                // This should produce varying cave types across the world as
                // the noise forms characteristically different contours at
                // different points between 0.25-0.75.
                float caveMask = 0.25f + transformCaveMask(maskNoise.noise(tx,ty))/2;
                
                // Multiply caveNoise or caveMask by 0 to 1 based on the depth
                // to try to deter a great multitude of surface cave entrances
                //if(noise >= 0D && noise <= 20D)
                //    caveMask *= Interpolation.QUADRATIC.easeIn(0.5f, 1, (float)noise / 20);
                
                //if((y < -200 && caveNoise > 0.8D) || (y < -180 && caveNoise > (0.8 - 0.2 * (180+y)/20f)))
                if(ty < -200 && cave > 0.8f)
                    set(tx, ty, lava);
                //else if(noise <= 0 || (caveNoise > 0.45D && caveNoise < 0.55D))
                else if(noise <= -1 || (cave > caveMask - 0.05f && cave < caveMask + 0.05f)) {
                //else if(noise <= 0 || caveNoise > 0.8D)
                    isCave = true;
                    w.setTileAt(tmpPos, air);
                }
                
                if(noise <= -1) {}
                else if(noise <= 0) {
                    if(rnd.nextInt(10) == 0) {
                        w.setTileAt(tmpPos.set(tx, ty).realign(), torch);
                    } else
                        set(tx, ty, air);
                } else if(noise <= 1) {
                    if(!isCave)
                        w.setTileAt(tmpPos, grass);
                    w.setWallAt(tmpPos, dirt);
                } else if(noise <= 5.75f)
                    set(tx, ty, dirt);
                else if(noise <= 200f)
                    set(tx, ty, w.chance(30) ? glowstone : stone);
                else if(noise <= 210f)
                    set(tx, ty, w.chance(30) ? glowstone :
                        (w.rnd().nextDouble() > (210-noise)/10 ? bedrock : stone));
                else
                    set(tx, ty, w.chance(30) ? glowstone : bedrock);
            }
        }
    }
    
    @Override
    protected void set(int x, int y, Tile t) {
    	tmpPos.set(x, y).realign();
        if(!isCave)
            w.setTileAt(tmpPos, t);
        w.setWallAt(tmpPos, t);
    }
    
    /**
     * Transforms the noise portion of the cave mask using the function
     * <tt>f(x) = ((2x - 1)<font size=-1><sup>3</sup></font> + 1) / 2</tt>, so
     * that it tends toward a value of 0.5.
     * 
     * @param caveMask The value for the cave mask noise.
     * 
     * @return The noise transformed such that the value tends toward 0.5.
     */
    private float transformCaveMask(float caveMask) {
        //f(x) = ((2x - 1)^3 + 1) / 2
        caveMask = 2*caveMask - 1;
        caveMask *= caveMask*caveMask;
        return (caveMask+1)/2;
    }
    
}
