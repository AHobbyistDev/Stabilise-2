package com.stabilise.item;

/**
 * An item for a tile.
 */
public class ItemTile extends Item {
	
	/** The item's parent tile. */
	//private final Tile tile;
	
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
	 * Creates a new tile Item with the default maximum stack size, specified
	 * by {@link Item#DEFAULT_MAX_STACK_SIZE}.
	 * 
	 * @param tile The item's parent tile.
	 */
	//ItemTile(Tile tile) {
	//	super();
	//	this.tile = tile;
	//}
	
	/**
	 * Creates a new tile Item.
	 * 
	 * @param tile The item's parent tile.
	 * @param maxStackSize The item's maximum stack size.
	 */
	//ItemTile(Tile tile, int maxStackSize) {
	//	super(maxStackSize);
	//	this.tile = tile;
	//}
	
	/**
	 * Gets the item's parent tile.
	 * 
	 * @return The tile.
	 */
	//public Tile getTile() {
	//	return tile;
	//}
	
}
