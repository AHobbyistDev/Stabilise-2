package com.stabilise.entity;

import static com.stabilise.world.World.*;

import com.stabilise.opengl.render.WorldRenderer;
import com.stabilise.world.World;

/**
 * A FreeGameObject is a game object whose coordinates are not constrained to
 * the coordinate grid of the world, and generally move freely.
 */
public class FreeGameObject extends GameObject {
    
    /** The GameObject's coordinates, in tile-lengths */
    public double x, y;
    
    
    /**
     * Creates a new FreeGameObject.
     */
    public FreeGameObject() {
        // nothing to see here, move along
    }
    
    /**
     * Creates a new FreeGameObject with the specified components.
     */
    public FreeGameObject(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public void update(World world) {}
    
    @Override
    public void render(WorldRenderer renderer) {}
    
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
