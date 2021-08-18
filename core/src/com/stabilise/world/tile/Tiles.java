package com.stabilise.world.tile;

/**
 * This class contains explicit references to all the tiles in the game.
 * 
 * <p>Note that this class should only be loaded into memory <i>after</i>
 * {@link Tile#TILES} has been populated by invocation of
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
                    Tiles.class + " loaded into memory before Tile.registerItems()"
                    + " was invoked!"
            );
    }
    
    @SuppressWarnings("unchecked")
    private static <T extends Tile> T get(String name) { return (T)Tile.getTile(name); }
    
    public static final Tile
            air           = get("air"),
            darkness      = get("void"),
            bedrock       = get("bedrock"),
            barrier       = get("invisibleBedrock"),
            stone         = get("stone"),
            dirt          = get("dirt"),
            grass         = get("grass"),
            wood          = get("wood"),
            leaves        = get("leaves"),
            planks        = get("planks"),
            water         = get("water"),
            lava          = get("lava"),
            ice           = get("ice"),
            stoneBrick    = get("stoneBrick"),
            oreIron       = get("oreIron"),
            oreCopper     = get("oreCopper"),
            oreGold       = get("oreGold"),
            oreSilver     = get("oreSilver"),
            oreDiamond    = get("oreDiamond"),
            mobSpawner    = get("mobSpawner"),
            glass         = get("glass"),
            torch         = get("torch"),
            glowstone     = get("glowstone"),
            voidRock      = get("voidRock"),
            voidRockDense = get("voidRockDense"),
            voidDirt      = get("voidDirt");
    public static final TileChest
            chest         = get("chest");
    
    
}
