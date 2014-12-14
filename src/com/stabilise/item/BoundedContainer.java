package com.stabilise.item;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A BoundedContainer is a Container with a constant maximum capacity, and is
 * backed by a typical array.
 */
public class BoundedContainer extends Container {
	
	/** The container's capacity. */
	private final int capacity;
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
		this.capacity = capacity;
		items = new ItemStack[capacity];
		Arrays.fill(items, ItemStack.NO_STACK);
	}
	
	@Override
	public int size() {
		return capacity;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws ArrayIndexOutOfBoundsException Thrown if {@code slot < 0} or
	 * {@code slot >= capacity}.
	 */
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
