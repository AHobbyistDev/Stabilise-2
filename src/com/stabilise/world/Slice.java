package com.stabilise.world;

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
	public static final int SLICE_SIZE = 16;
	/** {@link SLICE_SIZE} - 1; minor optimisation purposes. */
	public static final int SLICE_SIZE_MINUS_ONE = 15;
	/** The power of 2 of {@link SLICE_SIZE}; minor optimisation purposes. */
	public static final int SLICE_SIZE_SHIFT = 4;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The slice's coordinates, in slice-lengths. */
	public final int x, y;
	
	/** The tiles within the slice.
	 * <br>Tiles are indexed in the form [y][x].
	 * <br>Note that this may be {@code null}. */
	public int[][] tiles;
	
	/** The tile entities within the slice.
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
		this(x, y, null);
	}
	
	/**
	 * Creates a new slice.
	 * 
	 * @param x The x-coordinate of the slice, in slice-lengths.
	 * @param y The y-coordinate of the slice, in slice-lengths.
	 * @param tiles The slice's tiles.
	 */
	public Slice(int x, int y, int[][] tiles) {
		this.x = x;
		this.y = y;
		this.tiles = tiles;
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
	 * @param tileEntity The tile entity.
	 * 
	 * @throws NullPointerException if {@code tileEntity} is {@code null}.
	 * @throws ArrayIndexOutOfBoundsException if either x or y is negative or
	 * greater than 15.
	 */
	public void setTileEntityAt(int x, int y, TileEntity tileEntity) {
		if(tileEntity == null)
			throw new NullPointerException();
		initTileEntities();
		tileEntities[y][x] = tileEntity;
	}
	
	/**
	 * Removes the tile entity at the specified coordinates.
	 * 
	 * @param x The x-coordinate of the tile relative to the slice, in
	 * tile-lengths.
	 * @param y The y-coordinate of the tile relative to the slice, in
	 * tile-lengths.
	 * 
	 * @throws NullPointerException if there are no tile entities in this
	 * slice. This should only be used to weed out programming bugs wherein
	 * tile entities may be removed unnecessarily.
	 * @throws ArrayIndexOutOfBoundsException if either x or y is negative or
	 * greater than 15.
	 */
	public void removeTileEntityAt(int x, int y) {
		// Even if this eliminates the last tile entity from this slice, we do
		// not nullify tileEntities as to prevent a sequence of allocation and
		// deallocation on tile entity addition and removal.
		tileEntities[y][x] = null;
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
	 * Sets the slice's tiles.
	 * 
	 * @param tileArray The tiles, in the form of a 1D integer array. The array
	 * will be converted to a suitable 2D array for tile storage.
	 */
	public void setTilesAsIntArray(int[] tileArray) {
		tiles = new int[SLICE_SIZE][SLICE_SIZE];
		for(int r = 0; r < SLICE_SIZE; r++)
			System.arraycopy(tileArray, r*SLICE_SIZE, tiles[r], 0, SLICE_SIZE);
	}
	
	/**
	 * Initialises {@link #tileEntities} if it is {@code null}.
	 */
	public void initTileEntities() {
		if(tileEntities == null)
			tileEntities = new TileEntity[SLICE_SIZE][SLICE_SIZE];
	}
	
	/**
	 * Adds any entities and tile entities contained by the slice to the world.
	 * 
	 * @param world The world.
	 */
	public void addContainedEntitiesToWorld(AbstractWorld world) {
		if(tileEntities == null)
			return;
		// TODO: A more efficient method of finding tile entities may be preferable
		for(int r = 0; r < SLICE_SIZE; r++)
			for(int c = 0; c < SLICE_SIZE; c++)
				if(tileEntities[r][c] != null)
					world.addTileEntity(tileEntities[r][c]);
	}
	
	@Override
	public String toString() {
		return "Slice[" + x + "," + y + "]";
	}
	
}
