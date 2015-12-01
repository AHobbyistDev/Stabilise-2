package com.stabilise.util.box;

public class LongBox implements IBox {
    
    private long value;
    
    
    /**
     * Creates a new LongBox holding the value 0.
     */
    public LongBox() {
        this(0L);
    }
    
    public LongBox(long value) {
        this.value = value;
    }
    
    public long get()           { return value; }
    public void set(long value) { this.value = value; }
    
}
