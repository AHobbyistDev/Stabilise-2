package com.stabilise.world;

import com.stabilise.entity.Position;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.tile.Tile;
import com.stabilise.world.tile.Tiles;
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
    public static final float SLICE_SIZEf = (float) SLICE_SIZE;
    /** {@link #SLICE_SIZE} - 1; minor optimisation purposes. */
    public static final int SLICE_SIZE_MINUS_ONE = SLICE_SIZE - 1;
    /** The power of 2 of {@link #SLICE_SIZE}; minor optimisation purposes. */
    public static final int SLICE_SIZE_SHIFT = Maths.log2(SLICE_SIZE);
    /** See {@link Position#tileCoordRelativeToSliceFromTileCoordFree(double)}. */
    public static final double SLICE_SIZE_MINUS_EPSd = 15.9999995d; // TODO: calculate better epsilon
    /** See {@link Position#tileCoordRelativeToSliceFromTileCoordFree2(float)}. */
    public static final float SLICE_SIZE_MINUS_EPSf = 15.9999995f;
    
    /** Dummy slice to indicate the lack of a slice in preference to a null
     * pointer. */
    public static final Slice DUMMY_SLICE = new DummySlice();
    
    //--------------------==========--------------------
    //-------------=====Member Variables=====-----------
    //--------------------==========--------------------
    
    /** This slice's coordinates, in slice-lengths. */
    public final int x, y;
    
    /** The tiles within this slice. This is visible for convenience purposes.
     * <br>Tiles are indexed in the form [y][x]. */
    public final int[][] tiles;
    /** The walls within this slice. Visible for convenience purposes.
     * <br>Indexed in the form [y][x]. */
    public final int[][] walls;
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
     */
    public Slice(int x, int y) {
        this(x, y, new int[SLICE_SIZE][SLICE_SIZE], new int[SLICE_SIZE][SLICE_SIZE],
                new byte[SLICE_SIZE][SLICE_SIZE]);
    }
    
    /**
     * Creates a new slice. It is implicitly trusted that the given arrays are
     * legal.
     * 
     * @param x The x-coordinate of the slice, in slice-lengths.
     * @param y The y-coordinate of the slice, in slice-lengths.
     * @param tiles The slice's tiles.
     * @param walls The slice's walls.
     * @param light The slice's light values.
     */
    public Slice(int x, int y, int[][] tiles, int[][] walls, byte[][] light) {
        this.x = x;
        this.y = y;
        this.tiles = tiles;
        this.walls = walls;
        this.light = light;
    }
    
    /**
     * Creates a new slice. The given arrays are unpacked into their respective
     * 2D arrays.
     * 
     * @param x The x-coordinate of the slice, in slice-lengths.
     * @param y The y-coordinate of the slice, in slice-lengths.
     * @param tiles The slice's tiles.
     * @param walls The slice's walls.
     * @param light The slice's light values.
     * 
     * @throws NullPointerException if {@code tiles} is {@code null}.
     */
    public Slice(int x, int y, int[] tiles, int[] walls, byte[] light) {
        this(x, y, to2DArray(tiles), to2DArray(walls), to2DArray(light));
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
     * @throws ArrayIndexOutOfBoundsException if either x or y is {@code < 0 ||
     * >= }{@link #SLICE_SIZE}.
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
     * @throws ArrayIndexOutOfBoundsException if either x or y is {@code < 0 ||
     * >= }{@link #SLICE_SIZE}.
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
     * @throws ArrayIndexOutOfBoundsException if either x or y is {@code < 0 ||
     * >= }{@link #SLICE_SIZE}.
     * @throws NullPointerException if {@code tile} is {@code null}.
     */
    public void setTileAt(int x, int y, Tile tile) {
        setTileIDAt(x, y, tile.getID());
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
     * @throws ArrayIndexOutOfBoundsException if either x or y is {@code < 0 ||
     * >= }{@link #SLICE_SIZE}.
     */
    public void setTileIDAt(int x, int y, int tileID) {
        tiles[y][x] = tileID;
    }
    
    public Tile getWallAt(int x, int y) {
        return Tile.getTile(getWallIDAt(x, y));
    }
    
    public int getWallIDAt(int x, int y) {
        return walls[y][x];
    }
    
    public void setWallAt(int x, int y, Tile tile) {
        setWallIDAt(x, y, tile.getID());
    }
    
    public void setWallIDAt(int x, int y, int tileID) {
        walls[y][x] = tileID;
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
        
        /*
        Tile t = getTileAt(x, y);
        
        byte oldLight = getLightAt(x, y);
        byte level = t.getLight();
        
        if(level == oldLight)
            return;
        else if(level > oldLight) {
            setLightAt(x, y, level);
            level -= t.getFalloff();
            if(x != 0)           spreadLightTo(x-1, y  , level, false);
            if(y != 0)           spreadLightTo(x  , y-1, level, false);
            if(x < SLICE_SIZE-1) spreadLightTo(x+1, y  , level, false);
            if(y < SLICE_SIZE-1) spreadLightTo(x  , y+1, level, false);
        } else {
            
        }
        */
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
    
    public static int[] to1DArray(int[][] sliceData) {
        int[] arr = new int[SLICE_SIZE * SLICE_SIZE];
        for(int r = 0; r < SLICE_SIZE; r++)
            System.arraycopy(sliceData[r], 0, arr, r * SLICE_SIZE, SLICE_SIZE);
        return arr;
    }
    
    public static byte[] to1DArray(byte[][] sliceData) {
        byte[] arr = new byte[SLICE_SIZE * SLICE_SIZE];
        for(int r = 0; r < SLICE_SIZE; r++)
            System.arraycopy(sliceData[r], 0, arr, r * SLICE_SIZE, SLICE_SIZE);
        return arr;
    }
    
    /**
     * Converts the specified int array into the 2D int array format used to
     * store tiles.
     */
    private static int[][] to2DArray(int[] sliceData) {
        int[][] arr = new int[SLICE_SIZE][SLICE_SIZE];
        for(int r = 0; r < SLICE_SIZE; r++)
            System.arraycopy(sliceData, r*SLICE_SIZE, arr[r], 0, SLICE_SIZE);
        return arr;
    }
    
    private static byte[][] to2DArray(byte[] sliceData) {
        byte[][] arr = new byte[SLICE_SIZE][SLICE_SIZE];
        for(int r = 0; r < SLICE_SIZE; r++)
            System.arraycopy(sliceData, r*SLICE_SIZE, arr[r], 0, SLICE_SIZE);
        return arr;
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
        for(int r = 0; r < SLICE_SIZE; r++) {
            for(int c = 0; c < SLICE_SIZE; c++) {
                if(tileEntities[r][c] != null) {
                    tileEntities[r][c].handleAdd(world);
                    world.addTileEntityToUpdateList(tileEntities[r][c]);
                }
            }
        }
    }
    
    public void buildLight() {
        for(int y = 0; y < SLICE_SIZE; y++) {
            for(int x = 0; x < SLICE_SIZE; x++) {
                byte l1 = getTileAt(x, y).getLight();
                byte l2 = getWallAt(x, y).getLight();
                setLightAt(x, y, l1 > l2 ? l1 : l2);
            }
        }
        
        for(int y = 0; y < SLICE_SIZE; y++) {
            for(int x = 0; x < SLICE_SIZE; x++) {
                byte l1 = getTileAt(x, y).getLight();
                byte l2 = getWallAt(x, y).getLight();
                spreadLightTo(x, y, l1 > l2 ? l1 : l2, true);
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
    
    /**
     * Checks for whether this slice is a dummy slice.
     */
    public boolean isDummy() {
        return false;
    }
    
    
    private static class DummySlice extends Slice {
        
        public DummySlice() {
            super(0, 0, new int[SLICE_SIZE][SLICE_SIZE],
                    new int[SLICE_SIZE][SLICE_SIZE],
                    new byte[SLICE_SIZE][SLICE_SIZE]);
            
            for(int y = 0; y < SLICE_SIZE; y++) {
                for(int x = 0; x < SLICE_SIZE; x++) {
                    tiles[y][x] = Tiles.barrier.getID();
                }
            }
        }
        
        @Override public int  getTileIDAt(int x, int y) { return 0; }
        @Override public void setTileIDAt(int x, int y, int tileID) {}
        @Override public int  getWallIDAt(int x, int y) { return 0; }
        @Override public void setWallIDAt(int x, int y, int tileID) {}
        @Override public byte getLightAt(int x, int y) { return 0; }
        @Override public void setLightAt(int x, int y, byte level) {}
        @Override public TileEntity getTileEntityAt(int x, int y) { return null; }
        @Override public void setTileEntityAt(int x, int y, TileEntity tileEntity) {}
        
        @Override
        public boolean isDummy() {
            return true;
        }
        
    }
    
}
