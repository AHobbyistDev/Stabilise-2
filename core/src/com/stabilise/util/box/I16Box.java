package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;


/**
 * A box containing a single short value.
 */
public class I16Box implements IData {
    
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
    public DataType type() {
        return DataType.I16;
    }
    
    @Override
    public boolean canConvertToType(DataType type) {
        switch(type) {
            case BOOL:
            case I8:
            case I16:
            case I32:
            case I64:
            case F32:
            case F64:
            case I8ARR:
            case I32ARR:
            case I64ARR:
            case F32ARR:
            case F64ARR:
            case STRING:
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public IData convertToType(DataType type) {
        switch(type) {
            case BOOL:
                return new BoolBox(value != 0);
            case I8:
                return new I8Box((byte) value);
            case I16:
                return new I16Box(value);
            case I32:
                return new I32Box(value);
            case I64:
                return new I64Box(value);
            case F32:
                return new F32Box(value);
            case F64:
                return new F64Box(value);
            case I8ARR:
                return new I8ArrBox(new byte[] {(byte) value});
            case I32ARR:
                return new I32ArrBox(new int[] {value});
            case I64ARR:
                return new I64ArrBox(new long[] {value});
            case F32ARR:
                return new F32ArrBox(new float[] {value});
            case F64ARR:
                return new F64ArrBox(new double[] {value});
            case STRING:
                return new StringBox(Short.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: I16 --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return true; }
    //@Override public boolean isLong()    { return true; }
    //@Override public boolean isDouble()  { return true; }
    //@Override public boolean isString()  { return true; }
    
    //@Override public boolean getAsBoolean() { return value != 0;            }
    //@Override public long    getAsLong()    { return value;                 }
    //@Override public double  getAsDouble()  { return value;                 }
    //@Override public String  getAsString()  { return Short.toString(value); }
    
    @Override
    public I16Box duplicate() {
        return new I16Box(value);
    }
    
}
