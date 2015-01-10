package com.stabilise.world.tile;

import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityMob;
import com.stabilise.util.collect.RegistryNamespacedDefaulted;
import com.stabilise.world.BaseWorld;

/**
 * The fundamental building block of a world.
 */
public class Tile {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The registry of all tiles in the game. The default tile is the air
	 * tile. */
	public static final RegistryNamespacedDefaulted<Tile> TILES =
			new RegistryNamespacedDefaulted<Tile>("TILES", "stabilise", "air", 32);
	
	
	// Template values for hardness
	/** The hardness for dirt-like tiles. */
	protected static final float HARDNESS_DIRT = 1.0f;
	/** The hardness for stone-like tiles. */
	protected static final float HARDNESS_STONE = 10.0f;
	/** The hardness for wood-like tiles. */
	protected static final float HARDNESS_WOOD = 3.0f;
	/** The hardness for invulnerable tiles. */
	protected static final float HARDNESS_INVULNERABLE = 1000000;
	
	/** Flag which is set to true when tiles are registered. */
	private static boolean registered = false;
	
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
	protected float hardness = HARDNESS_STONE;
	/** The tile's frictive force, from 0 to 1. */
	protected float friction = 0.15f;			// arbitrary default friction value
	
	
	/**
	 * Creates a tile.
	 */
	protected Tile() {
		
	}
	
	/**
	 * Updates the tile.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void update(BaseWorld world, int x, int y) {
		// nothing to see here in the default implementation
	}
	
	/**
	 * Handles being placed in the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void handlePlace(BaseWorld world, int x, int y) {
		// TODO
	}
	
	/**
	 * Handles being removed from the world.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void handleRemove(BaseWorld world, int x, int y) {
		// TODO
	}
	
	/**
	 * Handles being broken. {@link #handleRemove(BaseWorld, int, int)} is invoked
	 * in addition to any functionality here.
	 * 
	 * @param world The world.
	 * @param x The x-coordinate of the tile, in tile-lengths.
	 * @param y The y-coordinate of the tile, in tile-lengths.
	 */
	public void handleBreak(BaseWorld world, int x, int y) {
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
	public void handleStep(BaseWorld world, int x, int y, Entity e) {
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
	public void handleOverlap(BaseWorld world, int x, int y, Entity e) {
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
	public void handleTouch(BaseWorld world, int x, int y, EntityMob mob) {
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
	public void handleInteract(BaseWorld world, int x, int y, EntityMob mob) {
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
	 * Gets the tile's ID.
	 * 
	 * @return The tile's ID.
	 */
	public int getID() {
		return id;
	}
	
	/** 
	 * Gets the tile's name.
	 * 
	 * @return The tile's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Checks for whether or not the tile is solid.
	 * 
	 * @return Whether or not the tile is solid.
	 */
	public boolean isSolid() {
		return solid;
	}
	
	/**
	 * Gets the tile's frictive value.
	 * 
	 * @return The tile's frictive value.
	 */
	public float getFriction() {
		return friction;
	}
	
	/**
	 * Gets the tile's hardness.
	 * 
	 * @return The tile's hardness.
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
	 * Gets the Tile with the specified ID.
	 * 
	 * @param id The requested tile's ID.
	 * 
	 * @return The requested Tile object, or {@link Tiles#AIR} if no such tile
	 * exists.
	 */
	public static Tile getTile(int id) {
		return TILES.get(id);
	}
	
	/**
	 * Gets the Tile with the specified name.
	 * 
	 * @param name The requested tile's name.
	 * 
	 * @return The requested Tile object, or {@link Tiles#AIR} if no such tile
	 * exists.
	 */
	public static Tile getTile(String name) {
		return TILES.get(name);
	}
	
	/**
	 * Registers all tiles, and then loads the {@link Tiles} class into memory.
	 * 
	 * @throws IllegalStateException if this method has already been invoked.
	 */
	public static void registerTiles() {
		if(registered)
			throw new IllegalStateException("Tiles have already been registered!");
		
		registerTile(0, "air", new TileAir());
		registerTile(1, "stone", (new Tile()).setHardness(HARDNESS_STONE));
		registerTile(2, "dirt", (new Tile()).setHardness(HARDNESS_DIRT));
		registerTile(3, "grass", (new TileGrass()).setHardness(HARDNESS_DIRT));
		registerTile(4, "wood", (new Tile()).setHardness(HARDNESS_WOOD));
		registerTile(5, "leaves", (new Tile()).setHardness(HARDNESS_DIRT));
		registerTile(6, "planks", (new Tile()).setHardness(HARDNESS_WOOD));
		registerTile(7, "water", (new TileFluid()).setViscosity(0.12f));
		registerTile(8, "lava", (new TileFluid()).setViscosity(0.8f));
		registerTile(9, "bedrock", (new Tile()).setHardness(HARDNESS_INVULNERABLE));
		registerTile(10, "invisibleBedrock", (new Tile()).setHardness(HARDNESS_INVULNERABLE));
		registerTile(11, "ice", (new Tile()).setHardness(HARDNESS_DIRT).setFriction(0.008f));
		registerTile(12, "stoneBrick", (new Tile()).setHardness(HARDNESS_STONE));
		registerTile(13, "oreIron", (new TileOre()).setHardness(HARDNESS_STONE));
		registerTile(14, "oreCopper", (new TileOre()).setHardness(HARDNESS_STONE));
		registerTile(15, "oreGold", (new TileOre()).setHardness(HARDNESS_STONE));
		registerTile(16, "oreSilver", (new TileOre()).setHardness(HARDNESS_STONE));
		registerTile(17, "oreDiamond", (new TileOre()).setHardness(HARDNESS_STONE));
		registerTile(18, "chest", (new TileChest()).setHardness(HARDNESS_WOOD));
		registerTile(19, "mobSpawner", new TileMobSpawner());
		
		TILES.lock();
		registered = true;
		
		Tiles.poke();
	}
	
	/**
	 * Registers a tile. This should only be called during initialisation of
	 * the game.
	 * 
	 * @param id The ID with which to register the tile.
	 * @param name The name of the tile.
	 * @param tile The tile.
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
		return registered;
	}

}