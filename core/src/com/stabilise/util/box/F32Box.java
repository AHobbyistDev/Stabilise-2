package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;

/**
 * Boxes a single float value.
 */
public class F32Box implements IData {
    
    /** Returns 0.0f */
    public static float defaultValue() { return 0f; }
    
    
    
    private float value;
    
    
    /**
     * Creates a new F32Box holding the value 0f.
     */
    public F32Box() {
        this(defaultValue());
    }
    
    public F32Box(float value) {
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
        value = o.getF32(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getF32();
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
        return DataType.F32;
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
                return new I16Box((short) value);
            case I32:
                return new I32Box((int) value);
            case I64:
                return new I64Box((long) value);
            case F32:
                return new F32Box(value);
            case F64:
                return new F64Box(value);
            case I8ARR:
                return new I8ArrBox(new byte[] {(byte) value});
            case I32ARR:
                return new I32ArrBox(new int[] {(int) value});
            case I64ARR:
                return new I64ArrBox(new long[] {(long) value});
            case F32ARR:
                return new F32ArrBox(new float[] {value});
            case F64ARR:
                return new F64ArrBox(new double[] {value});
            case STRING:
                return new StringBox(Float.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: F32 --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return true; }
    //@Override public boolean isLong()    { return true; }
    //@Override public boolean isDouble()  { return true; }
    //@Override public boolean isString()  { return true; }
    
    //@Override public boolean getAsBoolean() { return value != 0;            }
    //@Override public long    getAsLong()    { return (long) value;          }
    //@Override public float   getAsFloat()   { return value;                 }
    //@Override public double  getAsDouble()  { return value;                 }
    //@Override public String  getAsString()  { return Float.toString(value); }
    
    @Override
    public F32Box duplicate() {
        return new F32Box(value);
    }
    
}
