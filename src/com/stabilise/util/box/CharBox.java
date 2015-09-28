package com.stabilise.util.box;

public class CharBox implements IBox {
    
    private char value;
    
    
    /**
     * Creates a new CharBox holding a char with value 0.
     */
    public CharBox() {
        this((char) 0);
    }
    
    public CharBox(char value) {
        this.value = value;
    }
    
    public char get()           { return value; }
    public void set(char value) { this.value = value; }
    
}
