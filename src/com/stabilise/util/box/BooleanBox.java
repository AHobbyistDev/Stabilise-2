package com.stabilise.util.box;

public class BooleanBox implements IBox {
    
    private boolean value;
    
    
    /**
     * Creates a new BooleanBox holding the value false.
     */
    public BooleanBox() {
        this(false);
    }
    
    public BooleanBox(boolean value) {
        this.value = value;
    }
    
    public boolean get()           { return value;       }
    public void set(boolean value) { this.value = value; }
    
}
