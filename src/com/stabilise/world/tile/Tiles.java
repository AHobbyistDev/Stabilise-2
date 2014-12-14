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
	
	public static final Tile AIR = Tile.TILES.getObject("air");
	public static final Tile STONE = Tile.TILES.getObject("stone");
	public static final Tile DIRT = Tile.TILES.getObject("dirt");
	public static final Tile GRASS = Tile.TILES.getObject("grass");
	public static final Tile WOOD = Tile.TILES.getObject("wood");
	public static final Tile LEAVES = Tile.TILES.getObject("leaves");
	public static final Tile PLANKS = Tile.TILES.getObject("planks");
	public static final Tile WATER = Tile.TILES.getObject("water");
	public static final Tile LAVA = Tile.TILES.getObject("lava");
	public static final Tile BEDROCK = Tile.TILES.getObject("bedrock");
	public static final Tile BEDROCK_INVISIBLE = Tile.TILES.getObject("invisibleBedrock");
	public static final Tile ICE = Tile.TILES.getObject("ice");
	public static final Tile BRICK_STONE = Tile.TILES.getObject("stoneBrick");
	public static final Tile ORE_IRON = Tile.TILES.getObject("oreIron");
	public static final Tile ORE_COPPER = Tile.TILES.getObject("oreCopper");
	public static final Tile ORE_GOLD = Tile.TILES.getObject("oreGold");
	public static final Tile ORE_SILVER = Tile.TILES.getObject("oreSilver");
	public static final Tile ORE_DIAMOND = Tile.TILES.getObject("oreDiamond");
	public static final TileChest CHEST = (TileChest)Tile.TILES.getObject("chest");
	public static final Tile MOB_SPAWNER = Tile.TILES.getObject("mobSpawner");
	
}
