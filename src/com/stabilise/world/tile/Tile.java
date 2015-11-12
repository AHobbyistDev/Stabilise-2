package com.stabilise.world.tile;

import static com.stabilise.world.tile.TileBuilder.Template.*;

import com.stabilise.entity.Entity;
import com.stabilise.entity.EntityItem;
import com.stabilise.entity.EntityMob;
import com.stabilise.item.ItemStack;
import com.stabilise.item.Items;
import com.stabilise.util.collect.RegistryNamespacedDefaulted;
import com.stabilise.world.World;
import com.stabilise.world.tile.TileBuilder.Template;

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
    
    private static TileBuilder builder;
    
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
    private final int id;
    /** The tile's name. */
    private final String name;
    
    /** Whether or not the tile is solid. */
    protected final boolean solid;
    /** The tile's hardness. */
    protected final float hardness;
    /** The tile's frictive force, from 0 to 1. */
    protected final float friction;
    
    protected final byte light;
    protected final byte falloff;
    
    
    /**
     * Creates a tile.
     */
    protected Tile(TileBuilder b) {
        id = b.id;
        name = b.name;
        solid = b.solid;
        hardness = b.hardness;
        friction = b.friction;
        light = b.light;
        falloff = b.falloff;
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
        
        if(isSolid())
            createItemEntity(world, x, y, createStack(1));
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
     * Creates an item entity on this tile. Does nothing if {@code stack} is
     * {@link ItemStack#NO_STACK}.
     */
    protected void createItemEntity(World world, int x, int y, ItemStack stack) {
        if(stack != ItemStack.NO_STACK) {
            EntityItem e = new EntityItem(createStack(1));
            e.pop(world.getRnd());
            world.addEntity(e, x + 0.5, y + 0.1);
        }
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
    
    public byte getLight() {
        return light;
    }
    
    public byte getFalloff() {
        return falloff;
    }
    
    /**
     * Creates an ItemStack encapsulating this tile.
     * 
     * @param quantity The quantity of tiles in the stack.
     */
    public ItemStack createStack(int quantity) {
        return Items.TILE.stackOf(this, quantity);
    }
    
    @Override
    public String toString() {
        return name + " [" + id + "]";
    }
    
    @Override
    public boolean equals(Object o) {
        return o == this;
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
        builder = new TileBuilder();
        
        register(AIR,   0, "air");
        register(AIR,   1, "void");
        register(INVUL, 2, "bedrock");
        register(INVUL, 3, "invisibleBedrock");
        register(STONE, 4, "stone");
        register(DIRT,  5, "dirt");
        register(GRASS, 6, "grass");
        register(WOOD,  7, "wood");
        register(DIRT,  8, "leaves");
        register(WOOD,  9, "planks");
        register(WATER, 10, "water");
        register(LAVA,  11, "lava").light((byte)15);
        register(ICE,   12, "ice");
        register(STONE, 13, "stoneBrick");
        register(ORE,   14, "oreIron");
        register(ORE,   15, "oreCopper");
        register(ORE,   16, "oreGold");
        register(ORE,   17, "oreSilver");
        register(ORE,   18, "oreDiamond").hardness(50f);
        register(CHEST, 19, "chest");
        register(SPWNR, 20, "mobSpawner");
        register(STONE, 21, "glass").light((byte)15);
        
        builder.end();
        builder = null;
        
        TILES.lock();
    }
    
    /**
     * Returns a builder with which to begin building a tile with the specified
     * template, ID and name.
     */
    private static TileBuilder register(Template t, int id, String name) {
        return builder.begin(t, id, name);
    }
    
    /**
     * Registers a tile. This should only be called during initialisation of
     * the game.
     * 
     * @param id The ID with which to register the tile.
     * 
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     * @throws NullPointerException if the tile or its name is {@code null}.
     * @throws IllegalStateException if no more tiles may be registered.
     */
    static void registerTile(Tile t) {
        TILES.register(t.id, t.name, t);
    }
    
    /**
     * @return {@code true} if the tiles have been registered; {@code false}
     * otherwise.
     */
    static boolean isRegistered() {
        return TILES.isLocked();
    }
    
}