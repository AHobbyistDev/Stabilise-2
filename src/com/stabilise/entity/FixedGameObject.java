package com.stabilise.entity;

import static com.stabilise.world.World.*;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A FixedGameObject is a game object with grid (integer) coordinates.
 */
public class FixedGameObject extends GameObject {
    
    /** The GameObject's coordinates, in tile-lengths. */
    public int x, y;
    
    
    /**
     * Creates a new FixedGameObject with coordinates (0,0).
     */
    public FixedGameObject() {}
    
    /**
     * @param x The x-coordinate, in tile-lengths.
     * @param y The y-coordinate, in tile lengths.
     */
    public FixedGameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void update(World world) {}
    
    @Override
    public void render(WorldRenderer renderer) {}
    
    @Override
    public final double getX() {
        return tileCoordFixedToTileCoordFree(x);
    }
    
    @Override
    public final double getY() {
        return tileCoordFixedToTileCoordFree(y);
    }
    
    @Override
    public final int getTileX() {
        return x;
    }
    
    @Override
    public final int getTileY() {
        return y;
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
