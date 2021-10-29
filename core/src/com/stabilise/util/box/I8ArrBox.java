package com.stabilise.util.box;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import com.stabilise.util.Checks;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;

/**
 * Boxes an array of bytes.
 */
public class I8ArrBox implements IData {
    
    /** Returns a zero-length array */
    public static byte[] defaultValue() { return new byte[0]; }
    
    
    
    private byte[] value;
    
    
    /**
     * Creates a new I8ArrBox holding a zero-length byte array.
     */
    public I8ArrBox() {
        this.value = defaultValue();
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public I8ArrBox(byte[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public byte[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(byte[] value) { this.value = Objects.requireNonNull(value);  }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        value = new byte[in.readInt()];
        in.readFully(value);
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(value.length);
        out.write(value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getI8Arr(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getI8Arr();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " byte]" : " bytes]");
    }
    
    
    
    
    @Override
    public DataType type() {
        return DataType.I8ARR;
    }
    
    @Override
    public boolean canConvertToType(DataType type) {
        switch(type) {
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
            case I8ARR:
            {
                return new I8ArrBox(value.clone());
            }
            case I32ARR:
            {
                int[] data = new int[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = value[i];
                return new I32ArrBox(data);
            }
            case I64ARR:
            {
                long[] data = new long[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = value[i];
                return new I64ArrBox(data);
            }
            case F32ARR:
            {
                float[] data = new float[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = value[i];
                return new F32ArrBox(data);
            }
            case F64ARR:
            {
                double[] data = new double[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = value[i];
                return new F64ArrBox(data);
            }
            case STRING:
                return new StringBox(Arrays.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: I8Arr --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return false; }
    //@Override public boolean isLong()    { return false; }
    //@Override public boolean isDouble()  { return false; }
    //@Override public boolean isString()  { return true;  }
    
    //@Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert byte array to boolean... for now"); }
    //@Override public long    getAsLong()    { throw Checks.ISE("Can't convert byte array to long... for now");    }
    //@Override public double  getAsDouble()  { throw Checks.ISE("Can't convert byte array to double... for now");  }
    //@Override public String  getAsString()  { return Arrays.toString(value);                                      }
    
    @Override
    public I8ArrBox duplicate() {
        return new I8ArrBox(value.clone());
    }
    
}
