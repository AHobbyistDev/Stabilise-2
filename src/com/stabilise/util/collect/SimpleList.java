package com.stabilise.util.collect;

/**
 * SimpleList provides a unifying interface for a variety of simple list
 * implementations designed for element addition, iteration, and element
 * removal during iteration.
 */
public interface SimpleList<E> extends FunctionalIterable<E> {
    
    /**
     * Adds an element to this list.
     * 
     * <p>This method is named "append" rather than "add" as to avoid naming
     * conflicts with {@link java.util.List#add(Object) List.add()}.
     * 
     * @param e The element to add.
     * 
     * @throws NullPointerException if {@code e} is {@code null}, and this list
     * does not permit null elements.
     */
    void append(E e);
    
    /**
     * Returns the number of elements in this list.
     */
    @Override
    int size();
    
    /**
     * Clears this list.
     */
    void clear();
    
}
