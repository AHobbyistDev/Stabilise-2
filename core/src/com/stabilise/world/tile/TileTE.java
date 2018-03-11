package com.stabilise.world.tile;

import com.stabilise.entity.Position;
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
     * #handlePlace(World, Position)}.
     * 
     * @return The tile entity. Never null.
     */
    protected abstract T createTE();
    
    /**
     * Gets the tile entity at the given position.
     * 
     * @throws ClassCastException if the tile entity is not of type T.
     */
    @SuppressWarnings("unchecked")
    protected T getTE(WorldProvider world, Position pos) {
        return (T) world.getTileEntityAt(pos);
    }
    
    @Override
    public void handlePlace(WorldProvider world, Position pos) {
        super.handlePlace(world, pos);
        T t = createTE();
        t.pos.set(pos);
        world.setTileEntity(t);
    }
    
    @Override
    public void handleRemove(WorldProvider world, Position pos) {
        super.handleRemove(world, pos);
        world.removeTileEntityAt(pos);
    }
    
}
