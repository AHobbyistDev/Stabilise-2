package com.stabilise.world;

import static com.stabilise.world.World.*;

import java.util.Random;

import com.stabilise.entity.Entity;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * Defines methods which summarise a world implementation.
 */
public interface WorldProvider {
    
    /**
     * Adds an entity to the world. The entity's ID is assigned automatically.
     * 
     * <p>The entity is not added to the world immediately; rather, it is added
     * at the end of the current tick. This is intended as to prevent a {@code
     * ConcurrentModificationException} during iteration.
     * 
     * @param e The entity.
     * @param x The x-coordinate at which to place the entity, in tile-lengths.
     * @param y The y-coordinate at which to place the entity, in tile-lengths.
     */
    default void addEntity(Entity e, double x, double y) {
        e.x = x;
        e.y = y;
        addEntity(e);
    }
    
    /**
     * Adds an entity to the world. The entity's ID is assigned automatically.
     * 
     * <p>The entity is not added to the world immediately; rather, it is added
     * at the end of the current tick. This is intended as to prevent a {@code
     * ConcurrentModificationException} during iteration.
     */
    void addEntity(Entity e);
    
    // ==========World component getters and setters==========
    
    /**
     * Gets the slice at the given coordinates.
     * 
     * @param x The slice's x-coordinate, in slice lengths.
     * @param y The slice's y-coordinate, in slice lengths.
     * 
     * @return The slice at the given coordinates, or {@link Slice#DUMMY_SLICE}
     * if no such slice is loaded.
     */
    Slice getSliceAt(int x, int y);
    
    /**
     * Gets the slice at the given coordinates.
     * 
     * @param x The slice's x-coordinate, in tile lengths.
     * @param y The slice's y-coordinate, in tile lengths.
     * 
     * @return The slice at the given coordinates, or {@link Slice#DUMMY_SLICE}
     * if no such slice is loaded.
     */
    default Slice getSliceAtTile(int x, int y) {
        // This should be optimised for worlds which deal with regions
        return getSliceAt(
                sliceCoordFromTileCoord(x),
                sliceCoordFromTileCoord(y));
    }
    
    /**
     * Gets a tile at the given coordinates. Fractional coordinates are rounded
     * down.
     * 
     * @param x The x-coordinate of the tile, in tile-lengths.
     * @param y The y-coordinate of the tile, in tile-lengths.
     * 
     * @return The tile at the given coordinates, or the
     * {@link Tiles#barrier invisible bedrock} tile if no such tile
     * is loaded.
     */
    default Tile getTileAt(double x, double y) {
        return getTileAt(
                tileCoordFreeToTileCoordFixed(x),
                tileCoordFreeToTileCoordFixed(y));
    }
    
    /**
     * Gets a tile at the given coordinates.
     * 
     * @param x The x-coordinate of the tile, in tile-lengths.
     * @param y The y-coordinate of the tile, in tile-lengths.
     * 
     * @return The tile at the given coordinates, or the
     * {@link com.stabilise.world.tile.Tiles#barrier invisible
     * bedrock} tile if no such tile is loaded.
     */
    default Tile getTileAt(int x, int y) {
        return getSliceAtTile(x, y).getTileAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y)
        );
    }
    
    /**
     * Sets the tile at the specified coordinates.
     * 
     * @param x The x-coordinate of the tile, in tile-lengths.
     * @param y The y-coordinate of the tile, in tile-lengths.
     * @param tile The tile to set.
     * 
     * @throws NullPointerException if {@code tile} is {@code null}.
     */
    default void setTileAt(int x, int y, Tile tile) {
        setTileAt(x, y, tile.getID());
    }
    
    /**
     * Sets a tile at the specified coordinates.
     * 
     * @param x The x-coordinate of the tile, in tile-lengths.
     * @param y The y-coordinate of the tile, in tile-lengths.
     * @param id The ID of the tile to set.
     */
    void setTileAt(int x, int y, int id);
    
    default Tile getWallAt(int x, int y) {
        return getSliceAtTile(x, y).getWallAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y)
        );
    }
    
    default void setWallAt(int x, int y, Tile wall) {
        getSliceAtTile(x, y).setWallAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y),
                wall
        );
    }
    
    default void setWallAt(int x, int y, int id) {
        getSliceAtTile(x, y).setWallIDAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y),
                id
        );
    }
    
    default byte getLightAt(int x, int y) {
        return getSliceAtTile(x, y).getLightAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y)
        );
    }
    
    default void setLightAt(int x, int y, byte light) {
        getSliceAtTile(x, y).setLightAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y),
                light
        );
    }
    
    /**
     * Gets the tile entity at the given coordinates.
     * 
     * @param x The x-coordinate of the tile, in tile-lengths.
     * @param y The y-coordinate of the tile, in tile-lengths.
     * 
     * @return The tile entity at the given coordinates, or {@code null} if no
     * such tile entity is loaded.
     */
    default TileEntity getTileEntityAt(int x, int y) {
        return getSliceAtTile(x, y).getTileEntityAt(
                tileCoordRelativeToSliceFromTileCoord(x),
                tileCoordRelativeToSliceFromTileCoord(y)
        );
    }
    
    /**
     * Sets a tile entity at the given coordinates.
     * 
     * @param x The x-coordinate of the tile at which to place the tile entity,
     * in tile-lengths.
     * @param y The y-coordinate of the tile at which to place the tile entity,
     * in tile-lengths.
     * @param t The tile entity. Setting this to {@code null} will remove the
     * tile entity at the specified location, if it exists.
     */
    void setTileEntityAt(int x, int y, TileEntity t);
    
    /**
     * Removes a tile entity at the given coordinates. Invoking this method is
     * equivalent to invoking {@link #setTileEntityAt(int, int, TileEntity)}
     * with a {@code null} parameter.
     * 
     * @param x The x-coordinate of the tile at which the tile entity to remove
     * is placed.
     * @param y The y-coordinate of the tile at which the tile entity to remove
     * is placed.
     */
    default void removeTileEntityAt(int x, int y) {
        setTileEntityAt(x, y, null);
    }
    
    // ========== Utility Methods ==========
    
    /**
     * Utility method that provides a Random.
     */
    Random rnd();
    
    /**
     * Returns {@code true} {@code 1/n}<sup><font size=-1>th</font></sup> of
     * the time. Equivalent to {@code rnd().nextInt(n) == 0}.
     */
    default boolean chance(int n) {
        return rnd().nextInt(n) == 0;
    }
    
}
