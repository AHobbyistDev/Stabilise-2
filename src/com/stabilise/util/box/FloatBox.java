package com.stabilise.util.box;

public class FloatBox implements IBox {
    
    private float value;
    
    
    /**
     * Creates a new FloatBox holding the value 0f.
     */
    public FloatBox() {
        this(0f);
    }
    
    public FloatBox(float value) {
        this.value = value;
    }
    
    public float get()           { return value; }
    public void set(float value) { this.value = value; } 
    
}
