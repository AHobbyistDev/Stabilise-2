package com.stabilise.world.tile;

import com.stabilise.entity.Position;
import com.stabilise.item.ItemStack;
import com.stabilise.item.Items;
import com.stabilise.world.World;

/**
 * A grass tile.
 */
public class TileGrass extends Tile {
    
    /**
     * Creates a new grass tile.
     */
    TileGrass(TileBuilder b) {
        super(b);
    }
    
    @Override
    public void update(World world, Position pos) {
        Position tmp = pos.copy().add(0f, 1f).realign();
        if(world.getTileAt(tmp).isSolid()) {
            world.setTileAt(pos, Tiles.dirt);
            return;
        }
        
        Tile t1, t2;
        
        for(int tx = -1; tx <= 1; tx++) {
            t2 = world.getTileAt(tmp.set(pos, tx, -2).realign());
            for(int ty = -2; ty <= 1; ty++) {
                // If a tile is dirt and it has an air tile above it, spread
                // grass to that tile with a 1/3 chance
                t1 = t2;
                t2 = world.getTileAt(tmp.set(pos, tx, ty+1).realign());
                if(t1 == Tiles.dirt && !t2.isSolid() && world.chance(3))
                    world.setTileAt(tmp, getID());
            }
        }
    }
    
    @Override
    public ItemStack createStack(int quantity) {
        return Items.TILE.stackOf(Tiles.dirt, quantity);
    }
    
}
