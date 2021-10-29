package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

import com.google.common.collect.Iterators;
import com.stabilise.util.Checks;
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
 * <li>{@link #read(String, DataCompound)}
 * <li>{@link #read(DataList)}
 * <li>{@link #childCompound()}
 * <li>{@link #childList()}
 * </ul>
 * 
 * <p>Furthermore, this class wraps child compounds and lists (as returned by
 * {@link #getCompound(int)}) in their immutable variants.
 */
public final class ImmutableList implements DataList {
    
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
    public DataList setStrict(boolean strict) {
        throw new UnsupportedOperationException("Can't set strict mode for an ImmutableList!");
    }
    
    @Override
    public boolean isStrict() {
        return list.isStrict();
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
    public IData getNext() {
        throw new UnsupportedOperationException("Cannot directly get data from an ImmutableList!");
    }
    
    @Override
    public IData getData(int index) {
        return list.getData(index).duplicate();
    }
    
    @Override
    public void addData(IData data) {
        throw new UnsupportedOperationException("Can't add to an ImmutableCompound!");
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
    
    @Override public DataCompound getCompound(int i) { return ImmutableCompound.wrap((DataCompound)list.getData(i)); }
    @Override public DataList getList(int i)         { return wrap((DataList)list.getData(i));     }
    @Override public boolean  getBool(int i)         { return list.getBool(i); }
    @Override public byte     getI8(int i)           { return list.getI8(i); }
    @Override public short    getI16(int i)          { return list.getI16(i); }
    @Override public int      getI32(int i)          { return list.getI32(i); }
    @Override public long     getI64(int i)          { return list.getI64(i); }
    @Override public float    getF32(int i)          { return list.getF32(i); }
    @Override public double   getF64(int i)          { return list.getF64(i); }
    @Override public byte[]   getI8Arr(int i)        { return list.getI8Arr(i).clone(); }
    @Override public int[]    getI32Arr(int i)       { return list.getI32Arr(i).clone(); }
    @Override public long[]   getI64Arr(int i)       { return list.getI64Arr(i).clone(); }
    @Override public float[]  getF32Arr(int i)       { return list.getF32Arr(i).clone(); }
    @Override public double[] getF64Arr(int i)       { return list.getF64Arr(i).clone(); }
    @Override public String   getString(int i)       { return list.getString(i); }
    
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
    
    @Override
    public Iterator<IData> iterator() {
        // Duplicate the IData objects in the iterator so that
        return Iterators.transform(
                Iterators.unmodifiableIterator(list.iterator()),
                data -> data.duplicate()
        );
    }
    
    @Override
    public void forEach(Consumer<? super IData> action) {
        // Override to use the faster forEach ArrayList via this.list.forEach
        // rather than the default forEach of Iterator.
        list.forEach(data -> action.accept(data.duplicate()));
    }
}
