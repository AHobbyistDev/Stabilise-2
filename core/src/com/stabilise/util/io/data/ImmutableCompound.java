package com.stabilise.util.io.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.google.common.collect.Iterators;
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
 * <li>{@link #childCompound(String)}
 * <li>{@link #childList(String)}
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
    
    /**
     * Throws UnsupportedOperationException.
     */
    @Override
    public void read(String name, DataCompound o) {
        throw new UnsupportedOperationException("Cannot read to an immutable compound");
    }
    
    @Override
    public void write(String name, DataCompound o) {
        compound.write(name, o);
    }
    
    /**
     * Throws UnsupportedOperationException.
     */
    @Override
    public void read(DataList l) {
        throw new UnsupportedOperationException("Cannot read to an immutable compound");
    }
    
    @Override
    public void write(DataList l) {
        compound.write(l);
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
    public void putData(String name, IData data) {
        throw new UnsupportedOperationException("Can't put into an ImmutableCompound!");
    }
    
    @Override
    public IData getData(String name) {
        throw new UnsupportedOperationException("Can't directly get data for an ImmutableCompound!");
    }
    
    @Override
    public boolean contains(String name) {
        return compound.contains(name);
    }
    
    @Override public boolean containsCompound(String name) { return compound.containsCompound(name); }
    @Override public boolean containsList    (String name) { return compound.containsList(name);     }
    @Override public boolean containsBool    (String name) { return compound.containsBool(name);     }
    @Override public boolean containsI8      (String name) { return compound.containsI8(name);       }
    @Override public boolean containsI16     (String name) { return compound.containsI16(name);      }
    @Override public boolean containsI32     (String name) { return compound.containsI32(name);      }
    @Override public boolean containsI64     (String name) { return compound.containsI64(name);      }
    @Override public boolean containsF32     (String name) { return compound.containsF32(name);      }
    @Override public boolean containsF64     (String name) { return compound.containsF64(name);      }
    @Override public boolean containsI8Arr   (String name) { return compound.containsI8Arr(name);    }
    @Override public boolean containsI32Arr  (String name) { return compound.containsI32Arr(name);   }
    @Override public boolean containsI64Arr  (String name) { return compound.containsI64Arr(name);   }
    @Override public boolean containsF32Arr  (String name) { return compound.containsF32Arr(name);   }
    @Override public boolean containsF64Arr  (String name) { return compound.containsF64Arr(name);   }
    @Override public boolean containsString  (String name) { return compound.containsString(name);   }
    
    @Override public void put(String name, DataCompound data) { Checks.unsupported(); }
    @Override public void put(String name, DataList data)     { Checks.unsupported(); }
    @Override public void put(String name, boolean data)      { Checks.unsupported(); }
    @Override public void put(String name, byte data)         { Checks.unsupported(); }
    @Override public void put(String name, short data)        { Checks.unsupported(); }
    @Override public void put(String name, int data)          { Checks.unsupported(); }
    @Override public void put(String name, long data)         { Checks.unsupported(); }
    @Override public void put(String name, float data)        { Checks.unsupported(); }
    @Override public void put(String name, double data)       { Checks.unsupported(); }
    @Override public void put(String name, byte[] data)       { Checks.unsupported(); }
    @Override public void put(String name, int[] data)        { Checks.unsupported(); }
    @Override public void put(String name, long[] data)       { Checks.unsupported(); }
    @Override public void put(String name, float[] data)      { Checks.unsupported(); }
    @Override public void put(String name, double[] data)     { Checks.unsupported(); }
    @Override public void put(String name, String data)       { Checks.unsupported(); }
    
    @Override public DataCompound getCompound(String name) { return wrap(compound.getCompound(name)); }
    @Override public DataList getList(String name)  { return ImmutableList.wrap(compound.getList(name)); }
    @Override public boolean  getBool(String name)   { return compound.getBool(name);   }
    @Override public byte     getI8(String name)     { return compound.getI8(name);     }
    @Override public short    getI16(String name)    { return compound.getI16(name);    }
    @Override public int      getI32(String name)    { return compound.getI32(name);    }
    @Override public long     getI64(String name)    { return compound.getI64(name);    }
    @Override public float    getF32(String name)    { return compound.getF32(name);    }
    @Override public double   getF64(String name)    { return compound.getF64(name);    }
    @Override public byte[]   getI8Arr(String name)  { return compound.getI8Arr(name);  }
    @Override public int[]    getI32Arr(String name) { return compound.getI32Arr(name); }
    @Override public long[]   getI64Arr(String name) { return compound.getI64Arr(name); }
    @Override public float[]  getF32Arr(String name) { return compound.getF32Arr(name); }
    @Override public double[] getF64Arr(String name) { return compound.getF64Arr(name); }
    @Override public String   getString(String name) { return compound.getString(name); }
    
    @Override public Option<DataCompound> optCompound(String name) {
        return compound.optCompound(name).map(ImmutableCompound::wrap);
    }
    @Override public Option<DataList> optList(String name)  {
        return compound.optList(name).map(ImmutableList::wrap);
    }
    @Override public Option<Boolean>  optBool(String name)   { return compound.optBool(name);   }
    @Override public Option<Byte>     optI8(String name)     { return compound.optI8(name);     }
    @Override public Option<Short>    optI16(String name)    { return compound.optI16(name);    }
    @Override public Option<Integer>  optI32(String name)    { return compound.optI32(name);    }
    @Override public Option<Long>     optI64(String name)    { return compound.optI64(name);    }
    @Override public Option<Float>    optF32(String name)    { return compound.optF32(name);    }
    @Override public Option<Double>   optF64(String name)    { return compound.optF64(name);    }
    @Override public Option<byte[]>   optI8Arr(String name)  { return compound.optI8Arr(name);  }
    @Override public Option<int[]>    optI32Arr(String name) { return compound.optI32Arr(name); }
    @Override public Option<long[]>   optI64Arr(String name) { return compound.optI64Arr(name); }
    @Override public Option<float[]>  optF32Arr(String name) { return compound.optF32Arr(name); }
    @Override public Option<double[]> optF64Arr(String name) { return compound.optF64Arr(name); }
    @Override public Option<String>   optString(String name) { return compound.optString(name); }
    
    @Override
    public DataCompound childCompound(String name) {
        throw Checks.unsupported();
    }
    
    @Override
    public DataList childList(String name) {
        throw Checks.unsupported();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Can't clear an ImmutableCompound!");
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p>Note: does not wrap the returned compound in an ImmutableCompound.
     */
    @Override
    public DataCompound duplicate(Format format) {
        return compound.duplicate(format);
    }
    
    @Override
    public DataCompound setStrict(boolean strict) {
        throw new UnsupportedOperationException("Can't set strict for an ImmutableCompound!");
    }
    
    @Override
    public boolean isStrict() {
        return compound.isStrict();
    }
    
    @Override
    public void forEach(BiConsumer<String, IData> action) {
        compound.forEach((name,data) -> action.accept(name, data.duplicate()));
    }
    
    @Override
    public Iterator<Map.Entry<String, IData>> iterator() {
        // Gotta do two stages of wrapping, since a) Iterators have a remove()
        // method, and b) Map.Entry has setValue()! Additionally, we duplicate
        // the IData values, so that when exposed they don't pose a mutability
        // risk.
        return Iterators.transform(
                Iterators.unmodifiableIterator(compound.iterator()),
                entry -> new Map.Entry<String, IData>() {
                    @Override
                    public String getKey() {
                        return entry.getKey();
                    }
    
                    @Override
                    public IData getValue() {
                        return entry.getValue().duplicate();
                    }
    
                    @Override
                    public IData setValue(IData value) {
                        throw new UnsupportedOperationException("Can't set value for an immutable compound!");
                    }
                }
        );
    }
}
