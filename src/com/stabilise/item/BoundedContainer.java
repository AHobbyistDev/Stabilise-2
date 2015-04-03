package com.stabilise.item;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A BoundedContainer is a Container with a constant maximum capacity, and is
 * backed by a typical array.
 */
public class BoundedContainer extends Container {
	
	/** The container's items. */
	private ItemStack[] items;
	
	
	/**
	 * Creates a new BoundedContainer, filled completely with {@link
	 * ItemStack#NO_STACK}.
	 * 
	 * @param capacity The container's capacity.
	 */
	public BoundedContainer(int capacity) {
		super();
		items = new ItemStack[capacity];
		Arrays.fill(items, ItemStack.NO_STACK);
	}
	
	@Override
	public int size() {
		return items.length;
	}
	
	@Override
	public ItemStack getStack(int slot) {
		return items[slot];
	}
	
	@Override
	protected void setSlot(int slot, ItemStack stack) {
		items[slot] = stack;
	}
	
	@Override
	protected ItemStack getAndSetSlot(int slot, ItemStack stack) {
		ItemStack old = items[slot];
		items[slot] = stack;
		return old;
	}
	
	@Override
	public void sort(Comparator<ItemStack> comparator) {
		Arrays.sort(items, comparator);
	}
	
}
