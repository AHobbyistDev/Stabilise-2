package com.stabilise.item;

import com.stabilise.core.Constants;
import com.stabilise.util.collect.registry.RegistryNamespaced;
import com.stabilise.util.collect.registry.RegistryParams;
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
    public static final RegistryNamespaced<Item> ITEMS = 
            new RegistryNamespaced<>(new RegistryParams("ItemRegistry", 8), "stabilise");
    
    /** The default maximum stack size. */
    public static final int DEFAULT_MAX_STACK_SIZE = Constants.MAX_STACK_SIZE;
    
    /** An item which should be used as a placeholder to indicate the lack of
     * an item, in preference to using a null pointer.
     * <p>The name and max stack size are arbitrary, and the ID is 0, the
     * 'default' ID. */
    public static final Item NO_ITEM = new Item(0, "", 1) {
        @Override
        public ItemStack stackOf(int quantity) {
            return ItemStack.NO_STACK;
        }
    };
    
    /** Flag which is set to true when items are registered. */
    private static boolean registered = false;
    
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
     * @throws IllegalArgumentException if {@code maxStackSize < 1}.
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
     * @throws NullPointerException  if {@code name} is {@code null}.
     * @throws IllegalArgumentException if {@code maxStackSize < 1}.
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
    
    /**
     * Creates a new ItemStack with a quantity of 1 encapsulating this Item.
     */
    public ItemStack stackOf() {
        return stackOf(1);
    }
    
    /**
     * Creates a new ItemStack encapsulating this Item.
     * 
     * <p>Note that quantities above the item's max stack size are technically
     * permitted, as are negative quantities. To constrain the stack size to
     * this item's max stack size, use {@link #stackOfConstrained(int)}.
     * 
     * @param quantity The number of items in the stack.
     */
    public ItemStack stackOf(int quantity) {
        return new ItemStack(this, quantity);
    }
    
    /**
     * Creates a new ItemStack encapsulating this item, as per {@link
     * #stackOf(int)}.
     * 
     * @param quantity Stack quantity.
     * @param data Item data.
     */
    ItemStack stackOf(int quantity, int data) {
        return new ItemStack(this, quantity, data);
    }
    
    /**
     * Creates a new ItemStack encapsulating this Item. The provided quantity
     * is clamped to this Item's {@link #getMaxStackSize() max stack size}.
     * 
     * <p>Note that negative quantities are technically permitted.
     * 
     * @param quantity The number of items in the stack.
     */
    public ItemStack stackOfConstrained(int quantity) {
        return stackOf(quantity >= getMaxStackSize() ? getMaxStackSize() : quantity);
    }
    
    /**
     * Writes this Item to an NBT compound tag. The returned compound tag
     * possesses an integer tag "id" corresponding to the item's ID, and can be
     * used to reobtain this Item via {@link #fromNBT(NBTTagCompound)}.
     */
    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.addInt("id", id);
        return tag;
    }
    
    @Override
    public int hashCode() {
        //throw new AssertionError("hashCode not designed");
        return id;
    }
    
    @Override
    public boolean equals(Object o) {
        return o == this;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    //--------------------==========--------------------
    //------------=====Static Functions=====------------
    //--------------------==========--------------------
    
    /**
     * Gets the Item with the specified ID.
     * 
     * @return The item with the specified ID, or {@link #NO_ITEM} if no such
     * item exists.
     * @throws IndexOutOfBoundsException if {@code id < 0}.
     */
    public static Item getItem(int id) {
        Item item = ITEMS.get(id);
        return item == null ? NO_ITEM : item;
    }
    
    /**
     * Gets the Item with the specified name.
     * 
     * @return The item with the specified name, or {@link #NO_ITEM} if no such
     * item exists.
     */
    public static Item getItem(String name) {
        Item item = ITEMS.get(name);
        return item == null ? NO_ITEM : item;
    }
    
    /**
     * Reads an Item from an NBT compound tag. Such a tag should possess an
     * integer tag "id" corresponding to the item's ID.
     * 
     * @param tag The tag.
     * 
     * @return The item, or {@link #NO_ITEM} if the tag represents an invalid
     * item.
     * @throws NullPointerException if {@code tag} is {@code null}.
     */
    public static Item fromNBT(NBTTagCompound tag) {
        return getItem(tag.getInt("id"));
    }
    
    /**
     * Registers all items, and then loads the {@link Items} class into memory.
     * 
     * <p>This should be called after {@link Tile#registerTiles()} is invoked.
     * 
     * @throws IllegalStateException if this method has already been invoked.
     */
    public static void registerItems() {
        if(registered)
            throw new IllegalStateException("Items have already been registered!");
        
        registerItem(0, "",         NO_ITEM);
        registerItem(1, "tile",     new ItemTile());
        registerItem(2, "sword",    new Item());
        registerItem(3, "apple",    new Item());
        registerItem(4, "arrow",    new Item());
        
        // Create an item for every tile
        //for(Tile tile : Tile.TILES)
        //    registerItem(tile.getID(), tile.getName(), new ItemTile(tile));
        
        ITEMS.lock();
        registered = true;
        
        Items.poke();
    }
    
    /**
     * Registers an item. This should only be called by registerItems().
     * 
     * @param id The ID with which to register the item.
     * @param name The name of the item.
     * @param item The item.
     */
    private static void registerItem(int id, String name, Item item) {
        ITEMS.register(id, name, item);
        item.id = id;
        item.name = name;
    }
    
    /**
     * @return {@code true} if the items have been registered; {@code false}
     * otherwise.
     */
    static boolean isRegistered() {
        return registered;
    }
    
}
