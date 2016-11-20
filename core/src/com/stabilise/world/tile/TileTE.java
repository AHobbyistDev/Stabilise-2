package com.stabilise.world.tile;

import com.stabilise.world.World;
import com.stabilise.world.WorldProvider;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * A TileTE (Tile-TileEntity) is simply a tile with an associated tile entity.
 * This class is provided to make implementation of tile entities a little
 * easier.
 */
public abstract class TileTE<T extends TileEntity> extends Tile {
    
    TileTE(TileBuilder b) {
        super(b);
    }
    
    /**
     * Creates the TileEntity. This is invoked by {@link
     * #handlePlace(World, int, int)}.
     * 
     * @param x The x-coordinate, in tile-lengths.
     * @param y The y-coordinate, in tile-lengths.
     * 
     * @return The tile entity. Never null.
     */
    protected abstract T createTE(int x, int y);
    
    /**
     * Gets the tile entity at (x,y).
     * 
     * @param x The x-coordinate, in tile-lengths.
     * @param y The y-coordinate, in tile-lengths.
     * 
     * @throws ClassCastException if the tile entity is not of type T.
     */
    @SuppressWarnings("unchecked")
    protected T getTE(WorldProvider world, int x, int y) {
        return (T) world.getTileEntityAt(x, y);
    }
    
    @Override
    public void handlePlace(WorldProvider world, int x, int y) {
        super.handlePlace(world, x, y);
        world.setTileEntityAt(x, y, createTE(x, y));
    }
    
    @Override
    public void handleRemove(WorldProvider world, int x, int y) {
        super.handleRemove(world, x, y);
        world.removeTileEntityAt(x, y);
    }
    
}
