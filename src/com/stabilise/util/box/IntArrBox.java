package com.stabilise.util.box;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

public class IntArrBox implements IBox {
    
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
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readIntArr(name);
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " int]" : " ints]");
    }
    
}
