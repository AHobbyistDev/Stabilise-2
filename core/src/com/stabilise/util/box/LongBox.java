package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getLong(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getLong();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
    
    
    
    
    
    @Override
    public boolean isCompatibleType(ITag other) {
        return other.isLong();
    }
    
    @Override
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        return new LongBox(other.getAsLong());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value != 0;           }
    @Override public long    getAsLong()    { return value;                }
    @Override public double  getAsDouble()  { return (double) value;       }
    @Override public String  getAsString()  { return Long.toString(value); }
    
}
