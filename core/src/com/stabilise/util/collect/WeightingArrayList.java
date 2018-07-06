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
 * 
 * <p>See {@link #add(IWeightProvider)} for more details on how element
 * insertion works.
 */
public class WeightingArrayList<E extends IWeightProvider & IDuplicateResolver<E>>
        extends AbstractList<E>
        implements SimpleList<E>, RandomAccess {
    
    private static final int BINARY_SEARCH_THRESHOLD = Integer.MAX_VALUE; // TODO: temporary value
    
    private E[] data;
    private int size = 0;
    
    // Since this class allows list modification during iteration, inserting a
    // low weight element may cause a higher weight element to be iterated over
    // more than once (since it will be shifted right in the array). To avoid
    // any possibility of this occurring we use itrIdx and itrInc.
    
    /** Used instead of a local index variable when iterating. When an element
     * is inserted before this index, itrInc is incremented so that the current
     * element being iterated over is not iterated again. In each iteration
     * function where itrIdx is used, we store the value of itrIdx when the
     * function begins and reset to that when we are done as to allow reentrant
     * iteration (i.e. allow nested iterations). */
    private int itrIdx = -1;
    
    
    /**
     * Creates a WeightingArrayList using the given initial internal array.
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
     * Adds the given element to the list. The element is added after all
     * elements with a lower weight, and before all elements with a higher
     * weight, shifting if necessary. If other elements with the same weight
     * are present, then {@code e} is added after all of them. If however one
     * (or more) of the elements with the same weight is {@link
     * Object#equals(Object) equal} to {@code e}, then {@link
     * IDuplicateResolver#resolve(Object) elementAlreadyPresent.resolve(e)} is
     * invoked and {@code e} is only added if the result of the invocation is
     * either {@link IDuplicateResolver.Action#KEEP_BOTH KEEP_BOTH} or {@link
     * IDuplicateResolver.Action#OVERWRITE OVERWRITE}.
     * 
     * @return true if {@code e} was successfully added; false if an equal
     * element already present {@link IDuplicateResolver.Action#REJECT
     * rejected} the insertion of {@code e}.
     * @throws NullPointerException if {@code e} is {@code null}.
     */
    @Override
    public boolean add(E e) {
        Objects.requireNonNull(e);
        int w = e.getWeight(), w2;
        if(size >= BINARY_SEARCH_THRESHOLD) {
            return false; // TODO
        } else {
            for(int i = 0; i < size; i++) {
                w2 = data[i].getWeight();
                if(w2 < w) continue;
                else if(w2 > w) {
                    shift(i);
                    data[i] = e;
                    size++;
                    if(i <= itrIdx)
                        itrIdx++;
                    return true;
                } else { // w2 == w
                    if(data[i].equals(e)) {
                        Action a = data[i].resolve(e);
                        if(a == Action.OVERWRITE) {
                            data[i] = e;
                            return true;
                        } else if(a == Action.REJECT) return false;
                        else continue; // Action.KEEP_BOTH
                    }
                }
            }
            
            // We've reached the end
            expandIfNecessary();
            data[size++] = e;
            return true;
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see #add(IWeightProvider)
     */
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
        //for(int i = 0; i < size; i++)
        //    cons.accept(data[i]);
        
        int oldItrIdx = itrIdx;
        for(itrIdx = 0; itrIdx < size; itrIdx++)
            cons.accept(data[itrIdx]);
        itrIdx = oldItrIdx;
    }
    
    /**
     * Performs the given action on each element in this list whose weight is
     * less than or equal to the specified weight. This method is useful if you
     * only care about things near the start of a list.
     */
    public void forEach(Consumer<? super E> cons, int maxWeight) {
        //E e;
        //for(int i = 0; i < size && (e = data[i]).getWeight() <= maxWeight; i++)
        //    cons.accept(e);
        
        int oldItrIdx = itrIdx;
        E e;
        for(itrIdx = 0; itrIdx < size && (e = data[itrIdx]).getWeight() <= maxWeight; itrIdx++)
            cons.accept(e);
        itrIdx = oldItrIdx;
    }
    
    /**
     * Iterates <em>backwards</em> through this list, performing the given
     * action on any each element whose weight is greater than or equal to the
     * specified weight.
     * 
     * <p>Important note: make absolutely sure that no new elements are added
     * to this list during this backwards iteration, or elements may be
     * skipped!
     */
    public void forEachBackwards(Consumer<? super E> cons, int minWeight) {
        // Can't use itrIdx because it's only set up for forwards iteration.
        
        E e;
        for(int i = size-1; i >= 0 && (e = data[i]).getWeight() >= minWeight; i--)
            cons.accept(e);
    }
    
    @Override
    public void iterate(Predicate<? super E> pred) {
        //for(int i = 0; i < size; i++) {
        //    if(pred.test(data[i])) {
        //        doRemove(i);
        //        i--;
        //    }
        //}
        
        int oldItrIdx = itrIdx;
        for(itrIdx = 0; itrIdx < size; itrIdx++) {
            if(pred.test(data[itrIdx])) {
                doRemove(itrIdx);
                itrIdx--;
            }
        }
        itrIdx = oldItrIdx;
    }
    
    @Override
    public boolean any(Predicate<? super E> pred) {
        //for(int i = 0; i < size; i++)
        //    if(pred.test(data[i]))
        //        return true;
        //return false;
        
        int oldItrIdx = itrIdx;
        for(itrIdx = 0; itrIdx < size; itrIdx++) {
            if(pred.test(data[itrIdx])) {
                itrIdx = oldItrIdx;
                return true;
            }
        }
        itrIdx = oldItrIdx;
        return false;
    }
    
    /**
     * Much like {@link #any(Predicate)}, but iterates <em>backwards</em>
     * through the list, and only tests the given predicate on elements with a
     * weight greater than or equal to {@code minWeight}.
     * 
     * <p>Important note: make absolutely sure that no new elements are added
     * to this list during this backwards iteration, or elements may be
     * skipped!
     */
    public boolean anyBackwards(Predicate<? super E> pred, int minWeight) {
        // Can't use itrIdx here because it's only set up for forwards
        // iteration.
        
        E e;
        for(int i = size-1; i >= 0 && (e = data[i]).getWeight() >= minWeight; i--)
            if(pred.test(e))
                return true;
        return false;
    }
    
    @Override
    public boolean all(Predicate<? super E> pred) {
        //for(int i = 0; i < size; i++)
        //    if(!pred.test(data[i]))
        //        return false;
        //return true;
        
        int oldItrIdx = itrIdx;
        for(itrIdx = 0; itrIdx < size; itrIdx++) {
            if(!pred.test(data[itrIdx])) {
                itrIdx = oldItrIdx;
                return false;
            }
        }
        itrIdx = oldItrIdx;
        return true;
    }
    
    /**
     * Internally swaps this WeightingArrayList with the given other list.
     */
    /*
    public void swap(WeightingArrayList<E> other) {
        E[] tmpData = other.data;
        other.data = data;
        data = tmpData;
        
        int tmpSize = other.size;
        other.size = size;
        size = tmpSize;
    }
    */
    
    //--------------------==========--------------------
    //-------------=====Nested Classes=====-------------
    //--------------------==========--------------------
    
    // TODO: use itrIdx co in the iterators
    
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
