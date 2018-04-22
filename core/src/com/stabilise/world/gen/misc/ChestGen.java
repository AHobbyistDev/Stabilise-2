package com.stabilise.world.gen.misc;

import static com.stabilise.world.Slice.SLICE_SIZE;
import static com.stabilise.world.tile.Tiles.air;
import static com.stabilise.world.tile.Tiles.chest;
import static com.stabilise.world.tile.Tiles.grass;
import static com.stabilise.world.tile.Tiles.stone;

import java.util.Random;

import com.stabilise.entity.Position;
import com.stabilise.item.Items;
import com.stabilise.world.Region;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.gen.IWorldGenerator;
import com.stabilise.world.tile.tileentity.TileEntityChest;


/**
 * Tries to scatter some chests around the world.
 */
public class ChestGen implements IWorldGenerator {
    
    @Override
    public void generate(Region r, WorldProvider w, long seed) {
        long mix1 = 0x225bc9168ac6c9efL;
        final Random rnd = new Random(seed*mix1);
        Position tmp = Position.create();
        Position tmp2 = Position.create();
        
        r.forEachSlice(s -> {
            int x = s.x*SLICE_SIZE + rnd.nextInt(SLICE_SIZE);
            int y = s.y*SLICE_SIZE + rnd.nextInt(SLICE_SIZE - 1);
            int id = w.getTileIDAt(tmp.set(x, y).align());
            if((id == stone.getID() || id == grass.getID()) &&
                        w.getTileIDAt(tmp2.set(x, y+1).align()) == air.getID()) {
                w.setTileAt(tmp, chest);
                TileEntityChest te = (TileEntityChest)w.getTileEntityAt(tmp);
                te.items.addItem(Items.APPLE, rnd.nextInt(7)+1);
                te.items.addItem(Items.SWORD, rnd.nextInt(7)+1);
                te.items.addItem(Items.ARROW, rnd.nextInt(7)+1);
            }
        });
    }
    
}
