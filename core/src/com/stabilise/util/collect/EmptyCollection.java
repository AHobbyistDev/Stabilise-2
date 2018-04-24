package com.stabilise.util.collect;

import java.util.Collection;
import java.util.Iterator;


/**
 * A simple class representing an empty collection.
 */
public class EmptyCollection<E> implements Collection<E> {
    
    
    public static final EmptyCollection<Object> INSTANCE = new EmptyCollection<>();
    
    
    /**
     * Gets the global EmptyCollection instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> get() {
        return (Collection<T>) INSTANCE;
    }
    
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public boolean isEmpty() {
        return true;
    }
    
    @Override
    public boolean contains(Object o) {
        return false;
    }
    
    @Override
    public Iterator<E> iterator() {
        return IteratorUtils.emptyIterator();
    }
    
    @Override
    public Object[] toArray() {
        return new Object[0];
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        return (T[]) new Object[0];
    }
    
    @Override
    public boolean add(E e) {
        return false;
    }
    
    @Override
    public boolean remove(Object o) {
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }
    
    @Override
    public void clear() {
        
    }
    
}
