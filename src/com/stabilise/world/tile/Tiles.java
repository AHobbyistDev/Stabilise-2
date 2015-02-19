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
	
	public static final Tile AIR = Tile.getTile("air");
	public static final Tile VOID = Tile.getTile("void");
	public static final Tile BEDROCK = Tile.getTile("bedrock");
	public static final Tile BEDROCK_INVISIBLE = Tile.getTile("invisibleBedrock");
	public static final Tile STONE = Tile.getTile("stone");
	public static final Tile DIRT = Tile.getTile("dirt");
	public static final Tile GRASS = Tile.getTile("grass");
	public static final Tile WOOD = Tile.getTile("wood");
	public static final Tile LEAVES = Tile.getTile("leaves");
	public static final Tile PLANKS = Tile.getTile("planks");
	public static final Tile WATER = Tile.getTile("water");
	public static final Tile LAVA = Tile.getTile("lava");
	public static final Tile ICE = Tile.getTile("ice");
	public static final Tile BRICK_STONE = Tile.getTile("stoneBrick");
	public static final Tile ORE_IRON = Tile.getTile("oreIron");
	public static final Tile ORE_COPPER = Tile.getTile("oreCopper");
	public static final Tile ORE_GOLD = Tile.getTile("oreGold");
	public static final Tile ORE_SILVER = Tile.getTile("oreSilver");
	public static final Tile ORE_DIAMOND = Tile.getTile("oreDiamond");
	public static final TileChest CHEST = (TileChest)Tile.getTile("chest");
	public static final Tile MOB_SPAWNER = Tile.getTile("mobSpawner");
	
}
