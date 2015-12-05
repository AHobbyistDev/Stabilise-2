package com.stabilise.util.box;

/**
 * Provides static helper methods for utilising {@link Box}es.
 */
public class Boxes {
    
    private Boxes() {} // non-instantiable
    
    /**
     * Returns a box containing {@code null}. The returned box supports {@link
     * Box#set(Object) set()}.
     */
    public static <T> Box<T> empty() {
        return new ABox<T>(null);
    }
    
    /**
     * Returns a box containing the specified object. The returned box supports
     * {@link Box#set(Object) set()}.
     * 
     * @param object The object to box. May be null.
     */
    public static <T> Box<T> box(T object) {
        return new ABox<T>(object);
    }
    
    /**
     * Returns a box containing a volatile reference to the specified object.
     * The returned box supports {@link Box#set(Object) set()}.
     * 
     * @param object The object to box. May be {@code null}.
     * @see java.util.concurrent.atomic.AtomicReference
     */
    public static <T> Box<T> boxVolatile(T object) {
        return new AVolatileBox<T>(object);
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
    
    
    static class ABox<T> implements Box<T> {
        protected T value;
        
        public ABox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { value = t; }
    }
    
    static class AVolatileBox<T> implements Box<T> {
        protected volatile T value;
        
        public AVolatileBox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { value = t; }
    }
    
}
