package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;

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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getShort(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getShort();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
