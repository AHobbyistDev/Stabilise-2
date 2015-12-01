package com.stabilise.util.box;

import java.util.Objects;

public class ByteArrayBox implements IBox {
    
    private byte[] value;
    
    
    /**
     * Creates a new ByteArrayBox holding a zero-length byte array.
     */
    public ByteArrayBox() {
        this.value = new byte[0];
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public ByteArrayBox(byte[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public byte[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(byte[] value) { this.value = Objects.requireNonNull(value);  }
    
}
