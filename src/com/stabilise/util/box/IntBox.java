package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

public class IntBox implements IBox {
    
    private int value;
    
    
    /**
     * Creates a new IntBox holding the value 0.
     */
    public IntBox() {
        this(0);
    }
    
    public IntBox(int value) {
        this.value = value;
    }
    
    public int get()           { return value; }
    public void set(int value) { this.value = value; }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readInt();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(value);
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readInt(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
