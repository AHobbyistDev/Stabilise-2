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
    
    @SuppressWarnings("unchecked")
    private static <T extends Tile> T get(String name) { return (T)Tile.getTile(name); }
    
    public static final Tile
            AIR = get("air"),
            VOID = get("void"),
            BEDROCK = get("bedrock"),
            BEDROCK_INVISIBLE = get("invisibleBedrock"),
            STONE = get("stone"),
            DIRT = get("dirt"),
            GRASS = get("grass"),
            WOOD = get("wood"),
            LEAVES = get("leaves"),
            PLANKS = get("planks"),
            WATER = get("water"),
            LAVA = get("lava"),
            ICE = get("ice"),
            BRICK_STONE = get("stoneBrick"),
            ORE_IRON = get("oreIron"),
            ORE_COPPER = get("oreCopper"),
            ORE_GOLD = get("oreGold"),
            ORE_SILVER = get("oreSilver"),
            ORE_DIAMOND = get("oreDiamond"),
            MOB_SPAWNER = get("mobSpawner");
    public static final TileChest
            CHEST = get("chest");
    
    
}
