package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

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
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readFloat();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeFloat(value);
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readFloat(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
