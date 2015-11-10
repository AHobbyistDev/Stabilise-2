package com.stabilise.world;

import java.util.Objects;

import com.stabilise.util.maths.Maths;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.tileentity.TileEntity;

/**
 * A slice represents a 16x16-tile chunk of the world.
 */
public class Slice {
    
    //--------------------==========--------------------
    //-----=====Static Constants and Variables=====-----
    //--------------------==========--------------------
    
    /** The length of an edge of the square of tiles in a slice. */
    public static final int SLICE_SIZE = 16; // must be a power of 2
    /** {@link SLICE_SIZE} - 1; minor optimisation purposes. */
    public static final int SLICE_SIZE_MINUS_ONE = SLICE_SIZE - 1;
    /** The power of 2 of {@link SLICE_SIZE}; minor optimisation purposes. */
    public static final int SLICE_SIZE_SHIFT = Maths.log2(SLICE_SIZE);
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** This slice's coordinates, in slice-lengths. */
    public final int x, y;
    
    /** The tiles within this slice. This is visible for convenience purposes.
     * <br>Tiles are indexed in the form [y][x]. */
    public final int[][] tiles;
    /** The light levels within this slice.
     * <br>Indexed in the form [y][x]. */
    public final byte[][] light;
    
    /** The tile entities within the slice. This is public for convenience
     * purposes, but should generally not be interacted with.
     * <br>Tile entities are indexed in the form [y][x].
     * <br>This is lazily initialised - that is, {@code null} until a tile
     * entity is added to this slice. */
    public TileEntity[][] tileEntities;
    
    
    /**
     * Creates a new slice.
     * 
     * @param x The x-coordinate of the slice, in slice-lengths.
     * @param y The y-coordinate of the slice, in slice-lengths.
     * @param tiles The slice's tiles.
     * 
     * @throws NullPointerException if {@code tiles} is {@code null}.
     */
    public Slice(int x, int y, int[][] tiles) {
        this.x = x;
        this.y = y;
        this.tiles = Objects.requireNonNull(tiles);
        this.light = new byte[SLICE_SIZE][SLICE_SIZE];
    }
    
    /**
     * Creates a new slice.
     * 
     * @param x The x-coordinate of the slice, in slice-lengths.
     * @param y The y-coordinate of the slice, in slice-lengths.
     * @param tiles The slice's tiles, as would be returned by {@link
     * #getTilesAsIntArray()}. This array is unpacked into a 2D array.
     * 
     * @throws NullPointerException if {@code tiles} is {@code null}.
     */
    public Slice(int x, int y, int[] tiles) {
        this(x, y, getTilesFromArray(tiles));
    }
    
    /**
     * Gets a tile from the specified coordinates relative to this slice.
     * 
     * @param x The x-coordinate of the tile relative to this slice, in
     * tile-lengths.
     * @param y The y-coordinate of the tile relative to this slice, in
     * tile-lengths.
     * 
     * @return The tile at the specified coordinates.
     * @throws ArrayIndexOutOfBoundsException if either x or y is negative or
     * greater than 15.
     */
    public Tile getTileAt(int x, int y) {
        return Tile.getTile(tiles[y][x]);
    }
    
    /**
     * Gets the ID of the tile from the specified coordinates relative to this
     * slice.
     * 
     * @param x The x-coordinate of the tile relative to this slice, in
     * tile-lengths.
     * @param y The y-coordinate of the tile relative to this slice, in
     * tile-lengths.
     * 
     * @return The ID of the tile at the specified coordinates.
     * @throws ArrayIndexOutOfBoundsException if either x or y is negative or
     * greater than 15.
     */
    public int getTileIDAt(int x, int y) {
        return tiles[y][x];
    }
    
    /**
     * Sets a tile in the slice.
     * 
     * @param x The x-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param y The y-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param tile The tile.
     * 
     * @throws ArrayIndexOutOfBoundsException if either x or y are < 0 or > 15.
     * @throws NullPointerException if {@code tile} is {@code null}.
     */
    public void setTileAt(int x, int y, Tile tile) {
        tiles[y][x] = tile.getID();
    }
    
    /**
     * Sets a tile in the slice.
     * 
     * @param x The x-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param y The y-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param tileID The ID of the tile.
     * 
     * @throws ArrayIndexOutOfBoundsException if either x or y are < 0 or > 15.
     */
    public void setTileAt(int x, int y, int tileID) {
        tiles[y][x] = tileID;
    }
    
    public byte getLightAt(int x, int y) {
        return light[y][x];
    }
    
    public void setLightAt(int x, int y, byte level) {
        light[y][x] = level;
    }
    
    public void updateLight(int x, int y) {
        //spreadLightTo(x, y, getTileAt(x, y).getLight(), false);
        buildLight(); // 10/10 easy solution
    }
    
    /**
     * Gets a tile entity at the specified coordinates.
     * 
     * @param x The x-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param y The y-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * 
     * @return The tile entity at the specified coordinates, or {@code null}
     * if no such tile entity exists.
     * @throws ArrayIndexOutOfBoundsException if either x or y is negative or
     * greater than 15.
     */
    public TileEntity getTileEntityAt(int x, int y) {
        return tileEntities == null ? null : tileEntities[y][x];
    }
    
    /**
     * Sets a tile entity at the specified coordinates.
     * 
     * @param x The x-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param y The y-coordinate of the tile relative to the slice, in
     * tile-lengths.
     * @param tileEntity The tile entity. Setting this to {@code null} will
     * remove the tile entity at the specified location, if it exists.
     * 
     * @throws ArrayIndexOutOfBoundsException if either x or y is negative or
     * greater than 15.
     */
    public void setTileEntityAt(int x, int y, TileEntity tileEntity) {
        if(tileEntity != null)
            initTileEntities();
        tileEntities[y][x] = tileEntity;
    }
    
    /**
     * Gets the slice's tiles in the form of a 1D integer array.
     */
    public int[] getTilesAsIntArray() {
        int[] tileArray = new int[SLICE_SIZE * SLICE_SIZE];
        for(int r = 0; r < SLICE_SIZE; r++)
            System.arraycopy(tiles[r], 0, tileArray, r * SLICE_SIZE, SLICE_SIZE);
        return tileArray;
    }
    
    /**
     * Converts the specified int array into the 2D int array format used to
     * store tiles.
     */
    private static int[][] getTilesFromArray(int[] tileArray) {
        int[][] t = new int[SLICE_SIZE][SLICE_SIZE];
        for(int r = 0; r < SLICE_SIZE; r++)
            System.arraycopy(tileArray, r*SLICE_SIZE, t[r], 0, SLICE_SIZE);
        return t;
    }
    
    /**
     * Initialises {@link #tileEntities} if it is {@code null}.
     */
    public void initTileEntities() {
        if(tileEntities == null)
            tileEntities = new TileEntity[SLICE_SIZE][SLICE_SIZE];
    }
    
    /**
     * Adds any entities contained by this slice to the world.
     */
    void importEntities(AbstractWorld world) {
        // TODO
    }
    
    /**
     * Adds any tile entities contained by this slice to the world.
     */
    void importTileEntities(AbstractWorld world) {
        if(tileEntities == null)
            return;
        for(int r = 0; r < SLICE_SIZE; r++)
            for(int c = 0; c < SLICE_SIZE; c++)
                if(tileEntities[r][c] != null)
                    world.addTileEntity(tileEntities[r][c]);
    }
    
    public void buildLight() {
        for(int y = 0; y < SLICE_SIZE; y++) {
            for(int x = 0; x < SLICE_SIZE; x++) {
                setLightAt(x, y, getTileAt(x, y).getLight());
            }
        }
        
        for(int y = 0; y < SLICE_SIZE; y++) {
            for(int x = 0; x < SLICE_SIZE; x++) {
                spreadLightTo(x, y, getTileAt(x, y).getLight(), true);
            }
        }
    }
    
    private void spreadLightTo(int x, int y, byte level, boolean src) {
        if(!src) {
            if(level < getLightAt(x, y))
                return;
            setLightAt(x, y, level);
        }
        level -= getTileAt(x, y).getFalloff();
        if(x != 0)           spreadLightTo(x-1, y  , level, false);
        if(y != 0)           spreadLightTo(x  , y-1, level, false);
        if(x < SLICE_SIZE-1) spreadLightTo(x+1, y  , level, false);
        if(y < SLICE_SIZE-1) spreadLightTo(x  , y+1, level, false);
    }
    
    @Override
    public String toString() {
        return "Slice[" + x + "," + y + "]";
    }
    
}
