package com.stabilise.util.box;

public class IntBox implements IBox {
    
    private int value;
    
    
    /**
     * Creates a new IntBox holding the value 0.
     */
    public IntBox() {
        this(0);
    }
    
    public IntBox(int value) {
        this.value = value;
    }
    
    public int get()           { return value; }
    public void set(int value) { this.value = value; }
    
}
