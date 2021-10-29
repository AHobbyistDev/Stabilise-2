package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;


/**
 * Boxes a single byte value.
 */
public class I8Box implements IData {
    
    /** Returns 0 */
    public static byte defaultValue() { return 0; }
    
    
    
    private byte value;
    
    
    /**
     * Creates a new ByteBox holding the value 0.
     */
    public I8Box() {
        this.value = defaultValue();
    }
    
    public I8Box(byte value) {
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
    public void read(String name, DataCompound o) {
        value = o.getI8(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getI8();
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
        return DataType.I8;
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
                return new I8Box(value);
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
                return new I8ArrBox(new byte[] {value});
            case I32ARR:
                return new I32ArrBox(new int[] {value});
            case I64ARR:
                return new I64ArrBox(new long[] {value});
            case F32ARR:
                return new F32ArrBox(new float[] {value});
            case F64ARR:
                return new F64ArrBox(new double[] {value});
            case STRING:
                return new StringBox(Byte.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: I8 --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return true; }
    //@Override public boolean isLong()    { return true; }
    //@Override public boolean isDouble()  { return true; }
    //@Override public boolean isString()  { return true; }
    
    //@Override public boolean getAsBoolean() { return value != 0;           }
    //@Override public long    getAsLong()    { return value;                }
    //@Override public double  getAsDouble()  { return value;                }
    //@Override public String  getAsString()  { return Byte.toString(value); }
    
    @Override
    public I8Box duplicate() {
        return new I8Box(value);
    }
    
}
