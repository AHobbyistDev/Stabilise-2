package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;


/**
 * A box containing a single short value.
 */
public class I16Box implements ITag {
    
    /** Returns 0 */
    public static short defaultValue() { return 0; }
    
    
    
    private short value;
    
    
    /**
     * Creates a new I16Box holding the value 0.
     */
    public I16Box() {
        this.value = defaultValue();
    }
    
    public I16Box(short value) {
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
    public void read(String name, DataCompound o) {
        value = o.getI16(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getI16();
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
        return new I16Box((short)other.getAsLong());
    }
    
    @Override public boolean isBoolean() { return true; }
    @Override public boolean isLong()    { return true; }
    @Override public boolean isDouble()  { return true; }
    @Override public boolean isString()  { return true; }
    
    @Override public boolean getAsBoolean() { return value != 0;            }
    @Override public long    getAsLong()    { return (long) value;          }
    @Override public double  getAsDouble()  { return (double) value;        }
    @Override public String  getAsString()  { return Short.toString(value); }
    
}
