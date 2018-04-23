package com.stabilise.util.box;

import com.stabilise.util.Checks;

/**
 * Provides static helper methods for utilising {@link Box}es.
 */
public class Boxes {
    
    private static final Box<Object> EMPTY = new ImmutBox<>(null);
    
    private Boxes() {} // non-instantiable
    
    
    /**
     * Returns a new box containing {@code null}. The returned box supports
     * {@link Box#set(Object) set()}.
     */
    public static <T> Box<T> emptyMut() {
        return new MutBox<T>(null);
    }
    
    /**
     * Returns an immutable box containing {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Box<T> empty() {
        return (Box<T>) EMPTY;
    }
    
    /**
     * Returns a new box containing the specified object. The returned box
     * supports {@link Box#set(Object) set()}.
     * 
     * @param object The object to box. May be null.
     */
    public static <T> Box<T> box(T object) {
        return new MutBox<T>(object);
    }
    
    /**
     * Returns a new box containing a volatile reference to the specified
     * object. The returned box supports {@link Box#set(Object) set()}.
     * 
     * @param object The object to box. May be {@code null}.
     * @see java.util.concurrent.atomic.AtomicReference
     */
    public static <T> Box<T> boxVolatile(T object) {
        return new MutVolatileBox<T>(object);
    }
    
    public static BoolBox box(boolean data) {
        return BoolBox.valueOf(data);
    }
    
    public static ByteArrBox box(byte[] data) {
        return new ByteArrBox(data);
    }
    
    public static ByteBox box(byte data) {
        return new ByteBox(data);
    }
    
    public static CharBox box(char data) {
        return new CharBox(data);
    }
    
    public static DoubleBox box(double data) {
        return new DoubleBox(data);
    }
    
    public static FloatBox box(float data) {
        return new FloatBox(data);
    }
    
    public static IntArrBox box(int[] data) {
        return new IntArrBox(data);
    }
    
    public static IntBox box(int data) {
        return new IntBox(data);
    }
    
    public static LongBox box(long data) {
        return new LongBox(data);
    }
    
    public static ShortBox box(short data) {
        return new ShortBox(data);
    }
    
    public static StringBox box(String data) {
        return new StringBox(data);
    }
    
    
    static class MutBox<T> implements Box<T> {
        protected T value;
        
        public MutBox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { value = t; }
    }
    
    static class ImmutBox<T> implements Box<T> {
        protected final T value;
        
        public ImmutBox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { Checks.unsupported(); }
    }
    
    static class MutVolatileBox<T> implements Box<T> {
        protected volatile T value;
        
        public MutVolatileBox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { value = t; }
    }
    
}
