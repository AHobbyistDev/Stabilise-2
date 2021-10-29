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
 * Boxes an array of floats.
 */
public class F32ArrBox implements IData {
    
    /** Returns a zero-length array */
    public static float[] defaultValue() { return new float[0]; }
    
    
    
    private float[] value;
    
    
    /**
     * Creates a new F32ArrBox holding a zero-length float array.
     */
    public F32ArrBox() {
        this.value = defaultValue();
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public F32ArrBox(float[] value) {
        this.value = Objects.requireNonNull(value);
    }
    
    public float[] get()           { return value; }
    /** @throws NullPointerException if {@code value} is {@code null}. */
    public void set(float[] value) { this.value = Objects.requireNonNull(value); }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        int len = in.readInt();
        value = new float[len];
        for(int i = 0; i < len; i++)
            value[i] = in.readFloat();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(value.length);
        for(float i : value)
            out.writeFloat(i);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getF32Arr(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getF32Arr();
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " float]" : " floats]");
    }
    
    
    
    @Override
    public DataType type() {
        return DataType.F32ARR;
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
                long[] data = new long[value.length];
                for(int i = 0; i < value.length; i++)
                    data[i] = (long) value[i];
                return new I64ArrBox(data);
            }
            case F32ARR:
            {
                return new F32ArrBox(value.clone());
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
                throw new RuntimeException("Illegal conversion: F32Arr --> " + type);
        }
    }
    
    @Override
    public F32ArrBox duplicate() {
        return new F32ArrBox(value.clone());
    }
    
}
