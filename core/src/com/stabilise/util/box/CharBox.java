package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

public class CharBox implements ITag {
    
    private char value;
    
    
    /**
     * Creates a new CharBox holding a char with value 0.
     */
    public CharBox() {
        this((char) 0);
    }
    
    public CharBox(char value) {
        this.value = value;
    }
    
    public char get()           { return value; }
    public void set(char value) { this.value = value; }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = in.readChar();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeChar(value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getChar(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getChar();
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
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        return new CharBox((char)other.getAsLong());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value != 0;                }
    @Override public long    getAsLong()    { return (long) value;              }
    @Override public double  getAsDouble()  { return (double) value;            }
    @Override public String  getAsString()  { return Character.toString(value); }
    
}
