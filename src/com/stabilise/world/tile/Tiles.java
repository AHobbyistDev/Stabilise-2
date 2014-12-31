package com.stabilise.world.tile;


/**
 * This class contains explicit references to all the tiles in the game.
 * 
 * <p>Note that this class should only be loaded into memory <i>after</i>
 * {@link Tile.TILES} has been populated by invocation of
 * {@link Tile#registerTiles()}.
 */
public class Tiles {
	
	// non-instantiable
	private Tiles() {}
	
	/** Invoking this loads this class into memory but otherwise does nothing. */
	public static void poke() {}
	
	// Invoked when this class is loaded into memory
	static {
		if(!Tile.isRegistered())
			throw new IllegalStateException(
					Tiles.class.toString() + " loaded into memory before Tile.registerItems()"
					+ " was invoked!"
			);
	}
	
	public static final Tile AIR = Tile.TILES.get("air");
	public static final Tile STONE = Tile.TILES.get("stone");
	public static final Tile DIRT = Tile.TILES.get("dirt");
	public static final Tile GRASS = Tile.TILES.get("grass");
	public static final Tile WOOD = Tile.TILES.get("wood");
	public static final Tile LEAVES = Tile.TILES.get("leaves");
	public static final Tile PLANKS = Tile.TILES.get("planks");
	public static final Tile WATER = Tile.TILES.get("water");
	public static final Tile LAVA = Tile.TILES.get("lava");
	public static final Tile BEDROCK = Tile.TILES.get("bedrock");
	public static final Tile BEDROCK_INVISIBLE = Tile.TILES.get("invisibleBedrock");
	public static final Tile ICE = Tile.TILES.get("ice");
	public static final Tile BRICK_STONE = Tile.TILES.get("stoneBrick");
	public static final Tile ORE_IRON = Tile.TILES.get("oreIron");
	public static final Tile ORE_COPPER = Tile.TILES.get("oreCopper");
	public static final Tile ORE_GOLD = Tile.TILES.get("oreGold");
	public static final Tile ORE_SILVER = Tile.TILES.get("oreSilver");
	public static final Tile ORE_DIAMOND = Tile.TILES.get("oreDiamond");
	public static final TileChest CHEST = (TileChest)Tile.TILES.get("chest");
	public static final Tile MOB_SPAWNER = Tile.TILES.get("mobSpawner");
	
}
