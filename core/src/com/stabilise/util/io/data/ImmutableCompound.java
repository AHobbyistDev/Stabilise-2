package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.Objects;

import javaslang.control.Option;

import com.stabilise.util.Checks;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;


/**
 * An ImmutableCompound wraps a DataCompound to prevent modification.
 * 
 * <p>All {@code put()} methods throw {@code UnsupportedOperationException}.
 * Additionally, the following methods also throw a UOE:
 * 
 * <ul>
 * <li>{@link #readData(DataInStream)}
 * <li>{@link #createCompound(String)}
 * <li>{@link #createList(String)}
 * <li>{@link #setWriteMode()}
 * </ul>
 * 
 * <p>Furthermore, this class wraps child compounds and lists (as returned
 * by {@link #getCompound(String)} and {@link #optCompound(String)}, etc.)
 * in their immutable variants.
 */
public class ImmutableCompound implements DataCompound {
    
    /**
     * Wraps {@code c} in an {@code ImmutableCompound}. Returns {@code c} if it
     * is already an ImmutableCompound;
     * 
     * @throws NullPointerException if {@code c} is {@code null}.
     */
    public static ImmutableCompound wrap(DataCompound c) {
        if(c instanceof ImmutableCompound)
            return (ImmutableCompound) c;
        return new ImmutableCompound(c);
    }
    
    
    private final DataCompound compound;
    
    
    /**
     * Creates a new ImmutableCompound wrapping the given compound.
     * 
     * @throws NullPointerException if {@code compound} is {@code null}.
     */
    public ImmutableCompound(DataCompound compound) {
        this.compound = Objects.requireNonNull(compound);
    }
    
    /**
     * Throws UnsupportedOperationException.
     */
    @Override
    public void readData(DataInStream in) throws IOException {
        throw new UnsupportedOperationException("Cannot read to an immutable compound");
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        compound.writeData(out);
    }
    
    @Override
    public Format format() {
        return compound.format();
    }
    
    @Override
    public ImmutableCompound convert(Format format) {
        DataCompound c = compound.convert(format);
        if(c == compound)
            return this;
        return new ImmutableCompound(compound.convert(format));
    }
    
    @Override
    public boolean contains(String name) {
        return compound.contains(name);
    }
    
    @Override
    public DataCompound createCompound(String name) {
        throw Checks.unsupported();
    }
    
    @Override
    public DataList createList(String name) {
        throw Checks.unsupported();
    }
    
    @Override public void put(String name, DataCompound data) { Checks.unsupported(); }
    @Override public void put(String name, DataList data)     { Checks.unsupported(); }
    @Override public void put(String name, boolean data)      { Checks.unsupported(); }
    @Override public void put(String name, byte data)         { Checks.unsupported(); }
    @Override public void put(String name, char data)         { Checks.unsupported(); }
    @Override public void put(String name, double data)       { Checks.unsupported(); }
    @Override public void put(String name, float data)        { Checks.unsupported(); }
    @Override public void put(String name, int data)          { Checks.unsupported(); }
    @Override public void put(String name, long data)         { Checks.unsupported(); }
    @Override public void put(String name, short data)        { Checks.unsupported(); }
    @Override public void put(String name, String data)       { Checks.unsupported(); }
    @Override public void put(String name, byte[] data)       { Checks.unsupported(); }
    @Override public void put(String name, int[] data)        { Checks.unsupported(); }
    
    @Override public DataCompound getCompound(String name) { return wrap(compound.getCompound(name)); }
    @Override public DataList getList(String name) { return ImmutableList.wrap(compound.getList(name)); }
    @Override public boolean getBool(String name)   { return compound.getBool(name);    }
    @Override public byte getByte(String name)      { return compound.getByte(name);    }
    @Override public char getChar(String name)      { return compound.getChar(name);    }
    @Override public double getDouble(String name)  { return compound.getDouble(name);  }
    @Override public float getFloat(String name)    { return compound.getFloat(name);   }
    @Override public int getInt(String name)        { return compound.getInt(name);     }
    @Override public long getLong(String name)      { return compound.getLong(name);    }
    @Override public short getShort(String name)    { return compound.getShort(name);   }
    @Override public String getString(String name)  { return compound.getString(name);  }
    @Override public byte[] getByteArr(String name) { return compound.getByteArr(name); }
    @Override public int[] getIntArr(String name)   { return compound.getIntArr(name);  }
    
    @Override public Option<DataCompound> optCompound(String name) {
        return compound.optCompound(name).map(ImmutableCompound::wrap);
    }
    @Override public Option<DataList> optList(String name)  {
        return compound.optList(name).map(ImmutableList::wrap);
    }
    @Override public Option<Boolean> optBool(String name)   { return compound.optBool(name);    }
    @Override public Option<Byte> optByte(String name)      { return compound.optByte(name);    }
    @Override public Option<Character> optChar(String name) { return compound.optChar(name);    }
    @Override public Option<Double> optDouble(String name)  { return compound.optDouble(name);  }
    @Override public Option<Float> optFloat(String name)    { return compound.optFloat(name);   }
    @Override public Option<Integer> optInt(String name)    { return compound.optInt(name);     }
    @Override public Option<Long> optLong(String name)      { return compound.optLong(name);    }
    @Override public Option<Short> optShort(String name)    { return compound.optShort(name);   }
    @Override public Option<String> optString(String name)  { return compound.optString(name);  }
    @Override public Option<byte[]> optByteArr(String name) { return compound.optByteArr(name); }
    @Override public Option<int[]> optIntArr(String name)   { return compound.optIntArr(name);  }
    
    @Override
    public void setReadMode() {
        compound.setReadMode();
    }
    
    @Override
    public void setWriteMode() {
        Checks.unsupported();
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Note: does not wrap the returned compound in an ImmutableCompound.
     */
    @Override
    public DataCompound copy(Format format) {
        return compound.copy(format);
    }
    
}
