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
	
}
