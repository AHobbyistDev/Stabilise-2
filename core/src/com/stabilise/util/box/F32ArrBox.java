package com.stabilise.util.box;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import com.stabilise.util.Checks;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;


/**
 * Boxes an array of floats.
 */
public class F32ArrBox implements ITag {
    
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
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        throw Checks.ISE("Can't convert " + other.getClass().getSimpleName() + "to byte array");
    }
    
    @Override public boolean isBoolean() { return false; }
    @Override public boolean isLong()    { return false; }
    @Override public boolean isDouble()  { return false; }
    @Override public boolean isString()  { return true;  }
    
    @Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert float array to boolean... yet"); }
    @Override public long    getAsLong()    { throw Checks.ISE("Can't convert float array to long... yet");    }
    @Override public double  getAsDouble()  { throw Checks.ISE("Can't convert float array to double... yet");  }
    @Override public String  getAsString()  { return Arrays.toString(value);                                   }
    
}
