package com.stabilise.util.box;

public class DoubleBox implements IBox {
    
    private double value;
    
    
    /**
     * Creates a new DoubleBox holding the value 0d.
     */
    public DoubleBox() {
        this(0d);
    }
    
    public DoubleBox(double value) {
        this.value = value;
    }
    
    public double get()           { return value; }
    public void set(double value) { this.value = value; }
    
}
