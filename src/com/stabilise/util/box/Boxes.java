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
        return box(null);
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
    
    static class ABox<T> implements Box<T> {
        private T value;
        
        public ABox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { value = t; }
    }
    
    static class AVolatileBox<T> implements Box<T> {
        private volatile T value;
        
        public AVolatileBox(T value) { this.value = value; }
        
        @Override public T get()       { return value; }
        @Override public void set(T t) { value = t; }
    }
    
}
