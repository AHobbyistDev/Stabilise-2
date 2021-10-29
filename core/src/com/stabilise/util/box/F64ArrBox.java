package com.stabilise.util.box;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.IData;


/**
 * Boxes an array of doubles.
 */
public class F64ArrBox implements IData {
    
    /** Returns a zero-length array */
    public static double[] defaultValue() { return new double[0]; }
    
    
    
    private double[] value;
    
    
    /**
     * Creates a new F64ArrBox holding a zero-length double array.
     */
    public F64ArrBox() {
        this.value = defaultValue();
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public F64ArrBox(double[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public double[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(double[] value) { this.value = Objects.requireNonNull(value); }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int len = in.readInt();
        value = new double[len];
        for(int i = 0; i < len; i++)
            value[i] = in.readDouble();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(value.length);
        for(double i : value)
            out.writeDouble(i);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getF64Arr(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getF64Arr();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " double]" : " doubles]");
    }
    
    
    
    
    @Override
    public DataType type() {
        return DataType.F64ARR;
    }
    
    @Override
    public boolean canConvertToType(DataType type) {
        switch(type) {
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
                    data[i] = value[i];
                return new F64ArrBox(data);
            }
            case STRING:
                return new StringBox(Arrays.toString(value));
            default:
                throw new RuntimeException("Illegal conversion: F64Arr --> " + type);
        }
    }
    
    //@Override public boolean isBoolean() { return false; }
    //@Override public boolean isLong()    { return false; }
    //@Override public boolean isDouble()  { return false; }
    //@Override public boolean isString()  { return true;  }
    
    //@Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert double array to boolean... yet"); }
    //@Override public long    getAsLong()    { throw Checks.ISE("Can't convert double array to long... yet");    }
    //@Override public double  getAsDouble()  { throw Checks.ISE("Can't convert double array to double... yet");  }
    //@Override public String  getAsString()  { return Arrays.toString(value);                                   }
    
    @Override
    public F64ArrBox duplicate() {
        return new F64ArrBox(value.clone());
    }
    
}
