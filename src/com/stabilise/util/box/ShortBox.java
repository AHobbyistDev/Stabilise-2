package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

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
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readShort();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeShort(value);
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readShort(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
