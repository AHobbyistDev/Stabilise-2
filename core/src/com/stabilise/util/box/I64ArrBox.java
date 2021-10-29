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
 * Boxes an array of longs.
 */
public class I64ArrBox implements IData {
    
    /** Returns a zero-length array */
    public static long[] defaultValue() { return new long[0]; }
    
    
    
    private long[] value;
    
    
    /**
     * Creates a new LongArrayBox holding a zero-length long array.
     */
    public I64ArrBox() {
        this.value = defaultValue();
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public I64ArrBox(long[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public long[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(long[] value) { this.value = Objects.requireNonNull(value); }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int len = in.readInt();
        value = new long[len];
        for(int i = 0; i < len; i++)
            value[i] = in.readLong();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(value.length);
        for(long i : value)
            out.writeLong(i);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getI64Arr(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getI64Arr();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " long]" : " longs]");
    }
    
    
    
    
    @Override
    public DataType type() {
        return DataType.I64ARR;
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
                byte[] data = new byte[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = (byte) value[i];
                return new I8ArrBox(data);
            }
            case I32ARR:
            {
                int[] data = new int[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = (int) value[i];
                return new I32ArrBox(data);
            }
            case I64ARR:
            {
                return new I64ArrBox(value.clone());
            }
            case F32ARR:
            {
                float[] data = new float[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = (float) value[i];
                return new F32ArrBox(data);
            }
            case F64ARR:
            {
                double[] data = new double[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = (double) value[i];
                return new F64ArrBox(data);
            }
            case STRING:
                return new StringBox(Arrays.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: I64Arr --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return false; }
    //@Override public boolean isLong()    { return false; }
    //@Override public boolean isDouble()  { return false; }
    //@Override public boolean isString()  { return true;  }
    
    //@Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert long array to boolean... yet"); }
    //@Override public long    getAsLong()    { throw Checks.ISE("Can't convert long array to long... yet");    }
    //@Override public double  getAsDouble()  { throw Checks.ISE("Can't convert long array to double... yet");  }
    //@Override public String  getAsString()  { return Arrays.toString(value);                                  }
    
    @Override
    public I64ArrBox duplicate() {
        return new I64ArrBox(value.clone());
    }
    
}
