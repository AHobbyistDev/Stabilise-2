package com.stabilise.util.collect;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.concurrent.NotThreadSafe;

import com.stabilise.util.Checks;

/**
 * A FragList (or "fragmented list") is a variant on an ArrayList which
 * replaces removed elements with {@code null} in preference to filling up the
 * gap. This reduces overhead of removal operations at the cost of fragmenting
 * the list with nulls which must be skipped over during iteration. A FragList
 * may be {@link #flatten() flattened} to eliminate such nulls, but this can be
 * a costly operation and so should not be performed frequently.
 * 
 * <p>FragList accepts a parameter {@code flattenThreshold}, which determines
 * how often the list is automatically flattened. When the percentage of nulls
 * throughout the list exceeds the threshold, the list is flattened upon an
 * invocation of {@link #iterate(Predicate) forEach}() to eliminate those
 * nulls. A lower threshold means a FragList will be flattened more often,
 * which helps preserve data locality and reduce iteration overhead, but can
 * incur significant overhead. A higher threshold results in less frequent
 * flattening, but may result in the internal array being intermediated with
 * too many nulls. Flattening becomes less useful if elements are frequently
 * added to a FragList, as new elements will fill up the nulls on their own.
 * 
 * <p>A FragList does not preserve the insertion order of elements, but
 * guarantees that the relative ordering of any two elements never changes
 * (i.e. if element A precedes element B in the list, this will never change).
 * The primary purpose of a FragList is (try to) to offer a compromise between
 * the highly performant {@link UnorderedArrayList} and slower but
 * order-preserving lists such as {@link LinkedList}.
 * 
 * <p>This class does not implement the {@link List} interface as a FragList
 * generally isn't useful as an ordinary list, and the only operations for
 * which one comes in handy are those provided by {@link SimpleList}.
 * 
 * <h3><b>Example Scenario</b></h3>
 * 
 * <p>Consider the following FragList, where A-F represent generic elements and
 * an underscore represents an unused position in the list (i.e. a null):
 * 
 * <pre>
 * [ A, B, C, D, E, F, _, _ ] : size = 6
 * </pre>
 * 
 * <p>If we invoked {@code list.put(G)}, the list will look like (noting that
 * if the internal array were full, it would be resized before adding the new
 * element):
 * 
 * <pre>
 * [ A, B, C, D, E, F, G, _ ] : size = 7
 * </pre>
 * 
 * <p>Suppose we then iterated over the list using {@link #iterate(Predicate)}
 * and removed elements A, C and D. Assuming the list isn't flattened, it would
 * now look like:
 * 
 * <pre>
 * [ _, B, _, _, E, F, G, _ ] : size = 4
 * </pre>
 * 
 * <p>Here we can clearly see how the list has fragmented. If we wanted to add
 * a new element H ({@code list.put(H)}), the list will look like:
 * 
 * <pre>
 * [ H, B, _, _, E, F, G, _ ] : size = 5
 * </pre>
 * 
 * <p>We see that a FragList prefers to fill up gaps before appending to the
 * end of the list. As the final operation in this example, we can choose to
 * flatten the list, resulting in a final form:
 * 
 * <pre>
 * [ H, B, E, F, G, _, _, _ ] : size = 5
 * </pre>
 */
@NotThreadSafe
public class FragList<E> implements SimpleList<E> {
    
    /** The backing array.
     * Invariant: length >= size */
    private E[] data;
    /** The size of this list.
     * Invariant: size <= lastElement */
    private int size = 0;
    /** The index of the last element. Equivalent to size for a traditional
     * list.
     * Invariant: data.length >= lastElement
     *            size <= lastElement+1 */
    private int lastElement = -1;
    /** The index of the first "gap", or null element.
     * If the list is not contiguous, this is equal to lastElement+1. */
    private int firstNull = 0;
    
    /** When the percentage of nulls in this list (given by 1-size/lastElement)
     * exceeds this threshold, the list will be automatically flattened upon
     * an invocation of forEach(). */
    private float flattenThreshold;
    
    
    /**
     * Creates a new FragList with an initial capacity of 16 and a flattening
     * threshold of 0.25.
     */
    public FragList() {
        this(16, 0.25f);
    }
    
    /**
     * Creates a new FragList.
     * 
     * @param capacity The initial internal array length.
     * @param flattenThreshold When the percentage of nulls in this list
     * exceeds this threshold, this list will be automatically flattened upon
     * an invocation of {@link #iterate(Predicate)}.
     * 
     * @throws NegativeArraySizeException if {@code capacity} is negative.
     * @throws IllegalArgumentException if flattenThreshold < 0 ||
     * flattenThreshold > 1.
     */
    public FragList(int capacity, float flattenThreshold) {
        @SuppressWarnings("unchecked")
        final E[] arr = (E[])new Object[capacity];
        data = arr;
        this.flattenThreshold = Checks.test(flattenThreshold, 0f, 1f);
    }
    
    @Override
    public int size() {
        return size;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @throws NullPointerException if {@code e} is {@code null}.
     */
    @Override
    public void append(E e) {
        /*
        // This one just appends to the end, and doesn't try looking for the
        // first null.
        Objects.requireNonNull(e);
        if(++lastElement == data.length)
            expand();
        data[lastElement] = e;
        size++;
        if(firstNull == lastElement)
            firstNull++;
        */
        Objects.requireNonNull(e);
        if(firstNull == data.length)
            expand();
        if(firstNull == lastElement+1)
            lastElement++;
        data[firstNull] = e;
        size++;
        findNextNull();
    }
    
    @Override
    public void clear() {
        size = 0;
        lastElement = -1;
        firstNull = 0;
        resize(0);
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    @Override
    public void iterate(Predicate<? super E> action) {
        Objects.requireNonNull(action); // fail-fast
        
        for(int i = 0; i <= lastElement; i++) {
            if(data[i] != null && action.test(data[i]))
                remove(i);
        }
        
        flatten(flattenThreshold);
    }
    
    /** Removes the element at index i and decrements size. Adjusts firstNull
     * and lastElement if appropriate. */
    private void remove(int i) {
        data[i] = null;
        size--;
        if(i == lastElement) {
            // This was the last element, so we keep decrementing
            // lastElement until we land on something non-null.
            while(lastElement > 0 && data[--lastElement] == null) {}
            if(firstNull > lastElement)
                firstNull = lastElement + 1;
        } else if(i < firstNull) {
            firstNull = i;
        }
    }
    
    @Override
    public void forEach(Consumer<? super E> cons) {
        Objects.requireNonNull(cons);
        
        for(int i = 0; i <= lastElement; i++) {
            if(data[i] != null)
                cons.accept(data[i]);
        }
        
        // No flattening here since element removal does not occur.
    }
    
    /**
     * Flattens this list, removing any intermediate nulls.
     */
    public void flatten() {
        for(int i = firstNull + 1; i <= lastElement; i++) {
            if(data[i] != null) {
                data[firstNull] = data[i];
                data[i] = null;
                if(i == lastElement)
                    lastElement = firstNull;
                findNextNull();
            }
        }
    }
    
    /**
     * Flattens this list if the percentage of nulls is greater than the
     * specified threshold (that is, {@code threshold} should be between 0 and
     * 1).
     */
    public void flatten(float threshold) {
        if(size > 0 && 1f - (float)size / lastElement > threshold)
            flatten();
    }
    
    /**
     * Resizes the backing array to {@code length} if it is smaller than {@code
     * length}. This method can be useful for preventing excessive array
     * expansions, which can be wasteful.
     */
    public void ensureInternalLength(int length) {
        if(data.length < length)
            resize(length);
    }
    
    private void resize(int newLength) {
        data = Arrays.copyOf(data, newLength);
    }
    
    /**
     * Expands the array by the scaling factor to the next {@link
     * #scaleLength() scaled length}. The array size is guaranteed to increase
     * by at least 1.
     */
    private void expand() {
        resize(scaleLength());
    }
    
    private int scaleLength() {
        return (int)(data.length << 1) + 1;
    }
    
    /** Traverses the list starting from the current value of firstNull until
     * either another null is found or the end of the list is reached. */
    private void findNextNull() {
        do {
            firstNull++;
        } while(firstNull <= lastElement && data[firstNull] != null);
    }
    
    /**
     * Returns a debug string of this list.
     */
    protected String toStringDebug() {
        StringBuilder sb = new StringBuilder();
        sb.append("FragList[");
        sb.append(size);
        sb.append("/");
        sb.append(data.length);
        sb.append("] {\n");
        
        for(int i = 0; i < Math.min(32, data.length); i++) {
            sb.append("  [");
            if(i == lastElement && i == firstNull)
                sb.append("FILA");
            else if(i == firstNull)
                sb.append("NULL");
            else if(i == lastElement)
                sb.append("LAST");
            else
                sb.append("    ");
            sb.append("] = ");
            sb.append(data[i]);
            sb.append("\n");
        }
        
        sb.append("}");
        
        return sb.toString();
    }
    
    private class Itr implements Iterator<E> {
        
        private int cursor = -1;
        private int next = -1;
        
        /** returns next; -1 if no more */
        private int peek() {
            next = cursor + 1;
            while(next <= FragList.this.lastElement) {
                if(FragList.this.data[next] != null)
                    return next;
                next++;
            }
            next = -1;
            return -1;
        }
        
        @Override
        public boolean hasNext() {
            return next != -1 || peek() != -1;
        }
        
        @Override
        public E next() {
            if(!hasNext())
                throw new NoSuchElementException();
            cursor = next;
            next = -1;
            return FragList.this.data[cursor];
        }
        
        @Override
        public void remove() {
            FragList.this.remove(cursor);
        }
        
    }
    
}
