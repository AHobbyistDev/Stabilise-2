package com.stabilise.world.gen.terrain;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.entity.Position;
import com.stabilise.util.maths.Interpolation;
import com.stabilise.util.maths.OctaveNoise;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.IWorldGenerator;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;

/**
 * Terrain gen for the private dimension.
 */
@NotThreadSafe
public class PrivateTerrainGen implements IWorldGenerator {
    
    private static final float
            guaranteedAir = 15f, // guaranteed air up to this radius
            normalCaveNoise = 45f, // interp from guaranteed air to normal cave noise ends here
            blockTypeSwitchStart = 50f,
            blockTypeSwitchEnd = 90f,
            caveFalloffBegin = 75f,
            caveExtent = 125f, // cave noise stops here
            caveAbove = 0.5f;
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        long mix = 0x8daa1080e4bef1cdL;
        OctaveNoise caveNoise = OctaveNoise.simplex(seed^mix)
                .addOctave(16,  2)
                .normalise();
        
        Position pos = Position.create();
        
        for(int y = 0; y < REGION_SIZE_IN_TILES; y++) {
            for(int x = 0; x < REGION_SIZE_IN_TILES; x++) {
                pos.set(r.offsetX, r.offsetY, x, y).align();
                float d = (float) pos.distFromOrigin();
                
                if(d > caveExtent) {
                    w.setTileAt(pos, Tiles.voidRockDense);
                    w.setWallAt(pos, Tiles.voidRockDense);
                } else {
                    float cave = caveNoise.noise(pos.gx(), pos.gy()) + attenuation(d);
                    
                    float denseRockProb = d > blockTypeSwitchStart
                            ? (d - blockTypeSwitchStart)/(blockTypeSwitchEnd - blockTypeSwitchStart)
                            : 0f;
                    Tile rockType = (d > blockTypeSwitchEnd) || (w.rnd().nextFloat() <= denseRockProb)
                            ? Tiles.voidRockDense
                            : Tiles.voidRock;
                    
                    if(cave > caveAbove)
                    	w.setTileAt(pos, Tiles.air);
                    else
                    	w.setTileAt(pos, rockType);
                    w.setWallAt(pos, rockType);
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
