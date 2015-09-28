package com.stabilise.util.box;

import java.util.Objects;

public class IntArrayBox implements IBox {
    
    private int[] value;
    
    
    /**
     * Creates a new IntArrayBox holding a zero-length int array.
     */
    public IntArrayBox() {
        this.value = new int[0];
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public IntArrayBox(int[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public int[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(int[] value) { this.value = Objects.requireNonNull(value); }
    
}
