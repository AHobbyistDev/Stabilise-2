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
    public void write(String name, DataCompound o) {
        o.put(name, value);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        value = o.getByteArr(name);
    }
    
    @Override
    public void write(DataList l) {
        l.add(value);
    }
    
    @Override
    public void read(DataList l) {
        value = l.getByteArr();
    }
    
    @Override
    public String toString() {
        return "[" + value.length + (value.length == 1 ? " byte]" : " bytes]");
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
    
    @Override public boolean getAsBoolean() { throw Checks.ISE("Can't convert byte array to boolean... for now"); }
    @Override public long    getAsLong()    { throw Checks.ISE("Can't convert byte array to long... for now");    }
    @Override public double  getAsDouble()  { throw Checks.ISE("Can't convert byte array to double... for now");  }
    @Override public String  getAsString()  { return Arrays.toString(value);                                      }
    
}
