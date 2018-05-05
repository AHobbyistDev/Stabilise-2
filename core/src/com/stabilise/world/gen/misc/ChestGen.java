package com.stabilise.world.gen.misc;

import static com.stabilise.world.Slice.SLICE_SIZE;
import static com.stabilise.world.tile.Tiles.air;
import static com.stabilise.world.tile.Tiles.chest;
import static com.stabilise.world.tile.Tiles.grass;
import static com.stabilise.world.tile.Tiles.stone;

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
        Position tmp = Position.create();
        Position tmp2 = Position.create();
        
        r.forEachSlice(s -> {
            int x = w.rnd().nextInt(SLICE_SIZE);
            int y = w.rnd().nextInt(SLICE_SIZE - 1);
            tmp.set(s.x, s.y, x, y); // no need to align
            int id = w.getTileIDAt(tmp);
            if((id == stone.getID() || id == grass.getID()) &&
                        w.getTileIDAt(tmp2.set(tmp, 0, 1).alignY()) == air.getID()) {
                w.setTileAt(tmp, chest);
                TileEntityChest te = (TileEntityChest)w.getTileEntityAt(tmp);
                te.items.addItem(Items.APPLE, w.rnd().nextInt(7)+1);
                te.items.addItem(Items.SWORD, w.rnd().nextInt(7)+1);
                te.items.addItem(Items.ARROW, w.rnd().nextInt(7)+1);
            }
        });
    }
    
}
