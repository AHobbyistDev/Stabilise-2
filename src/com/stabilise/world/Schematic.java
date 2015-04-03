package com.stabilise.world;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.core.Resources;
import com.stabilise.util.ArrayUtil;
import com.stabilise.util.nbt.NBTIO;
import com.stabilise.util.nbt.NBTTagCompound;

/**
 * A schematic is a structure blueprint.
 * 
 * <p>Currently schematics may be no larger than 32,767 tiles in width or
 * height, though it's highly unlikely one would find a use for schematics of
 * such dimensions anyway.
 */
public class Schematic {
	
	/** The schematic's name. */
	public String name = "";
	/** The schematic's tiles. */
	public int[][] tiles;
	/** The schematic's origin's x/y-coordinate. */
	public int x, y;
	/** The schematic's width/height. */
	public short width, height;
	
	
	/**
	 * Creates a new empty schematic.
	 */
	public Schematic() {
		// le nothing
	}
	
	/**
	 * Creates a new schematic.
	 * 
	 * @param name The schematic's name.
	 */
	public Schematic(String name) {
		this(name, null);
	}
	
	/**
	 * Creates a new schematic.
	 * 
	 * @param tiles The schematic's tiles.
	 */
	public Schematic(int[][] tiles) {
		this("", tiles);
	}
	
	/**
	 * Creates a new schematic.
	 * 
	 * @param name The schematic's name.
	 * @param tiles The schematic's files.
	 */
	public Schematic(String name, int[][] tiles) {
		this(name, tiles, 0, 0);
	}
	
	/**
	 * Creates a new schematic.
	 * 
	 * @param name The schematic's name.
	 * @param tiles The schematic's files.
	 * @param x The x-coordinate of the schematic's pivot.
	 * @param y The y-coordinate of the schematic's pivot.
	 */
	public Schematic(String name, int[][] tiles, int x, int y) {
		this.name = name;
		this.tiles = tiles;
		this.x = x;
		this.y = y;
		
		if(tiles != null) {
			height = (byte)tiles.length;
			width = (byte)tiles[0].length;
		}
	}
	
	/**
	 * Gets the ID of the tile at the specified coordinates.
	 * 
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * 
	 * @return The ID of the tile at the specified coordinates.
	 * @throws NullPointerException if the tiles haven't been set.
	 * @throws ArrayIndexOutOfBoundsException if the specified tile
	 * coordinates are out of bounds.
	 */
	public int getTileAt(int x, int y) {
		return tiles[y][x];
	}
	
	/**
	 * Gets the schematic's file.
	 * 
	 * @return The schematic's file, or {@code null} if the schematic lacks a
	 * name.
	 */
	private FileHandle getFile() {
		if(name == "")
			return null;
		
		return Resources.SCHEMATIC_DIR.child(name + ".schematic");
	}
	
	/**
	 * Loads this schematic.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void load() throws IOException {
		FileHandle file = getFile();
		
		if(file == null)
			throw new IOException("Attempting to load an unnamed schematic!");
		
		NBTTagCompound schematicTag = NBTIO.readCompressed(file);
		
		height = schematicTag.getShort("height");
		width = schematicTag.getShort("width");
		
		int[] tiles1D = schematicTag.getIntArray("tiles");
		tiles = new int[height][width];
		
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				tiles[r][c] = tiles1D[r * width + c];
			}
		}
		
		x = schematicTag.getInt("x");
		y = schematicTag.getInt("y");
	}
	
	/**
	 * Saves the schematic.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void save() throws IOException {
		FileHandle file = getFile();
		
		if(file == null)
			throw new IOException("Attempting to save an unnamed schematic!");
		
		NBTTagCompound schematicTag = new NBTTagCompound();
		
		//byte height = (byte)tiles.length;
		//byte width = (byte)tiles[0].length;
		
		schematicTag.addShort("height", height);
		schematicTag.addShort("width", width);
		
		int[] tiles1D = new int[height * width];
		
		for(int r = 0; r < height; r++) {
			for(int c = 0; c < width; c++) {
				tiles1D[r * width + c] = tiles[r][c];
			}
		}
		
		schematicTag.addIntArray("tiles", tiles1D);
		
		schematicTag.addInt("x", x);
		schematicTag.addInt("y", y);
		
		NBTIO.writeCompressed(file, schematicTag);
	}
	
	@Override
	public String toString() {
		return "Schematic[" + name + "]";
	}
	
	// Some preconstructed schematics
	
	public static final Schematic TREE_1 = new Schematic("tree_1", ArrayUtil.flip2DIntArray(new int[][] {
			{ -1,-1, 5, 5, 5,-1,-1 },
			{ -1, 5, 5, 5, 5, 5,-1 },
			{  5, 5, 5, 4, 5, 5, 5 },
			{  5, 5, 5, 4, 5, 5, 5 },
			{ -1,-1,-1, 4,-1,-1,-1 },
			{ -1,-1,-1, 4,-1,-1,-1 },
			{ -1,-1,-1, 4,-1,-1,-1 },
			{ -1,-1,-1, 4,-1,-1,-1 },
			{ -1,-1,-1, 2,-1,-1,-1 },
	}), 3, 0);
	
}
