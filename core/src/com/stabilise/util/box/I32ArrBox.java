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
 * Boxes an array of ints.
 */
public class I32ArrBox implements IData {
    
    /** Returns a zero-length array */
    public static int[] defaultValue() { return new int[0]; }
    
    
    
    private int[] value;
    
    
    /**
     * Creates a new I32ArrBox holding a zero-length int array.
     */
    public I32ArrBox() {
        this.value = defaultValue();
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public I32ArrBox(int[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public int[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(int[] value) { this.value = Objects.requireNonNull(value); }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int len = in.readInt();
        value = new int[len];
        for(int i = 0; i < len; i++)
            value[i] = in.readInt();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(value.length);
        for(int i : value)
            out.writeInt(i);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getI32Arr(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getI32Arr();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " int]" : " ints]");
    }
    
    
    
    
    @Override
    public DataType type() {
        return DataType.I32ARR;
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
                return new I32ArrBox(value.clone());
            }
            case I64ARR:
            {
                long[] data = new long[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = (long) value[i];
                return new I64ArrBox(data);
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
                throw new RuntimeException("Illegal conversion: I32Arr --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return false; }
    //@Override public boolean isLong()    { return false; }
    //@Override public boolean isDouble()  { return false; }
    //@Override public boolean isString()  { return true;  }
    
    //@Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert int array to boolean... yet"); }
    //@Override public long    getAsLong()    { throw Checks.ISE("Can't convert int array to long... yet");    }
    //@Override public double  getAsDouble()  { throw Checks.ISE("Can't convert int array to double... yet");  }
    //@Override public String  getAsString()  { return Arrays.toString(value);                                 }
    
    @Override
    public I32ArrBox duplicate() {
        return new I32ArrBox(value.clone());
    }
    
}
