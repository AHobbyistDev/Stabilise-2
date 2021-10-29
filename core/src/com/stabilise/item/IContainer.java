package com.stabilise.item;


public interface IContainer {
    
    /**
     * Gets the size of this container.
     * 
     * <ul>
     * <li>If this container is <b>bounded</b>, the returned value is its
     *     capacity. This value is such that any number {@code slot} in the
     *     domain {@code 0 <= slot < size()} should never throw an {@code
     *     IndexOutOfBoundsException} for any relevant methods.
     * <li>If this container is <b>unbounded</b>, the returned value is
     *     usually equivalent to the index of the largest occupied slot plus
     *     one.
     * </ul>
     * 
     * @return The size of this container.
     */
    int size();
    
    /**
     * Checks for whether an item stack is may be added to this container - that
     * is, whether a valid slot exists in which at least one of the item can be
     * placed.
     *
     * @return {@code true} if the stack may be to be added; {@code false}
     * otherwise.
     * @throws NullPointerException if {@code stack} is {@code null}.
     */
    boolean canAddStack(ItemStack stack);
    
    /**
     * Adds a specified quantity of items to the container in the first
     * available slot (or slots, if such a quantity does not fit in a single
     * stack; larger quantities will be partitioned into multiple stacks). If
     * there are any incompletely filled stacks with a matching item in the
     * container, they will be added to before any new slots are used.
     * 
     * @param item The template of the item(s) to add.
     * @param quantity The number of items to add.
     * 
     * @return The number of items which were not added to the container.
     * @throws NullPointerException if {@code item} is {@code null}.
     * @throws IllegalArgumentException if {@code quantity <= 0}.
     */
    int addItem(Item item, int quantity);
    
    /**
     * Adds an item stack to the container in the first available slot (or
     * slots, if it does not fit in a single slot). If there are any
     * incompletely filled stacks with a matching item in the container, they
     * will be added to before any new slots are used.
     * 
     * <p>If this method returns {@code true}, ownership of {@code stack}
     * should be relinquished as it is now considered property of this
     * Container.
     * 
     * @param stack The item stack to add to the container.
     * 
     * @return {@code true} if the stack was added in its entirety and should
     * be released; {@code false} otherwise.
     * @throws NullPointerException if {@code stack} is {@code null}.
     */
    boolean addStack(ItemStack stack);
    
    /**
     * Checks for whether this container contains any number of the specified
     * item.
     */
    boolean contains(Item item);
    
    /**
     * Checks for whether this container contains at least the specified
     * minimum quantity of the specified item.
     */
    boolean contains(Item item, int minQuantity);
    
    /**
     * Clears the contents of the container. These contents will be garbage
     * collected if not otherwise referenced.
     */
    void clear();
    
}
