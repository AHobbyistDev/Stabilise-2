package com.stabilise.entity;

import static com.stabilise.world.World.*;

/**
 * A FreeGameObject is a game object whose coordinates are not constrained to
 * the coordinate grid of the world, and generally move freely.
 */
public class FreeGameObject extends GameObject {
    
    /** The GameObject's coordinates, in tile-lengths */
    public double x, y;
    
    
    /**
     * Creates a new FreeGameObject with coordinates (0,0).
     */
    public FreeGameObject() {}
    
    /**
     * Creates a new FreeGameObject at the specified coordinates.
     * 
     * @param x The x-coordinate, in tile-lengths.
     * @param y The y-coordinate, in tile lengths.
     */
    public FreeGameObject(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public final double getX() {
        return x;
    }
    
    @Override
    public final double getY() {
        return y;
    }
    
    @Override
    public final int getTileX() {
        return tileCoordFreeToTileCoordFixed(x);
    }
    
    @Override
    public final int getTileY() {
        return tileCoordFreeToTileCoordFixed(y);
    }
    
    @Override
    public final int getSliceX() {
        return sliceCoordFromTileCoord(x);
    }
    
    @Override
    public final int getSliceY() {
        return sliceCoordFromTileCoord(y);
    }

}
