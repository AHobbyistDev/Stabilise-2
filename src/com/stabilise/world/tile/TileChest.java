package com.stabilise.world.tile;

import com.stabilise.entity.Entity;
import com.stabilise.util.Log;
import com.stabilise.world.World;
import com.stabilise.world.tile.tileentity.TileEntityChest;

/**
 * A chest tile.
 */
public class TileChest extends TileTE {
    
    /**
     * Creates a chest tile.
     */
    TileChest(TileBuilder b) {
        super(b);
    }
    
    @Override
    public TileEntityChest createTE(int x, int y) {
        return new TileEntityChest(x, y);
    }
    
    @Override
    public void handleInteract(World world, int x, int y, Entity e) {
        super.handleInteract(world, x, y, e);
        
        TileEntityChest c = (TileEntityChest)world.getTileEntityAt(x, y);
        // TODO: temporary
        if(c == null)
            Log.get().postWarning("The chest tile entity is missing!");
        else
            Log.get().postDebug(c.toString());
    }
    
}
