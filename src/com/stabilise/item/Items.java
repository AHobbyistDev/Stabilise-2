package com.stabilise.item;

/**
 * This class contains explicit references to all the items in the game.
 * 
 * <p>Note that this class should only be loaded into memory <i>after</i>
 * {@link Item.ITEMS} has been populated by invocation of
 * {@link Item#registerItems()}.
 */
public class Items {
	
	// non-instantiable
	public Items() {}
	
	public static final Item TILE = Item.ITEMS.getObject("item");
	public static final Item SWORD = Item.ITEMS.getObject("sword");
	public static final Item APPLE = Item.ITEMS.getObject("apple");
	public static final Item ARROW = Item.ITEMS.getObject("arrow");
	
}
