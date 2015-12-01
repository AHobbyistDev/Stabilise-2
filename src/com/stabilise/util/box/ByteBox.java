package com.stabilise.util.box;

public class ByteBox implements IBox {
    
    private byte value;
    
    
    /**
     * Creates a new ByteBox holding the value 0.
     */
    public ByteBox() {
        this((byte) 0);
    }
    
    public ByteBox(byte value) {
        this.value = value;
    }
    
    public byte get()           { return value;       }
    public void set(byte value) { this.value = value; }
    
}
