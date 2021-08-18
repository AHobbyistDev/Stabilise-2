package com.stabilise.item;

import java.util.ArrayList;
import java.util.Comparator;


/**
 * An UnboundedContainer is a Container which may contain a large number of
 * item stacks. Note that the memory footprint of such a container grows in
 * proportion to its largest occupied slot. As a general rule, it's preferable
 * to use {@link #addStack(ItemStack)} over {@link #addStack(ItemStack, int)}
 * when adding item stacks to restrict unbounded heap growth and wasted memory.
 */
public class UnboundedContainer extends Container {
    
    /** The container's items. */
    private final ArrayList<ItemStack> items = new ArrayList<>();
    
    
    /**
     * Creates a new UnboundedContainer.
     */
    public UnboundedContainer() {
        // nothing to see here, move along
    }
    
    @Override
    public int size() {
        return items.size();
    }
    
    @Override
    public ItemStack getStack(int slot) {
        if(slot >= items.size())
            return ItemStack.NO_STACK;
        return items.get(slot);
    }
    
    @Override
    public ItemStack removeStack(int slot) {
        if(slot >= items.size())
            return ItemStack.NO_STACK;
        ItemStack stack = items.remove(slot);
        // If this was the last slot, remove any null entries between it and
        // the next occupied slot.
        while(items.size() > 0 && items.get(items.size() - 1) == ItemStack.NO_STACK)
            items.remove(items.size() - 1);
        return stack;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p><b>Note</b>: this operation always succeeds and hence always returns
     * {@code 0} for an UnboundedContainer.
     * 
     * @throws NullPointerException if {@code item} is {@code null}.
     * @throws IllegalArgumentException if {@code quantity <= 0}.
     */
    @Override
    public int addItem(Item item, int quantity) {
        // Add it in the normal way...
        quantity = super.addItem(item, quantity);
        
        // Create new slots if more items are yet to be added
        if(quantity > 0) {
            while(quantity > item.getMaxStackSize()) {
                items.add(item.stackOf(item.getMaxStackSize()));
                quantity -= item.getMaxStackSize();
            }
            items.add(item.stackOf(quantity));
        }
        return 0;
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p><b>Note</b>: this operation always succeeds and hence always returns
     * {@code true} for an UnboundedContainer.
     */
    @Override
    public boolean addStack(ItemStack stack) {
        // Basically the same as a standard Container, but add a new slot
        // if there are no other available slots.
        if(!super.addStack(stack))
            items.add(stack);
        return true;
    }
    
    @Override
    protected void setSlot(int slot, ItemStack stack) {
        getAndSetSlot(slot, stack);
    }
    
    @Override
    protected ItemStack getAndSetSlot(int slot, ItemStack stack) {
        // add intermediates
        items.ensureCapacity(slot + 1);
        while(items.size() <= slot)
            items.add(ItemStack.NO_STACK);
        return items.set(slot, stack);
    }
    
    @Override
    public void clear() {
        items.clear();
    }
    
    @Override
    public void sort(Comparator<ItemStack> comparator) {
        items.sort(comparator);
    }
    
}
