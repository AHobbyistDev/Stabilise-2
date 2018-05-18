package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.Objects;

import com.stabilise.util.Checks;
import com.stabilise.util.box.*;
import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * An ImmutableList wraps a DataList to prevent modification.
 * 
 * <p>All {@code add()} and {@code get()} (without the {@code index} parameter)
 * methods throw {@code UnsupportedOperationException}. Additionally, the
 * following methods also throw a UOE:
 * 
 * <ul>
 * <li>{@link #readData(DataInStream)}
 * <li>{@link #createCompound(String)}
 * <li>{@link #createList(String)}
 * <li>{@link #setWriteMode()}
 * <li>{@link #getTag(int)}
 * </ul>
 * 
 * <p>Furthermore, this class wraps child compounds and lists (as returned by
 * {@link #getCompound(int)} and {@link #optCompound(int)}) in their immutable
 * variants.
 */
public class ImmutableList implements DataList {
    
    /**
     * Wraps {@code l} in an {@code ImmutableList}. Returns {@code l} if it
     * is already an ImmutableList;
     * 
     * @throws NullPointerException if {@code l} is {@code null}.
     */
    public static ImmutableList wrap(DataList l) {
        if(l instanceof ImmutableList)
            return (ImmutableList) l;
        return new ImmutableList(l);
    }
    
    
    private final DataList list;
    
    
    /**
     * Creates a new ImmutableList wrapping the given list.
     * 
     * @throws NullPointerException if {@code list} is {@code null}.
     */
    public ImmutableList(DataList list) {
        this.list = Objects.requireNonNull(list);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        Checks.unsupported();
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        list.writeData(out);
    }
    
    @Override
    public void read(String name, DataCompound o) {
        Checks.unsupported();
    }
    
    @Override
    public void write(String name, DataCompound o) {
        list.write(name, o);
    }
    
    @Override
    public void read(DataList l) {
        Checks.unsupported();
    }
    
    @Override
    public void write(DataList l) {
        list.write(l);
    }
    
    @Override
    public Format format() {
        return list.format();
    }
    
    @Override
    public ImmutableList convert(Format format) {
        DataList l = list.convert(format);
        if(l == list)
            return this;
        return new ImmutableList(l);
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public boolean hasNext() {
        throw Checks.unsupported();
    }
    
    @Override
    public DataCompound childCompound() {
        throw Checks.unsupported();
    }
    
    @Override
    public DataList childList() {
        throw Checks.unsupported();
    }
    
    @Override public void add(DataCompound data) { Checks.unsupported(); }
    @Override public void add(DataList data)     { Checks.unsupported(); }
    @Override public void add(boolean data)      { Checks.unsupported(); }
    @Override public void add(byte data)         { Checks.unsupported(); }
    @Override public void add(short data)        { Checks.unsupported(); }
    @Override public void add(int data)          { Checks.unsupported(); }
    @Override public void add(long data)         { Checks.unsupported(); }
    @Override public void add(float data)        { Checks.unsupported(); }
    @Override public void add(double data)       { Checks.unsupported(); }
    @Override public void add(byte[] data)       { Checks.unsupported(); }
    @Override public void add(int[] data)        { Checks.unsupported(); }
    @Override public void add(long[] data)       { Checks.unsupported(); }
    @Override public void add(float[] data)      { Checks.unsupported(); }
    @Override public void add(double[] data)     { Checks.unsupported(); }
    @Override public void add(String data)       { Checks.unsupported(); }
    
    @Override public ITag    getTag(int i)           { throw Checks.unsupported("Just no");  }
    @Override public DataCompound getCompound(int i) { return ImmutableCompound.wrap((DataCompound)getTag(i)); }
    @Override public DataList getList(int i)         { return wrap((DataList)getTag(i));     }
    @Override public boolean  getBool(int i)         { return ((BoolBox)   getTag(i)).get(); }
    @Override public byte     getI8(int i)           { return ((I8Box)     getTag(i)).get(); }
    @Override public short    getI16(int i)          { return ((I16Box)    getTag(i)).get(); }
    @Override public int      getI32(int i)          { return ((I32Box)    getTag(i)).get(); }
    @Override public long     getI64(int i)          { return ((I64Box)    getTag(i)).get(); }
    @Override public float    getF32(int i)          { return ((F32Box)    getTag(i)).get(); }
    @Override public double   getF64(int i)          { return ((F64Box)    getTag(i)).get(); }
    @Override public byte[]   getI8Arr(int i)        { return ((I8ArrBox)  getTag(i)).get(); }
    @Override public int[]    getI32Arr(int i)       { return ((I32ArrBox) getTag(i)).get(); }
    @Override public long[]   getI64Arr(int i)       { return ((I64ArrBox) getTag(i)).get(); }
    @Override public float[]  getF32Arr(int i)       { return ((F32ArrBox) getTag(i)).get(); }
    @Override public double[] getF64Arr(int i)       { return ((F64ArrBox) getTag(i)).get(); }
    @Override public String   getString(int i)       { return ((StringBox) getTag(i)).get(); }
    
    @Override public DataCompound getCompound() { throw Checks.unsupported("Mutable state!"); }
    @Override public DataList getList()         { throw Checks.unsupported("Mutable state!"); }
    @Override public boolean  getBool()         { throw Checks.unsupported("Mutable state!"); }
    @Override public byte     getI8()           { throw Checks.unsupported("Mutable state!"); }
    @Override public short    getI16()          { throw Checks.unsupported("Mutable state!"); }
    @Override public int      getI32()          { throw Checks.unsupported("Mutable state!"); }
    @Override public long     getI64()          { throw Checks.unsupported("Mutable state!"); }
    @Override public float    getF32()          { throw Checks.unsupported("Mutable state!"); }
    @Override public double   getF64()          { throw Checks.unsupported("Mutable state!"); }
    @Override public byte[]   getI8Arr()        { throw Checks.unsupported("Mutable state!"); }
    @Override public int[]    getI32Arr()       { throw Checks.unsupported("Mutable state!"); }
    @Override public long[]   getI64Arr()       { throw Checks.unsupported("Mutable state!"); }
    @Override public float[]  getF32Arr()       { throw Checks.unsupported("Mutable state!"); }
    @Override public double[] getF64Arr()       { throw Checks.unsupported("Mutable state!"); }
    @Override public String   getString()       { throw Checks.unsupported("Mutable state!"); }
    
}
