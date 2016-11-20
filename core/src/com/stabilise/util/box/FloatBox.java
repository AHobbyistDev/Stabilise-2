package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;

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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getFloat(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getFloat();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
