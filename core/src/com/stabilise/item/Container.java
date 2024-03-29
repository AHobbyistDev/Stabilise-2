package com.stabilise.item;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Exportable;

/**
 * A container is something which contains items - e.g. player inventory,
 * chest.
 */
public abstract class Container implements IContainer,
                                           Iterable<ItemStack>,
                                           Exportable {
    
    /**
     * @return {@code true} if this container is bounded; {@code false} if it
     * is unbounded.
     */
    protected boolean isBounded() {
        return this instanceof BoundedContainer;
    }
    
    /**
     * Gets the size of this container.
     * <ul>
     * <li>If this container is a <b>BoundedContainer</b>, the returned value
     * is its capacity. This value is such that any number {@code slot} in the
     * domain {@code 0 <= slot < size()} should never throw an {@code
     * IndexOutOfBoundsException} for any of this class' relevant methods.
     * <li>If this container is an <b>UnboundedContainer</b>, the returned
     * value is usually equivalent to the index of the largest occupied slot
     * plus one (though this may not be the case with every implementation).
     * </ul>
     * 
     * @return The size of this container.
     */
    public abstract int size();
    
    /**
     * Gets the item stack in the specified slot. Note that ownership of the
     * returned ItemStack is still considered to be held by this Container - to
     * force this Container to relinquish ownership, refer instead to {@link
     * #removeStack(int)}.
     * 
     * @return The item stack in the slot, or {@link ItemStack#NO_STACK} if the
     * slot is empty.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    public abstract ItemStack getStack(int slot);
    
    /**
     * Removes an item stack from the specified slot in the container, and
     * returns the stack. This causes this Container to relinquish ownership of
     * the returned ItemStack.
     * 
     * @param slot The slot.
     * 
     * @return The item stack in the slot, or {@link ItemStack#NO_STACK} if the
     * slot is empty.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    public ItemStack removeStack(int slot) {
        return getAndSetSlot(slot, ItemStack.NO_STACK);
    }
    
    @Override
    public boolean canAddStack(ItemStack stack) {
        if(stack == null)
            throw new NullPointerException("stack is null");
        
        if(!isBounded())
            return true;
        
        for(int i = 0; i < size(); i++) {
            if(getStack(i).accepts(stack))
                return true;
        }
        return false;
    }
    
    @Override
    public int addItem(Item item, int quantity) {
        if(quantity <= 0)
            throw new IllegalArgumentException("quantity <= 0: " + quantity);
        
        int lowestMatching = 0;
        int lowestEmpty = 0;
        boolean foundEmpty = false;
        
        // First, try to find any stacks which accept this item, and add to
        // them if possible.
        addToMatchingStacks:
        do {
            ItemStack stack = item.stackOfConstrained(quantity);
            quantity -= stack.getQuantity();
            
            for(int i = lowestMatching; i < size(); i++) {
                if(!foundEmpty) {
                    if(isSlotEmpty(i))
                        foundEmpty = true;
                    else
                        lowestEmpty++;
                }
                
                if(getStack(i).add(stack)) {
                    lowestMatching = i;
                    continue addToMatchingStacks;
                }
            }
            
            // If we reach this point, there are no (or no more) available
            // matching stacks.
            quantity += stack.getQuantity();
            break;
        } while(quantity > 0);
        
        // Next, add the item(s) to empty slots
        addToEmptySlots:
        while(quantity > 0) {
            ItemStack stack = item.stackOfConstrained(quantity);
            quantity -= stack.getQuantity();
            
            for(int i = lowestEmpty; i < size(); i++) {
                if(isSlotEmpty(i) && trySetSlot(i, stack)) {
                    lowestEmpty = i+1;
                    continue addToEmptySlots;
                }
            }
            
            // If we reach here, there simply isn't enough space to accommodate
            // the specified number of items.
            return quantity + stack.getQuantity(); // the amount that wasn't added
        }
        
        // If we reach here, all was successful!
        return 0;
    }
    
    /**
     * Unoptimised form of {@link #addItem(Item, int)}.
     */
    @SuppressWarnings("unused")
    private int addItemUnoptimised(Item item, int quantity) {
        if(quantity <= 0)
            throw new IllegalArgumentException("quantity <= 0: " + quantity);
        
        // If the desired quantity is larger than the max stack size, partition
        // into multiple ItemStacks.
        while(quantity > item.getMaxStackSize()) { // throws the NPE here
            ItemStack stack = item.stackOf(item.getMaxStackSize());
            quantity -= item.getMaxStackSize();
            if(!addStack(stack))
                return quantity + stack.getQuantity();
        }
        ItemStack stack = item.stackOf(quantity);
        addStack(stack);
        
        return stack.getQuantity();
    }
    
    /**
     * Adds a specified quantity of items to the container in the specified
     * slot.
     * 
     * @param item The template of the item(s) to add.
     * @param quantity The number of items to add.
     * @param slot The slot in which to add the items, or -1 if they may be
     * added to any slot, as per {@link #addItem(Item, int)}.
     * 
     * @return The number of items which were not added to the container, due
     * to there not existing enough space in the container.
     * @throws NullPointerException if {@code item} is {@code null}.
     * @throws IllegalArgumentException if {@code quantity <= 0}.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    public int addItem(Item item, int quantity, int slot) {
        if(quantity <= 0)
            throw new IllegalArgumentException("quantity <= 0: " + quantity);
        
        ItemStack stack = item.stackOfConstrained(quantity);
        quantity -= stack.getQuantity();
        addStack(stack, slot);
        return quantity + stack.getQuantity();
    }
    
    @Override
    public boolean addStack(ItemStack stack) {
        if(stack == null)
            throw new NullPointerException("stack is null");
        
        // First search for matching items
        for(int i = 0; i < size(); i++) {
            if(getStack(i).add(stack))
                return true;
        }
        // If no matching non-full stacks are found (or remain), place it
        // in a new slot
        for(int i = 0; i < size(); i++) {
            if(isSlotEmpty(i) && trySetSlot(i, stack))
                return true;
        }
        return false;
    }
    
    /**
     * Adds an item stack to the container.
     * 
     * <p>If this method returns {@code true}, ownership of {@code stack}
     * should be relinquished as it is now considered property of this
     * Container.
     * 
     * <p>Note that even if this method returns {@code false}, some of the
     * given stack may have been added to this container, and its {@link
     * ItemStack#getQuantity() quantity} may have hence changed.
     * 
     * @param stack The item stack to add to the container.
     * @param slot The slot in which to add the stack.
     * 
     * @return {@code true} if the stack was added in its entirety and should
     * be released; {@code false} otherwise.
     * @throws NullPointerException if {@code stack} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    public boolean addStack(ItemStack stack, int slot) {
        if(stack == null)
            throw new NullPointerException("stack is null");
        if(slot < 0)
            throw new IndexOutOfBoundsException("slot < 0: " + slot);
        
        if(isSlotEmpty(slot))
            return trySetSlot(slot, stack);
        else
            return getStack(slot).add(stack);
    }
    
    /**
     * Sets a stack in the specified slot. This always succeeds and returns
     * {@code true} in the default implementation; however, subclasses may wish
     * to override this for custom containers wherein a slot may only accept a
     * certain item.
     * 
     * <p>Note that this may overwrite the contents of the slot regardless of
     * whether it is empty.
     * 
     * @param slot The slot.
     * @param stack The stack. This should not be {@code null}; {@link
     * ItemStack#NO_STACK} should be used to indicate the lack of an item.
     * 
     * @return {@code true} if the stack was placed in the slot; {@code false}
     * otherwise.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    protected boolean trySetSlot(int slot, ItemStack stack) {
        setSlot(slot, stack);
        return true;
    }
    
    /**
     * Sets the stack in a specified slot. This overwrites the contents of the
     * slot with no restrictions.
     * 
     * @param slot The slot.
     * @param stack The stack. This should not be {@code null}; {@link
     * ItemStack#NO_STACK} should be used to indicate the lack of an item.
     * 
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    protected abstract void setSlot(int slot, ItemStack stack);
    
    /**
     * Sets the stack in a specified slot, and returns the former contents of
     * the slot.
     * 
     * @param slot The slot.
     * @param stack The stack. This should not be {@code null}; {@link
     * ItemStack#NO_STACK} should be used to indicate the lack of an item.
     * 
     * @return The former contents of the slot, or {@link ItemStack#NO_STACK}
     * if no stack occupied the specified slot.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    protected abstract ItemStack getAndSetSlot(int slot, ItemStack stack);
    
    /**
     * Checks for whether the specified slot is empty. The returned value is
     * equivalent to the expression:
     * <pre>{@link #getStack(int) getStack(slot)} == ItemStack.NO_STACK</pre>
     * 
     * @param slot The slot.
     * 
     * @return {@code true} if the slot is empty; {@code false} otherwise.
     * @throws IndexOutOfBoundsException if {@code slot < 0}, or this is a
     * bounded container and {@code slot >= size()}.
     */
    public boolean isSlotEmpty(int slot) {
        return getStack(slot) == ItemStack.NO_STACK;
    }
    
    @Override
    public boolean contains(Item item) {
        for(int i = 0; i < size(); i++) {
            if(getStack(i).holds(item))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean contains(Item item, int minQuantity) {
        int count = 0;
        for(int i = 0; i < size(); i++) {
            ItemStack s = getStack(i);
            if(s.holds(item) && (count = count + s.getQuantity()) >= minQuantity)
                return true;
        }
        return false;
    }
    
    @Override
    public void clear() {
        for(int i = 0; i < size(); i++)
            setSlot(i, ItemStack.NO_STACK);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Container[");
        sb.append(size());
        sb.append("]: {");
        
        for(int i = 0; i < size(); i++) {
            ItemStack s = getStack(i);
            if(s == ItemStack.NO_STACK)
                continue;
            
            sb.append("\n\t[");
            sb.append(i);
            sb.append("]: ");
            sb.append(s);
        }
        
        sb.append("\n}");
        
        return sb.toString();
    }
    
    @Override
    public void importFromCompound(DataCompound c) {
        DataList l = c.getList("items");
        while(l.hasNext()) {
            DataCompound stack = l.getCompound();
            byte slot = stack.getI8("slot");
            setSlot(slot, ItemStack.createFromCompound(stack));
        }
    }
    
    @Override
    public void exportToCompound(DataCompound c) {
        DataList l = c.childList("items");
        for(int i = 0; i < size(); i++) {
            if(!isSlotEmpty(i)) {
                DataCompound stack = l.childCompound();
                getStack(i).exportToCompound(stack);
                stack.put("slot", (byte)i);
            }
        }
    }
    
    /**
     * Sorts the contents of the Container.
     * 
     * @param comparator The comparator to use when sorting.
     */
    public abstract void sort(Comparator<ItemStack> comparator);
    
    /*
     * compresses matching adjacent stacks into one stack if possible
     */
    /*
    private void compact() {
        if(size() == 0)
            return;
        
        // if stacks are removed due to having their contents dumped into
        // another stack, slots are vacated and we need to keep track of where
        // each following stack should be shifted to
        int firstEmpty = 0;
        ItemStack lastStack = getStack(0); // should never be NO_STACK if sorted
        
        for(int i = 1; i < size(); i++) {
            ItemStack next = getStack(i);
            if(lastStack.holds(next.getItem())) { // items match; try to dump
                if(lastStack.add(next)) { // next has been emptied; vacate its slot
                    firstEmpty = i;
                } else { // next is still there; is there a vacant slot to shift it to?
                    if(firstEmpty != i)
                        
                    lastStack = next;
                }
            } else { // items do not match; reset the stack to dump into and move up
                firstEmpty++;
                lastStack = next;
            }
        }
        
        if(!isBounded())
            ; // clean up excess unbounded slots
    }
    */
    
    @Override
    public void forEach(Consumer<? super ItemStack> action) {
        for(int i = 0; i < size(); i++) {
            ItemStack s = getStack(i);
            if(s != ItemStack.NO_STACK)
                action.accept(s);
        }
    }
    
    @Override
    public Iterator<ItemStack> iterator() {
        return new ContainerIterator();
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    /**
     * An Iterator implementation for a Container.
     */
    private class ContainerIterator implements Iterator<ItemStack> {
        
        /** Index of the last element returned. */
        private int cursor = -1;
        /** Index of the next available element. */
        private int nextLoc = -1;
        
        
        @Override
        public boolean hasNext() {
            return findNext() != -1;
        }
        
        @Override
        public ItemStack next() {
            if(!hasNext()) // advance if an implementation fails to check
                throw new NoSuchElementException();
            cursor = nextLoc;
            return getStack(cursor);
        }
        
        @Override
        public void remove() {
            removeStack(cursor);
        }
        
        /**
         * Finds the index of the next item.
         * 
         * @return nextLoc. A value of {@code -1} indicates there is no next
         * item.
         */
        private int findNext() {
            // Should return here if hasNext() is invoked twice, or next() is
            // invoked after hasNext().
            if(cursor != nextLoc)
                return nextLoc;
            while(++nextLoc < size())
                if(!isSlotEmpty(nextLoc))
                    return nextLoc;
            nextLoc = -1;
            return nextLoc;
        }
        
    }
    
    /**
     * A comparator which sorts a Container's contents in ascending order of
     * ID for its first order, then descending order of stack size for its
     * second order.
     * 
     * <p>Note that even after being sorted in this manner, a container may
     * hold adjacent stacks which may be merged.
     */
    public static final Comparator<ItemStack> COMPARATOR_ID = (s1, s2) -> {
        // ItemStacks in a container should NEVER be null
        //if(s1 == null && s2 == null) return 0;
        //if(s1 == null) return 1;
        //if(s2 == null) return -1;
        
        if(s1 == ItemStack.NO_STACK && s2 == ItemStack.NO_STACK) return 0;
        if(s1 == ItemStack.NO_STACK) return 1;
        if(s2 == ItemStack.NO_STACK) return -1;
        if(s1.getItem().getID() > s2.getItem().getID()) return 1;
        if(s1.getItem().getID() < s2.getItem().getID()) return -1;
        if(s1.getQuantity() > s2.getQuantity()) return -1;
        if(s1.getQuantity() < s2.getQuantity()) return 1;
        return 0;
    };
    
    /**
     * A comparator which sorts a Container's contents in alphabetical order
     * for its first order, then descending stack size for its second order.
     * 
     * <p>Note that even after being sorted in this manner, a container may
     * hold adjacent stacks which may be merged.
     */
    public static final Comparator<ItemStack> COMPARATOR_NAME = (s1, s2) -> {
        // ItemStacks in a container should NEVER be null
        //if(s1 == null && s2 == null) return 0;
        //if(s1 == null) return 1;
        //if(s2 == null) return -1;
        
        if(s1 == ItemStack.NO_STACK && s2 == ItemStack.NO_STACK) return 0;
        if(s1 == ItemStack.NO_STACK) return 1;
        if(s2 == ItemStack.NO_STACK) return -1;
        int r = s1.getItem().getName().compareToIgnoreCase(s2.getItem().getName());
        if(r != 0) return r;
        if(s1.getQuantity() > s2.getQuantity()) return -1;
        if(s1.getQuantity() < s2.getQuantity()) return 1;
        return 0;
    };
    
}
