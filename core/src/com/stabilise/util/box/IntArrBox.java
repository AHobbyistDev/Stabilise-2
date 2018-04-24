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

public class IntArrBox implements ITag {
    
    private int[] value;
    
    
    /**
     * Creates a new IntArrayBox holding a zero-length int array.
     */
    public IntArrBox() {
        this.value = new int[0];
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public IntArrBox(int[] value) {
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
        value = o.getIntArr(name);
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getIntArr();
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
    public ITag convertToSameType(ITag other) {
        if(isSameType(other))
            return other;
        throw Checks.ISE("Can't convert " + other.getClass().getSimpleName() + "to byte array");
    }
    
    @Override public boolean isBoolean() { return false; }
    @Override public boolean isLong()    { return false; }
    @Override public boolean isDouble()  { return false; }
    @Override public boolean isString()  { return true;  }
    
    @Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert int array to boolean... yet"); }
    @Override public long    getAsLong()    { throw Checks.ISE("Can't convert int array to long... yet");    }
    @Override public double  getAsDouble()  { throw Checks.ISE("Can't convert int array to double... yet");  }
    @Override public String  getAsString()  { return Arrays.toString(value);                                 }
    
}
