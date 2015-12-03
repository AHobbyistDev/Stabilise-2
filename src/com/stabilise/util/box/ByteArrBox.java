package com.stabilise.util.box;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.beta.DataObject;

public class ByteArrBox implements IBox {
    
    private byte[] value;
    
    
    /**
     * Creates a new ByteArrayBox holding a zero-length byte array.
     */
    public ByteArrBox() {
        this.value = new byte[0];
    }
    
    /**
     * @throws NullPointerException if {@code value} is {@code null}.
     */
    public ByteArrBox(byte[] value) {
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
    public void write(String name, DataObject o) throws IOException {
        o.write(name, value);
    }

    @Override
    public void read(String name, DataObject o) throws IOException {
        value = o.readByteArr(name);
    }
    
}
