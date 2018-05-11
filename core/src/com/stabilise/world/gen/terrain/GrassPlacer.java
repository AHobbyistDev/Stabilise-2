package com.stabilise.world.gen.terrain;

import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;

import com.stabilise.entity.Position;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.IWorldGenerator;
import com.stabilise.world.tile.Tiles;


public class GrassPlacer implements IWorldGenerator {
    
    // TODO:
    // The way I want to implement this requires a lot of implementational
    // hoops.
    // * Need to have the region above us fully generated in order to properly
    //   lay down grass and dirt.
    // * Ideally I want this to be as ergonomic as possible, so generation
    //   code can be written without having to worry about loading regions
    //   behind the scenes.
    // * It also needs to be consistent. i.e., generation should be insensitive
    //   to which region is generated first. This can be difficult to get right
    //   if we take a naive approach to the ergonomic point above.
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        Position pos1 = Position.createFixed();
        Position pos2 = Position.createFixed();
        
        // Make y to to REGION_SIZE_IN_TILES-1 for now, until I figure out a
        // good way to go about looking at the air tile above
        for(int y = 0; y < REGION_SIZE_IN_TILES-1; y++) {
            for(int x = 0; x < REGION_SIZE_IN_TILES; x++) {
                pos1.set(r.offsetX, r.offsetY, x, y).align();
                pos2.set(pos1, 0, 1).align();
                
                if(w.getTileAt(pos2).equals(Tiles.air) && w.getTileAt(pos1).isSolid()) {
                    w.setTileAt(pos1, Tiles.grass);
                }
            }
        }
    }
    
}
