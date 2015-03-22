package com.stabilise.world.tile;

import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.util.collect.RegistryNamespacedDefaulted;
import com.stabilise.world.World;

/**
 * The fundamental building block of a world.
 */
public class Tile {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The registry of all tiles in the game. The default tile is the air
	 * tile. */
	private static final RegistryNamespacedDefaulted<Tile> TILES =
			new RegistryNamespacedDefaulted<>("TileRegistry", "stabilise", "air", 32);
	
	// Template values for hardness
	/** Template hardness values for different tile types. */
	protected static final float
			H_DIRT = 1.0f,
			H_STONE = 10.0f,
			H_WOOD = 3.0f,
			H_INVULNERABLE = Float.MAX_VALUE;
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The tile's ID. */
	private int id;
	/** The tile's name. */
	private String name;
	
	/** Whether or not the tile is solid. */
	protected boolean solid = true;
	/** The tile's hardness. */
	protected float hardness = H_STONE;
	/** The tile's frictive force, from 0 to 1. */
	protected float friction = 0.15f;			// TODO arbitrary default friction value
	
	
	/**
	 * Creates a tile.
	 */
	protected Tile() {
		// nothing to see here, move along
	}
	
	/**
	 * Updates the tile.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void update(World world, int x, int y) {
		// nothing to see here in the default implementation
	}
	
	/**
	 * Handles being placed in the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void handlePlace(World world, int x, int y) {
		// TODO
	}
	
	/**
	 * Handles being removed from the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void handleRemove(World world, int x, int y) {
		// TODO
	}
	
	/**
	 * Handles being broken. {@link #handleRemove(World, int, int)} is invoked
	 * in addition to any functionality here.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void handleBreak(World world, int x, int y) {
		handleRemove(world, x, y);
		
		/*
		EntityItem e = new EntityItem(world, item, 1);
		e.dx = world.rng.nextFloat() * 0.4f - 0.2f;
		e.dy = 0.1f + world.rng.nextFloat() * 0.2f;
		world.addEntity(e, x + 0.5, y + 0.5);
		*/
	}
	
	/**
	 * Handles the tile being stepped on by an entity.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param e The entity which stepped on the tile.
	 */
	public void handleStep(World world, int x, int y, Entity e) {
		// TODO
	}
	
	/**
	 * Handles the tile being overlapped by an entity.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param e The entity which is overlapping the tile.
	 */
	public void handleOverlap(World world, int x, int y, Entity e) {
		// TODO
	}
	
	/**
	 * Handles being touched, as per a left-click.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param mob The mob to touch the tile.
	 */
	public void handleTouch(World world, int x, int y, EntityMob mob) {
		// nothing in the default implementation
	}
	
	/**
	 * Handles being interacted with, as per a right-click.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 * @param mob The mob to interact with the tile.
	 */
	public void handleInteract(World world, int x, int y, EntityMob mob) {
		// nothing in the default implementation
	}
	
	/**
	 * Sets the tile's friction value.
	 * 
	 * @param friction The tile's desired friction value.
	 * 
	 * @return The tile, for chaining operations.
	 */
	protected Tile setFriction(float friction) {
		this.friction = friction;
		return this;
	}
	
	/**
	 * Sets the tile's hardness value.
	 * 
	 * @param hardness The tile's desired hardness value.
	 * 
	 * @return The tile, for chaining operations.
	 */
	protected Tile setHardness(float hardness) {
		this.hardness = hardness;
		return this;
	}
	
	
	/**
	 * Gets this tile's ID.
	 */
	public int getID() {
		return id;
	}
	
	/** 
	 * Gets this tile's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns {@code true} if this tile is solid; {@code false} otherwise.
	 */
	public boolean isSolid() {
		return solid;
	}
	
	/**
	 * Gets this tile's frictive value.
	 * 
	 * <p>TODO: define 'frictive value'.
	 */
	public float getFriction() {
		return friction;
	}
	
	/**
	 * Gets this tile's hardness.
	 */
	public float getHardness() {
		return hardness;
	}
	
	@Override
	public String toString() {
		return name + " [" + id + "]";
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Returns the tile with the specified ID, or {@link Tiles#AIR} if no such
	 * tile exists.
	 */
	public static Tile getTile(int id) {
		return TILES.get(id);
	}
	
	/**
	 * Returns the tile with the specified name, or {@link Tiles#AIR} if no
	 * such tile exists.
	 */
	public static Tile getTile(String name) {
		return TILES.get(name);
	}
	
	/**
	 * Registers all tiles.
	 * 
	 * @throws IllegalStateException if this method has already been invoked.
	 */
	public static void registerTiles() {
		registerTile(0, "air", new TileAir());
		registerTile(1, "void", new Tile().setHardness(H_INVULNERABLE));
		registerTile(2, "bedrock", new Tile().setHardness(H_INVULNERABLE));
		registerTile(3, "invisibleBedrock", new Tile().setHardness(H_INVULNERABLE));
		registerTile(4, "stone", new Tile().setHardness(H_STONE));
		registerTile(5, "dirt", new Tile().setHardness(H_DIRT));
		registerTile(6, "grass", new TileGrass().setHardness(H_DIRT));
		registerTile(7, "wood", new Tile().setHardness(H_WOOD));
		registerTile(8, "leaves", new Tile().setHardness(H_DIRT));
		registerTile(9, "planks", new Tile().setHardness(H_WOOD));
		registerTile(10, "water", new TileFluid().setViscosity(0.12f));
		registerTile(11, "lava", new TileFluid().setViscosity(0.8f));
		registerTile(12, "ice", new Tile().setHardness(H_DIRT).setFriction(0.008f));
		registerTile(13, "stoneBrick", new Tile().setHardness(H_STONE));
		registerTile(14, "oreIron", new TileOre().setHardness(H_STONE));
		registerTile(15, "oreCopper", new TileOre().setHardness(H_STONE));
		registerTile(16, "oreGold", new TileOre().setHardness(H_STONE));
		registerTile(17, "oreSilver", new TileOre().setHardness(H_STONE));
		registerTile(18, "oreDiamond", new TileOre().setHardness(H_STONE));
		registerTile(19, "chest", new TileChest().setHardness(H_WOOD));
		registerTile(20, "mobSpawner", new TileMobSpawner());
		
		TILES.lock();
	}
	
	/**
	 * Registers a tile. This should only be called during initialisation of
	 * the game.
	 * 
	 * @param id The ID with which to register the tile.
	 * @param name The name of the tile.
	 * @param tile The tile.
	 * 
	 * @throws IllegalStateException if no more tiles may be registered.
	 */
	private static void registerTile(int id, String name, Tile tile) {
		TILES.register(id, name, tile);
		tile.id = id;
		tile.name = name;
	}
	
	/**
	 * @return {@code true} if the tiles have been registered; {@code false}
	 * otherwise.
	 */
	static boolean isRegistered() {
		return TILES.isLocked();
	}
	
}