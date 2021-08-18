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
        return new MutBox<>(null);
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
        return new MutBox<>(object);
    }
    
    /**
     * Returns a new box containing a volatile reference to the specified
     * object. The returned box supports {@link Box#set(Object) set()}.
     * 
     * @param object The object to box. May be {@code null}.
     * @see java.util.concurrent.atomic.AtomicReference
     */
    public static <T> Box<T> boxVolatile(T object) {
        return new MutVolatileBox<>(object);
    }
    
    public static BoolBox box(boolean data) {
        return BoolBox.valueOf(data);
    }
    
    public static I8Box box(byte data) {
        return new I8Box(data);
    }
    
    public static I16Box box(short data) {
        return new I16Box(data);
    }
    
    public static I32Box box(int data) {
        return new I32Box(data);
    }
    
    public static I64Box box(long data) {
        return new I64Box(data);
    }
    
    public static F32Box box(float data) {
        return new F32Box(data);
    }
    
    public static F64Box box(double data) {
        return new F64Box(data);
    }
    
    public static I8ArrBox box(byte[] data) {
        return new I8ArrBox(data);
    }
    
    public static I32ArrBox box(int[] data) {
        return new I32ArrBox(data);
    }
    
    public static I64ArrBox box(long[] data) {
        return new I64ArrBox(data);
    }
    
    public static F32ArrBox box(float[] data) {
        return new F32ArrBox(data);
    }
    
    public static F64ArrBox box(double[] data) {
        return new F64ArrBox(data);
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
