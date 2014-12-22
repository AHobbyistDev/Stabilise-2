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
	
	/** Invoking this loads this class into memory. */
	static void poke() {}
	
	// Invoked when this class is loaded into memory
	static {
		if(!Item.isRegistered())
			throw new IllegalStateException(
					Items.class.toString() + " loaded into memory before Item.registerItems()"
					+ " was invoked!"
			);
	}
	
	public static final Item TILE = Item.ITEMS.getObject("tile");
	public static final Item SWORD = Item.ITEMS.getObject("sword");
	public static final Item APPLE = Item.ITEMS.getObject("apple");
	public static final Item ARROW = Item.ITEMS.getObject("arrow");
	
}
