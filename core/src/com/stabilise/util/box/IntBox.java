package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getInt(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getInt();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
    
    
    
    
    @Override
    public boolean isCompatibleType(ITag other) {
        return other.isInt();
    }
    
    @Override
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        return new IntBox(other.getAsInt());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value != 0;              }
    @Override public int     getAsInt()     { return value;                   }
    @Override public long    getAsLong()    { return (long) value;            }
    @Override public double  getAsDouble()  { return (double) value;          }
    @Override public String  getAsString()  { return Integer.toString(value); }
    
}
