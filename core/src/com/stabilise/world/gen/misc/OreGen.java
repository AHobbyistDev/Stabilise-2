package com.stabilise.world.gen.misc;

import static com.stabilise.world.Slice.SLICE_SIZE;

import java.util.Random;

import com.stabilise.util.Checks;
import com.stabilise.util.maths.INoise;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.Slice;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.IWorldGenerator;
import com.stabilise.world.tile.Tiles;

/**
 * Basic ore generation.
 */
public class OreGen implements IWorldGenerator {
    
    private final int n;
    
    /**
     * @param n The inverse chance of an ore vein generating in a slice. That
     * is, an ore vein will have a 1/n chance of generating.
     * 
     * @throws IllegalArgumentException if n < 1.
     */
    public OreGen(int n) {
        this.n = Checks.testMin(n, 1);
    }
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        long mix1 = 0xdb64064dff219635L;
        long mix2 = 0xf1d4c49b0ac04506L;
        Random rnd = new Random(seed^mix1);
        OctaveNoise noise = OctaveNoise.simplex(2, seed^mix2)
                .addOctave(32, 4)
                .addOctave(8,  1);
        r.forEachSlice(s -> { if(w.chance(n)) addOreVein(s,rnd,noise); });
    }
    
    private void addOreVein(Slice s, Random rnd, INoise noise) {
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
                int stoneID = Tiles.stone.getID();
                if(noise.noise(baseX + x, baseY + y) * fact > 0.5 
                        && s.getTileIDAt(x, y) == stoneID) {
                    s.setTileIDAt(x, y, ore);
                }
            }
        }
    }
    
}