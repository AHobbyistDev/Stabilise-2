package com.stabilise.world.tile;

import com.stabilise.entity.Entity;
import com.stabilise.entity.Position;
import com.stabilise.util.Log;
import com.stabilise.world.World;
import com.stabilise.world.tile.tileentity.TileEntityChest;

/**
 * A chest tile.
 */
public class TileChest extends TileTE<TileEntityChest> {
    
    /**
     * Creates a chest tile.
     */
    TileChest(TileBuilder b) {
        super(b);
    }
    
    @Override
    public TileEntityChest createTE() {
        return new TileEntityChest();
    }
    
    @Override
    public void handleInteract(World world, Position pos, Entity e) {
        super.handleInteract(world, pos, e);
        
        TileEntityChest c = getTE(world, pos);
        // TODO: temporary
        if(c == null)
            Log.get().postWarning("The chest tile entity is missing!");
        else
            Log.get().postDebug(c.toString());
    }
    
}
