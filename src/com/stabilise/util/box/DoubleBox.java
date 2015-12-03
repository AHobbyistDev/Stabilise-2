package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

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
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readDouble();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeDouble(value);
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readDouble(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
