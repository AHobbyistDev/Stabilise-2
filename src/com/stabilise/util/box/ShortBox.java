package com.stabilise.util.box;

public class ShortBox implements IBox {
    
    private short value;
    
    
    /**
     * Creates a new ShortBox holding the value 0.
     */
    public ShortBox() {
        this((short) 0);
    }
    
    public ShortBox(short value) {
        this.value = value;
    }
    
    public short get()           { return value; }
    public void set(short value) { this.value = value; }
    
}
