package com.stabilise.item;

import com.stabilise.util.RegistryNamespaced;
import com.stabilise.util.nbt.NBTTagCompound;
import com.stabilise.world.tile.Tile;

/**
 * Items are essentially ownable game objects.
 */
public class Item {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** The registry containing all items in the game. */
	public static final RegistryNamespaced<Item> ITEMS = new RegistryNamespaced<Item>("ITEMS", 8, "stabilise");
	
	/** The default maximum stack size. */
	public static final int DEFAULT_MAX_STACK_SIZE = 64;
	
	/** An item which should be used as a placeholder to indicate the lack of
	 * an item, in preference to using a null pointer.
	 * <p>The name and max stack size are arbitrary, and the ID is -1, the
	 * quintessential "invalid index" number (though any ID should
	 * theoretically be acceptable as things currently stand). */
	public static final Item NO_ITEM = new Item(-1, "null", 1);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The item's ID. This should be unique, and >= 0. */
	private int id;
	/** The item's name. */
	private String name;
	/** The maximum stack size. */
	private final int maxStackSize;
	
	
	/**
	 * Creates a new Item with the default maximum stack size, specified by
	 * {@link #DEFAULT_MAX_STACK_SIZE}.
	 */
	protected Item() {
		this(DEFAULT_MAX_STACK_SIZE);
	}
	
	/**
	 * Creates a new Item.
	 * 
	 * @param maxStackSize The item's maximum stack size.
	 * 
	 * @throws IllegalArgumentException Thrown if {@code maxStackSize < 1}.
	 */
	protected Item(int maxStackSize) {
		if(maxStackSize < 1)
			throw new IllegalArgumentException("maxStackSize should be positive!");
		
		this.maxStackSize = maxStackSize;
	}
	
	/**
	 * Creates a new Item.
	 * 
	 * @param id The item's id.
	 * @param name The items name.
	 * @param maxStackSize The item's maximum stack size.
	 * 
	 * @throws NullPointerException Thrown if {@code name} is {@code null}.
	 * @throws IllegalArgumentException Thrown if {@code maxStackSize < 1}.
	 */
	protected Item(int id, String name, int maxStackSize) {
		if(name == null)
			throw new NullPointerException("name is null");
		if(maxStackSize < 1)
			throw new IllegalArgumentException("maxStackSize should be positive!");
		
		this.id = id;
		this.name = name;
		this.maxStackSize = maxStackSize;
	}
	
	/**
	 * Gets the item's ID.
	 * 
	 * @return The item's ID.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Gets the item's name.
	 * 
	 * @return The item's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the item's maximum stack size.
	 * 
	 * @return The item's maximum stack size.
	 */
	public int getMaxStackSize() {
		return maxStackSize;
	}
	
	// Note: Overriding equals() isn't necessary since the equality operator
	// is sufficient.
	
	@Override
	public String toString() {
		return name;
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets an Item object with the given ID.
	 * 
	 * @param id The requested item's ID.
	 * 
	 * @return The item with the given ID, or {@link #NO_ITEM} if no such item
	 * exists.
	 */
	public static Item getItem(int id) {
		Item item = ITEMS.getObject(id);
		return item == null ? NO_ITEM : item;
	}
	
	/**
	 * Writes an Item to an NBT compound tag.
	 * 
	 * @return The item in the form of an NBT compound tag.
	 */
	public static NBTTagCompound toNBT(Item item) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.addInt("id", item.id);
		return tag;
	}
	
	/**
	 * Reads an Item from an NBT compound tag.
	 * 
	 * @param tag The tag.
	 * 
	 * @return The item, or {@link #NO_ITEM} if the tag represents an invalid
	 * item.
	 * @throws NullPointerException Thrown if {@code tag} is {@code null}.
	 */
	public static Item fromNBT(NBTTagCompound tag) {
		return getItem(tag.getInt("id"));
	}
	
	/**
	 * Registers all items.
	 * 
	 * <p>This should be called after {@link Tile#registerTiles()} is invoked,
	 * as respective items are created for every tile.
	 */
	public static void registerItems() {
		registerItem(0, "tile", new Item());
		registerItem(1, "sword", new Item());
		registerItem(2, "apple", new Item());
		registerItem(3, "arrow", new Item());
		
		// Create an item for every tile
		//for(Tile tile : Tile.TILES)
		//	registerItem(tile.getID(), tile.getName(), new ItemTile(tile));
	}
	
	/**
	 * Registers an item. This should only be called by registerItems().
	 * 
	 * @param id The ID with which to register the item.
	 * @param name The name of the item.
	 * @param item The item.
	 */
	private static void registerItem(int id, String name, Item item) {
		ITEMS.registerObject(id, name, item);
		item.id = id;
		item.name = name;
	}
	
}
