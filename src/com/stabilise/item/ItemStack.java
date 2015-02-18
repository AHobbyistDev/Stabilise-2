package com.stabilise.item;

import com.stabilise.util.nbt.NBTTagCompound;

/**
 * This class represents a stack of identical items.
 * 
 * <p>To instantiate an {@code ItemStack}, refer to {@link Item#stackOf()} or
 * {@link Item#stackOf(int)}.
 */
public class ItemStack {
	
	//--------------------==========--------------------
	//-----=====Static Constants and Variables=====-----
	//--------------------==========--------------------
	
	/** An ItemStack which should be used as a placeholder to indicate the lack
	 * of an ItemStack, in preference to using a null pointer.
	 * <p>The quantity of 0 is arbitrary. */
	public static final ItemStack NO_STACK = new NoStack(Item.NO_ITEM, 0);
	
	//--------------------==========--------------------
	//-------------=====Member Variables=====-----------
	//--------------------==========--------------------
	
	/** The stack's underlying item. */
	private final Item item;
	/** The number of items in the stack. */
	private int quantity;
	
	
	/**
	 * Creates a new ItemStack.
	 * 
	 * <p>Note that quantities above the item's max stack size are technically
	 * permitted, as are negative quantities.
	 * 
	 * <p>This constructor should be used exclusively by {@link
	 * Item#stackOf(int)}.
	 * 
	 * @param item The stack's underlying item.
	 * @param quantity The number of items in the stack.
	 */
	ItemStack(Item item, int quantity) {
		this.item = item;
		this.quantity = quantity;
	}
	
	/**
	 * Checks for whether or not this stack holds the specified underlying
	 * item.
	 * 
	 * @param item The item.
	 * 
	 * @return {@code true} if this stack holds the specified item; {@code
	 * false} otherwise.
	 */
	public boolean holds(Item item) {
		return this.item.equals(item);
	}
	
	/**
	 * Checks for whether or not this stack accepts items from the specified
	 * stack. More strictly, this will return {@code true} iff both stacks
	 * contain the same underlying item and this stack is smaller than the
	 * item's {@link Item#getMaxStackSize() max stack size}.
	 * 
	 * <p>This method will return {@code true} if this stack is {@link
	 * #NO_STACK}.
	 * 
	 * @param stack The other stack.
	 * 
	 * @return {@code true} if this stack can accept items from the specified
	 * stack; {@code false} otherwise.
	 * @throws NullPointerException if {@code stack} is {@code null}.
	 */
	public boolean accepts(ItemStack stack) {
		return holds(stack.item) && quantity < item.getMaxStackSize();
	}
	
	/**
	 * Adds a specified item stack to this stack, if it contains the same
	 * underlying item (i.e. <tt>{@link #accepts(ItemStack) accepts(stack)} ==
	 * true</tt>). This is accomplished simply by moving a quantity of the
	 * items in the given stack to this stack. A stack which is added in its
	 * entirety is considered empty and should usually be disposed.
	 * 
	 * <p>This method will return {@code false} if this stack is {@link
	 * #NO_STACK}.
	 * 
	 * @param stack The stack.
	 * 
	 * @return {@code true} if the stack was added in its entirety (i.e. {@code
	 * stack.quantity == 0}); {@code false} otherwise.
	 * @throws NullPointerException if {@code stack} is {@code null}.
	 * @throws IllegalArgumentException if {@code stack.getQuantity() < 0}.
	 */
	public boolean add(ItemStack stack) {
		if(!accepts(stack))
			return false;
		stack.quantity = addQuantity(stack.quantity);
		return stack.quantity == 0;
	}
	
	/**
	 * Gets the stack's underlying item.
	 * 
	 * @return The item.
	 */
	public Item getItem() {
		return item;
	}
	
	/**
	 * @return The number of items in this stack.
	 */
	public int getQuantity() {
		return quantity;
	}
	
	/**
	 * Sets the number of items in the stack.
	 * 
	 * <p>Note that quantities above the item's max stack size are technically
	 * permitted.
	 * 
	 * @param quantity The quantity.
	 * 
	 * @throws IllegalArgumentException if {@code quantity < 0}.
	 */
	public void setQuantity(int quantity) {
		if(quantity < 0)
			throw new IllegalArgumentException("quantity < 0: " + quantity);
		this.quantity = quantity;
	}
	
	/**
	 * Increases the quantity of items in this stack by the specified amount,
	 * up to the max stack size.
	 * 
	 * @param amount The number of items to add.
	 * 
	 * @return The number of items which were not added.
	 * @throws IllegalArgumentException if {@code amount < 0}.
	 */
	public int addQuantity(int amount) {
		if(amount < 0)
			throw new IllegalArgumentException("amount < 0: " + amount);
		quantity += amount;
		if(quantity > item.getMaxStackSize()) {
			amount = quantity - item.getMaxStackSize();
			quantity = item.getMaxStackSize();
			return amount;
		}
		return 0;
	}
	
	/**
	 * Reduces the quantity of items in this stack by the specified amount,
	 * down to 0. If this stack's size is reduced to 0, it should usually be
	 * disposed.
	 * 
	 * @param amount The number of items to remove.
	 * 
	 * @return The number of items which were not removed.
	 * @throws IllegalArgumentException if {@code amount < 0}.
	 */
	public int removeQuantity(int amount) {
		if(amount < 0)
			throw new IllegalArgumentException("amount < 0: " + amount);
		if(quantity >= amount) {
			quantity -= amount;
			return 0;
		} else {
			amount -= quantity;
			quantity = 0;
			return amount;
		}
	}
	
	@Override
	public String toString() {
		return "Stack[\"" + item.getName() + "\", " + quantity + "]";
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Writes an ItemStack to an NBT compound tag.
	 * 
	 * @param stack The ItemStack.
	 * 
	 * @return The stack in the form of an NBT compound tag.
	 * @throws NullPointerException if {@code stack} is {@code null}.
	 */
	public static NBTTagCompound toNBT(ItemStack stack) {
		NBTTagCompound tag = Item.toNBT(stack.item);
		tag.addInt("count", stack.quantity);
		return tag;
	}
	
	/**
	 * Reads an ItemStack from an NBT compound tag.
	 * 
	 * @param tag The tag.
	 * 
	 * @return The stack, or {@link #NO_STACK} if the tag represents an invalid
	 * item stack.
	 * @throws NullPointerException if {@code tag} is {@code null}.
	 */
	public static ItemStack fromNBT(NBTTagCompound tag) {
		Item item = Item.fromNBT(tag);
		if(item == Item.NO_ITEM)
			return NO_STACK;
		int quantity = tag.getInt("count");
		return item.stackOf(quantity);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Private class for the {@link ItemStack#NO_STACK NO_STACK} object to
	 * return specialised values.
	 */
	private static class NoStack extends ItemStack {
		
		private NoStack(Item item, int quantity) {
			super(item, quantity);
		}
		
		/**
		 * Returns {@code true} as an empty slot may accept a stack.
		 */
		@Override
		public boolean accepts(ItemStack stack) {
			return true;
		}
		
		/**
		 * Returns {@code false} as no stack may be added to this stack.
		 */
		@Override
		public boolean add(ItemStack stack) {
			return false;
		}
		
	}
	
}
