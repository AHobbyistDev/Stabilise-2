package com.stabilise.util.collect;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.stabilise.util.Checks;
import com.stabilise.util.collect.IDuplicateResolver.Action;


/**
 * A WeightingArrayList is an ArrayList which sorts inserted elements by their
 * {@link IWeightProvider#getWeight() weight}, in order of lowest weight to
 * highest weight.
 */
public class WeightingArrayList<E extends IWeightProvider & IDuplicateResolver<E>>
        extends AbstractList<E>
        implements SimpleList<E>, RandomAccess {
    
    private static final int TREE_THRESHOLD = Integer.MAX_VALUE; // TODO: temporary value
    
    private E[] data;
    private int size = 0;
    
    
    /**
     * Creates a WeightingArrayList with the specified initial capacity.
     * 
     * @param array The internal array to use. This must be explicitly supplied
     * due to the limitations of generics (curse you, type erasure!)
     * 
     * @throws NullPointerException if {@code array} is {@code null}.
     * @throws IllegalArgumentException if {@code array.length == 0}.
     */
    public WeightingArrayList(E[] array) {
        this.data = Objects.requireNonNull(array);
        if(array.length == 0)
            throw new IllegalArgumentException("Zero-length array!");
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException if {@code e} is {@code null}.
     */
    @Override
    public boolean add(E e) {
        Objects.requireNonNull(e);
        int w = e.getWeight(), w2;
        if(size >= TREE_THRESHOLD) {
            return false; // TODO
        } else {
            for(int i = 0; i < size; i++) {
                w2 = data[i].getWeight();
                if(w2 < w) continue;
                else if(w2 == w) {
                    if(data[i].equals(e)) {
                        Action a = data[i].resolve(e);
                        if(a == Action.OVERWRITE) {
                            data[i] = e;
                            return true;
                        } else if(a == Action.REJECT) return false;
                        else continue;
                    }
                } else { // w2 > w
                    shift(i);
                    data[i] = e;
                    size++;
                    return true;
                }
            }
            
            // We've reached the end
            expandIfNecessary();
            data[size++] = e;
            return true;
        }
    }
    
    @Override
    public void append(E e) {
        add(e);
    }
    
    /**
     * Shifts everything at and past the specified index one to the right,
     * expanding the backing array if necessary.
     */
    private void shift(int index) {
        expandIfNecessary();
        System.arraycopy(data, index, data, index + 1, size - index);
    }
    
    private void expandIfNecessary() {
        if(size == data.length)
            data = Arrays.copyOf(data, data.length * 2);
    }
    
    @Override
    public E get(int index) {
        return data[Checks.testMaxIndex(index, size-1)];
    }
    
    @Override
    public E remove(int index) {
        E e = data[Checks.testIndex(index, 0, size-1)];
        doRemove(index);
        return e;
    }
    
    private void doRemove(int index) {
        size--;
        System.arraycopy(data, index+1, data, index, size - index);
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public void clear() {
        for(int i = 0; i < size; i++)
            data[i] = null;
        size = 0;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        return new ListItr(Checks.testIndex(index, 0, size));
    }
    
    @Override
    public void forEach(Consumer<? super E> cons) {
        for(int i = 0; i < size; i++)
            cons.accept(data[i]);
    }
    
    @Override
    public void iterate(Predicate<? super E> pred) {
        for(int i = 0; i < size; i++)
            if(pred.test(data[i]))
                doRemove(i);
    }
    
    @Override
    public boolean iterateUntil(Predicate<? super E> pred) {
        for(int i = 0; i < size; i++) {
            if(pred.test(data[i])) {
                return false;
            }
        }
        return true;
    }
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    private class Itr implements Iterator<E> {
        
        int cursor = 0;
        
        @Override
        public boolean hasNext() {
            return cursor < size;
        }
        
        @Override
        public E next() {
            if(hasNext())
                return data[cursor++];
            else
                throw new NoSuchElementException();
        }
        
        @Override
        public void remove() {
            WeightingArrayList.this.doRemove(--cursor);
        }
        
    }
    
    private class ListItr extends Itr implements ListIterator<E> {
        
        int cursor;
        int lastRet = -1;
        
        ListItr(int cursor) {
            this.cursor = cursor;
        }
        
        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }
        
        @Override
        public E next() {
            lastRet = cursor;
            return super.next();
        }
        
        @Override
        public E previous() {
            if(hasPrevious())
                return data[lastRet = --cursor];
            else
                throw new NoSuchElementException();
        }
        
        @Override
        public int nextIndex() {
            return cursor;
        }
        
        @Override
        public int previousIndex() {
            return cursor - 1;
        }
        
        @Override
        public void set(E e) {
            if(lastRet == -1)
                throw new IllegalStateException();
            WeightingArrayList.this.data[lastRet] = e;
        }
        
        @Override
        public void add(E e) {
            if(lastRet == -1)
                throw new IllegalStateException();
            WeightingArrayList.this.add(lastRet, e);
            cursor++;
            lastRet = -1;
        }
        
    }
    
}
