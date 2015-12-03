package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

public class LongBox implements IBox {
    
    private long value;
    
    
    /**
     * Creates a new LongBox holding the value 0.
     */
    public LongBox() {
        this(0L);
    }
    
    public LongBox(long value) {
        this.value = value;
    }
    
    public long get()           { return value; }
    public void set(long value) { this.value = value; }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readLong();
    }

    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeLong(value);
    }
    
    @Override
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readLong(name);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
}
