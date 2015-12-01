package com.stabilise.world.tile;

import com.stabilise.world.tile.tileentity.TileEntity;
import com.stabilise.world.tile.tileentity.TileEntityMobSpawner;

/**
 * Mob spawner tile. Has an associated tile entity.
 * 
 * @see TileEntityMobSpawner
 */
public class TileMobSpawner extends TileTE {
    
    public TileMobSpawner(TileBuilder b) {
        super(b);
    }
    
    @Override
    public TileEntity createTE(int x, int y) {
        return new TileEntityMobSpawner(x, y);
    }
    
}
