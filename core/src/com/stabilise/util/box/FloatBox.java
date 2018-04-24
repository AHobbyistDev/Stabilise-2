package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

public class FloatBox implements ITag {
    
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
    public void read(String name, DataCompound o) {
        value = o.getFloat(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getFloat();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "" + value;
    }
    
    
    
    
    
    @Override
    public boolean isCompatibleType(ITag other) {
        return other.isFloat();
    }
    
    @Override
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        return new FloatBox(other.getAsFloat());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value != 0;            }
    @Override public long    getAsLong()    { return (long) value;          }
    @Override public float   getAsFloat()   { return value;                 }
    @Override public double  getAsDouble()  { return (double) value;        }
    @Override public String  getAsString()  { return Float.toString(value); }
    
}
