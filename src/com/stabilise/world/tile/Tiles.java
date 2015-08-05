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
	
	public static final Tile
			AIR = Tile.getTile("air"),
			VOID = Tile.getTile("void"),
			BEDROCK = Tile.getTile("bedrock"),
			BEDROCK_INVISIBLE = Tile.getTile("invisibleBedrock"),
			STONE = Tile.getTile("stone"),
			DIRT = Tile.getTile("dirt"),
			GRASS = Tile.getTile("grass"),
			WOOD = Tile.getTile("wood"),
			LEAVES = Tile.getTile("leaves"),
			PLANKS = Tile.getTile("planks"),
			WATER = Tile.getTile("water"),
			LAVA = Tile.getTile("lava"),
			ICE = Tile.getTile("ice"),
			BRICK_STONE = Tile.getTile("stoneBrick"),
			ORE_IRON = Tile.getTile("oreIron"),
			ORE_COPPER = Tile.getTile("oreCopper"),
			ORE_GOLD = Tile.getTile("oreGold"),
			ORE_SILVER = Tile.getTile("oreSilver"),
			ORE_DIAMOND = Tile.getTile("oreDiamond"),
			MOB_SPAWNER = Tile.getTile("mobSpawner");
	public static final TileChest CHEST = (TileChest)Tile.getTile("chest");
	
	
}
