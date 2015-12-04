package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;

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
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readByte();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(value);
    }
    
    @Override
    public void write(String name, DataObject o) {
        o.put(name, value);
    }

    @Override
    public void read(String name, DataObject o) {
        value = o.getByte(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }

    @Override
    public void read(DataList l) {
        value = l.getByte();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
