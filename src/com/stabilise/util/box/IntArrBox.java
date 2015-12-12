package com.stabilise.util.box;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;

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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getIntArr(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getIntArr();
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " int]" : " ints]");
    }
    
}
