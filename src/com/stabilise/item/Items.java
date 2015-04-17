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
	
	/** Invoking this loads this class into memory but otherwise does nothing. */
	public static void poke() {}
	
	// Invoked when this class is loaded into memory
	static {
		// ...funnily enough, invoking this in fact loads Item into memory.
		if(!Item.isRegistered())
			throw new IllegalStateException(
					Items.class.toString() + " loaded into memory before"
							+ "Item.registerItems() was invoked!"
			);
	}
	
	public static final Item NO_ITEM = Item.NO_ITEM;
	public static final Item TILE = Item.ITEMS.get("tile");
	public static final Item SWORD = Item.ITEMS.get("sword");
	public static final Item APPLE = Item.ITEMS.get("apple");
	public static final Item ARROW = Item.ITEMS.get("arrow");
	
}
