package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getDouble(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getDouble();
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
    
    
    
    
    @Override
    public boolean isCompatibleType(ITag other) {
        return other.isDouble();
    }
    
    @Override
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        return new DoubleBox(other.getAsDouble());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value != 0;             }
    @Override public long    getAsLong()    { return (long) value;           }
    @Override public double  getAsDouble()  { return value;                  }
    @Override public String  getAsString()  { return Double.toString(value); }
    
}
