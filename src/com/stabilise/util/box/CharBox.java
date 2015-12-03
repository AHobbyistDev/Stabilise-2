package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

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
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readChar();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeChar(value);
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readChar(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
