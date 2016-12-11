package com.stabilise.world.gen;

import static com.stabilise.world.tile.Tiles.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.stabilise.item.Items;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.PerlinNoise1D;
import com.stabilise.util.maths.SimplexNoise;
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
public class OverworldGeneratorOld implements IWorldGenerator {
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        new PerlinRegionGenerator(seed).generateRegion(r, w, seed);
    }
    
    /**
     * An internal class for which newly-constructed instances are delegated
     * the task of generating each new region, as the construction overhead
     * should be much less than the synchronisation overhead if more than one
     * region is to be generated simultaneously.
     */
    private class PerlinRegionGenerator {
        private List<Region.QueuedStructure> schematics = new ArrayList<>();
        
        // Landform                             WAVELENGTH    AMPLITUDE
        private PerlinNoise1D noise1D_1;    //    128            64
        private PerlinNoise1D noise1D_2;    //    64             32
        private PerlinNoise1D noise1D_3;    //    32             16
        private PerlinNoise1D noise1D_4;    //    16             8
        private PerlinNoise1D noise1D_5;    //    8              4
        private PerlinNoise1D noise1D_6;    //    4              2
        
        // Caves                                SCALE         AMPLITUDE
        private SimplexNoise simplex128;    //    128             2
        private SimplexNoise simplex64;     //    64              8
        private SimplexNoise simplex32;     //    32              4
        private SimplexNoise simplex16;     //    16              1
        
        private SimplexNoise simplex512;    //    512             1
        
        private Slice s;
        private boolean cave;
        
        private PerlinRegionGenerator(long seed) {
            noise1D_1 = new PerlinNoise1D(seed, 128f);
            noise1D_2 = new PerlinNoise1D(seed, 64f);
            noise1D_3 = new PerlinNoise1D(seed, 32f);
            noise1D_4 = new PerlinNoise1D(seed, 16f);
            noise1D_5 = new PerlinNoise1D(seed, 8f);
            noise1D_6 = new PerlinNoise1D(seed, 4f);
            
            simplex128 = new SimplexNoise(seed, 128f);
            simplex64 = new SimplexNoise(seed, 64f);
            simplex32 = new SimplexNoise(seed, 32f);
            simplex16 = new SimplexNoise(seed, 16f);
            
            simplex512 = new SimplexNoise(seed, 512f);
        }
        private void generateRegion(Region r, WorldProvider w, long seed) {
            final int defSlice = Region.REGION_SIZE - 1;
            final int defTile = Slice.SLICE_SIZE - 1;
            
            int offsetX = r.x() * REGION_SIZE_IN_TILES;
            int offsetY = r.y() * REGION_SIZE_IN_TILES;
            
            int sliceX = defSlice;
            int sliceY = defSlice;
            int tileX = defTile;
            int tileY = defTile;
            
            int n = r.x() + r.y() * 57;
            n = (n<<13) ^ n;
            n = n * (n * n * 15731 + 789221) + 1376312589;
            Random rnd = new Random(seed + n);
            
            s = r.getSliceAt(defSlice, defSlice);
            
            for(int x = offsetX + REGION_SIZE_IN_TILES; x > offsetX; x--) {
                double noise = noise1D(x) - offsetY - REGION_SIZE_IN_TILES;
                
                tileY = defTile;
                for(int y = offsetY + REGION_SIZE_IN_TILES; y > offsetY; y--) {
                    cave = false;
                    
                    double caveNoise = noise2D(x, y);
                    // This should produce varying cave types across the world as
                    // the noise forms characteristically different contours at
                    // different points between 0.25-0.75.
                    double caveMask = 0.25D + transformCaveMask(simplex512.noise(x, y))/2;
                    
                    // Multiply caveNoise or caveMask by 0 to 1 based on the depth
                    // to try to deter a great multitude of surface cave entrances
                    //if(noise >= 0D && noise <= 20D)
                    //    caveMask *= Interpolation.QUADRATIC.easeIn(0.5f, 1, (float)noise / 20);
                    
                    //if((y < -200 && caveNoise > 0.8D) || (y < -180 && caveNoise > (0.8 - 0.2 * (180+y)/20f)))
                    if(y < -200 && caveNoise > 0.8D)
                        set(tileX, tileY, lava);
                    //else if(noise <= 0 || (caveNoise > 0.45D && caveNoise < 0.55D))
                    else if(noise <= -1 || (caveNoise > caveMask - 0.05D && caveNoise < caveMask + 0.05D)) {
                    //else if(noise <= 0 || caveNoise > 0.8D)
                        cave = true;
                        s.setTileAt(tileX, tileY, air);
                    }
                    
                    if(noise <= -1) {}
                    else if(noise <= 0) {
                        if(rnd.nextInt(10) == 0) {
                            s.setTileAt(tileX, tileY, torch);
                        } else
                            set(tileX, tileY, air);
                    } else if(noise <= 1) {
                        if(!cave)
                            s.setTileAt(tileX, tileY, grass);
                        s.setWallAt(tileX, tileY, dirt);
                        //if(rnd.nextInt(10) == 0)
                        //    addSchematic("tree_1", s.x, s.y, tileX, tileY);
                    } else if(noise <= 5.75D)
                        set(tileX, tileY, dirt);
                    else {
                        if(rnd.nextInt(30) == 0)
                            set(tileX, tileY, glowstone);
                        else
                            set(tileX, tileY, stone);
                    }
                    
                    noise++;
                    
                    tileY--;
                    if(tileY == -1) {
                        sliceY--;
                        if(sliceY == -1)
                            break;
                        s = r.getSliceAt(sliceX, sliceY);
                        tileY = defTile;
                    }
                }
                
                tileX--;
                sliceY = defSlice;
                
                if(tileX == -1) {
                    sliceX--;
                    if(sliceX == -1)
                        break;
                    tileX = defTile;
                }
                
                // Reset the slice
                s = r.getSliceAt(sliceX, sliceY);
            }
            
            ///*
            // For each slice, pick a random tile, and if applicable, place a
            // chest
            for(int x = 0; x < REGION_SIZE; x++) {
                for(int y = 0; y < REGION_SIZE; y++) {
                    s = r.getSliceAt(x, y);
                    tileX = rnd.nextInt(SLICE_SIZE);
                    tileY = rnd.nextInt(SLICE_SIZE-1);
                    if(s.getTileAt(tileX, tileY).getID() == stone.getID() &&
                            s.getTileAt(tileX, tileY+1).getID() == 0) {
                        s.tiles[tileY+1][tileX] = chest.getID();
                        TileEntityChest chestTE = chest.createTE(
                                offsetX + x*SLICE_SIZE + tileX,
                                offsetY + y*SLICE_SIZE + tileY + 1);
                        chestTE.items.addItem(Items.APPLE, rnd.nextInt(7)+1);
                        chestTE.items.addItem(Items.SWORD, rnd.nextInt(7)+1);
                        chestTE.items.addItem(Items.ARROW, rnd.nextInt(7)+1);
                        s.setTileEntityAt(tileX, tileY+1, chestTE);
                    }
                    
                    if(rnd.nextInt(1) == 0)
                        addOres(s, rnd);
                    
                    s.buildLight();
                }
            }
            //*/
            
            /*
            // Temporary schematics for testing purposes
            generator.addSchematicAt(r, "testhouse", 15, 15, 8, 15, SchematicParams.defaultParams());
            generator.addSchematicAt(r, "testhouse", 0, 14, 8, 15, SchematicParams.defaultParams());
            generator.addSchematicAt(r, "testStructure", 7, 0, 1, 0, SchematicParams.defaultParams());
            //*/
            
            addSchematics(r);
        }
        
        private void set(int x, int y, Tile t) {
            if(!cave)
                s.setTileAt(x, y, t);
            s.setWallAt(x, y, t);
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
        private double transformCaveMask(double caveMask) {
            //f(x) = ((2x - 1)^3 + 1) / 2
            caveMask = 2*caveMask - 1;
            caveMask *= caveMask*caveMask;
            return (caveMask+1)/2D;
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
                    if(simplex16.noise(baseX + x, baseY + y) * fact > 0.5 
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
        
        /**
         * Gets the 1-dimensional noise value at the given x-coordinate.
         * 
         * @param x The x-coordinate.
         * 
         * @return The noise value at x.
         */
        private double noise1D(int x) {
            return noise1D_1.noise(x) * 64f +
                    noise1D_2.noise(x) * 32f +
                    noise1D_3.noise(x) * 16f +
                    noise1D_4.noise(x) * 8f +
                    noise1D_5.noise(x) * 4f +
                    noise1D_6.noise(x) * 2f;
            
            /*
            double noise = 0.0D;
            for(int i = 0; i < LAYERS_1D.length; i++) {
                float amplitude = LAYERS_1D[i][0];
                float wavelength = LAYERS_1D[i][1];
                
                setWavelength(wavelength);
                double scaledX = x / wavelength;
                double flooredX = Math.floor(scaledX);
                
                setSeed((int)flooredX);
                double y0 = (2*rnd.nextFloat() - 1) * amplitude;
                setSeed((int)(flooredX+1));
                double y1 = (2*rnd.nextFloat() - 1) * amplitude;
                
                noise += MathUtil.interpolateSinusoidal(y0, y1, scaledX - flooredX);
            }
            return noise;
            */
        }
        
        private double noise2D(int x, int y) {
            return (simplex128.noise(x,y) * 2 +
                    simplex64.noise(x,y) * 8 +
                    simplex32.noise(x,y) * 4 +
                    simplex16.noise(x,y) * 1) / 15D;
        }
        
        @SuppressWarnings("unused")
        private void addSchematic(String sc, int sliceX, int sliceY, int tileX, int tileY) {
            schematics.add(new Region.QueuedStructure(sc, sliceX, sliceY, tileX, tileY, 0, 0));
        }
        
        private void addSchematics(Region r) {
            //for(Region.QueuedStructure s : schematics)
            //    addSchematicAt(r, s.schematicName, s.sliceX, s.sliceY, s.tileX, s.tileY, SchematicParams.defaultParams());
            //schematics.clear();
        }
    }
    
    /*
    @Override
    public void setPlayerSpawn(WorldInfo world) {
        world.spawnSliceX = 0;
        world.spawnSliceY = World.sliceCoordFromTileCoord(MathUtil.fastFloor(new PerlinRegionGenerator(seed).noise1D(0)));
    }
    */
    
}
