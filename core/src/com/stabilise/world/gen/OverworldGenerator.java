package com.stabilise.world.gen;

import static com.stabilise.world.tile.Tiles.*;

import java.util.Random;

import com.stabilise.item.Items;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntityChest;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.Slice.SLICE_SIZE;

/**
 * Generates worlds using the perlin noise algorithm.
 */
public class OverworldGenerator implements IWorldGenerator {
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        new PerlinRegionGenerator(w, seed).generateRegion(r, seed);
    }
    
    /**
     * An internal class for which newly-constructed instances are delegated
     * the task of generating each new region, as the construction overhead
     * should be much less than the synchronisation overhead if more than one
     * region is to be generated simultaneously.
     */
    private static class PerlinRegionGenerator {
        private OctaveNoise landNoise;
        private OctaveNoise caveNoise;
        private OctaveNoise maskNoise;
        
        private final WorldProvider w;
        private boolean isCave;
        
        private PerlinRegionGenerator(WorldProvider w, long seed) {
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
            
            this.w = w;
        }
        private void generateRegion(Region r, long seed) {
            int offsetX = r.x() * REGION_SIZE_IN_TILES;
            int offsetY = r.y() * REGION_SIZE_IN_TILES;
            
            int n = r.x() + r.y() * 57;
            n = (n<<13) ^ n;
            n = n * (n * n * 15731 + 789221) + 1376312589;
            Random rnd = new Random(seed + n);
            
            float[] noiseVec = new float[REGION_SIZE_IN_TILES];
            for(int x = 0; x < REGION_SIZE_IN_TILES; x++) {
                noiseVec[x] = landNoise.noise(x+offsetX) - offsetY;
            }
            
            for(int y = 0, ty = offsetY; y < REGION_SIZE_IN_TILES; y++, ty++) {
                for(int x = 0, tx = offsetX; x < REGION_SIZE_IN_TILES; x++, tx++) {
                    isCave = false;
                    float noise = noiseVec[x];
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
                        w.setTileAt(tx, ty, air);
                    }
                    
                    if(noise <= -1) {}
                    else if(noise <= 0) {
                        if(rnd.nextInt(10) == 0) {
                            w.setTileAt(tx, ty, torch);
                        } else
                            set(tx, ty, air);
                    } else if(noise <= 1) {
                        if(!isCave)
                            w.setTileAt(tx, ty, grass);
                        w.setWallAt(tx, ty, dirt);
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
            
            ///*
            // For each slice, pick a random tile, and if applicable, place a
            // chest
            for(int x = r.offsetX; x < r.offsetX + REGION_SIZE; x++) {
                for(int y = r.offsetY; y < r.offsetY + REGION_SIZE; y++) {
                    Slice s = w.getSliceAt(x, y);
                    int tx = x*SLICE_SIZE + rnd.nextInt(SLICE_SIZE);
                    int ty = y*SLICE_SIZE + rnd.nextInt(SLICE_SIZE-1);
                    if(w.getTileAt(tx, ty).getID() == stone.getID() &&
                            w.getTileAt(tx, ty+1).getID() == air.getID()) {
                        
                        w.setTileAt(tx, ty, chest);
                        TileEntityChest te = (TileEntityChest)w.getTileEntityAt(tx, ty);
                        te.items.addItem(Items.APPLE, rnd.nextInt(7)+1);
                        te.items.addItem(Items.SWORD, rnd.nextInt(7)+1);
                        te.items.addItem(Items.ARROW, rnd.nextInt(7)+1);
                    }
                    
                    if(w.chance(1))
                        addOres(s, rnd);
                    
                    s.buildLight();
                }
            }
            //*/
        }
        
        private void set(int x, int y, Tile t) {
            if(!isCave)
                w.setTileAt(x, y, t);
            w.setWallAt(x, y, t);
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
        
        /**
         * Adds ore veins to a region.
         * 
         * @param r The region.
         */
        private void addOres(Slice s, Random rnd) {
            int ore = new int[] {
                    Tiles.oreCopper.getID(),
                    Tiles.oreIron.getID(),
                    Tiles.oreSilver.getID(),
                    Tiles.oreGold.getID(),
                    Tiles.oreDiamond.getID()
            }[rnd.nextInt(5)];
            
            int baseX = rnd.nextInt(Integer.MAX_VALUE - SLICE_SIZE);
            int baseY = rnd.nextInt(Integer.MAX_VALUE - SLICE_SIZE);
            Interpolation interp = Interpolation.QUADRATIC.inOut;
            int max = SLICE_SIZE/2;
            
            double[] factors = new double[SLICE_SIZE*SLICE_SIZE];
            int i = 0;
            
            for(int y = 0; y < SLICE_SIZE; y++) {
                for(int x = 0; x < SLICE_SIZE; x++) {
                    
                    // x and y factors range from 0-8; min at edges, max at centre
                    float xFact = interp.transform((max - (x <= max ? max-x-1 : x-max))/(float)max);
                    float yFact = interp.transform((max - (y <= max ? max-y-1 : y-max))/(float)max);
                    double fact = Math.sqrt(xFact*yFact);
                    factors[i++] = fact;
                    if(caveNoise.noiseN(baseX + x, baseY + y, 3) * fact > 0.5 
                            && s.getTileIDAt(x, y) == Tiles.stone.getID()) {
                        s.setTileIDAt(x, y, ore);
                    }
                }
            }
            
            /*
            System.out.println("----BEGIN FACTORS----");
            i = 0;
            StringBuilder sb;
            for(int y = 0; y < SLICE_SIZE; y++) {
                sb = new StringBuilder();
                sb.append('[');
                for(int x = 0; x < SLICE_SIZE; x++) {
                    sb.append(String.format("%.2f", factors[i++]));
                    if(x != SLICE_SIZE-1)
                        sb.append(", ");
                }
                sb.append(']');
                System.out.println(sb.toString());
            }
            System.out.println("----BEGIN FACTORS----");
            */
        }
        
    }
    
}
