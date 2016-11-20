package com.stabilise.item;

import com.stabilise.world.tile.Tile;

/**
 * An item for a tile.
 */
public class ItemTile extends Item {
    
    /**
     * Creates a new tile Item.
     */
    ItemTile() {
        super();
    }
    
    /**
     * Creates a new tile Item.
     * 
     * @param maxStackSize The item's maximum stack size.
     */
    ItemTile(int maxStackSize) {
        super(maxStackSize);
    }
    
    /**
     * Does not work - use {@link #stackOf(Tile, int)} instead!
     * 
     * <p>If invoked, throws a RuntimeException.
     */
    @Override
    public ItemStack stackOf() {
        throw new RuntimeException("Use stackOf(Tile, int) for ItemTile!");
    }
    
    /**
     * Does not work - use {@link #stackOf(Tile, int)} instead!
     * 
     * <p>If invoked, throws a RuntimeException.
     */
    public ItemStack stackOf(int quantity) {
        throw new RuntimeException("Use stackOf(Tile, int) for ItemTile!");
    }
    
    /**
     * Creates a new ItemStack encapsulating a specific tile.
     * 
     * @param t The tile.
     * @param quantity The number of tiles in the stack.
     * 
     * @throws NullPointerException if {@code t} is {@code null}.
     */
    public ItemStack stackOf(Tile t, int quantity) {
        return super.stackOf(quantity, t.getID());
    }
    
}
