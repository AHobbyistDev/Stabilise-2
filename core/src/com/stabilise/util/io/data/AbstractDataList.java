package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

import com.stabilise.util.box.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base implementation for a DataCompound backed by a regular java List.
 * External code should never refer to this class directly.
 */
public abstract class AbstractDataList implements DataList {
    
    protected final List<IData> data;
    protected int index = 0;
    
    protected boolean strict = true;
    
    
    public AbstractDataList() {
        data = new ArrayList<>();
    }
    
    public AbstractDataList(int initialCapacity) {
        data = new ArrayList<>(initialCapacity);
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public boolean hasNext() {
        return index < data.size();
    }
    
    @Override
    public IData getNext() {
        return data.get(index++);
    }
    
    @Override
    public IData getData(int index) {
        return data.get(index);
    }
    
    @Override
    public void addData(IData d) {
        Objects.requireNonNull(d);
        checkType(d);
        if(d instanceof DataCompound)
            data.add(d.asCompound().convert(format()));
        else if(d instanceof DataList)
            data.add(d.asList().convert(format()));
        else
            data.add(d);
        setDirty();
    }
    
    /**
     * Like {@link #addData(IData)}, but without checks.
     */
    protected void addData2(IData d) {
        checkType(d);
        data.add(d);
        setDirty();
    }
    
    /**
     * Called when data is added to this list.
     * Subclasses may override this to enforce only a single type. Default
     * implementation does nothing.
     */
    protected void checkType(IData d) {
        // Do nothing, but an implementation is here for copying & pasting
        /*
        if(size() > 0) {
            if(d.type() != listType) {
                throw new IllegalArgumentException("Invalid type! (Tried adding "
                        + d.type() + " to a list containing " + listType + ")");
            }
        } else {
            listType = d.type();
        }
        */
    }
    
    // WOW, TRULY I AM A BUG FAN OF REPETITION
    
    @Override public void add(DataCompound data) { addData2(data.convert(format())); }
    @Override public void add(DataList data)     { addData2(data.convert(format())); }
    @Override public void add(boolean data)      { addData2(box(data)); }
    @Override public void add(byte data)         { addData2(box(data)); }
    @Override public void add(short data)        { addData2(box(data)); }
    @Override public void add(int data)          { addData2(box(data)); }
    @Override public void add(long data)         { addData2(box(data)); }
    @Override public void add(float data)        { addData2(box(data)); }
    @Override public void add(double data)       { addData2(box(data)); }
    @Override public void add(byte[] data)       { addData2(box(Objects.requireNonNull(data))); }
    @Override public void add(int[] data)        { addData2(box(Objects.requireNonNull(data))); }
    @Override public void add(long[] data)       { addData2(box(Objects.requireNonNull(data))); }
    @Override public void add(float[] data)      { addData2(box(Objects.requireNonNull(data))); }
    @Override public void add(double[] data)     { addData2(box(Objects.requireNonNull(data))); }
    @Override public void add(String data)       { addData2(box(Objects.requireNonNull(data))); }
    
    @Override
    public DataCompound getCompound(int index) {
        IData d = data.get(index);
        if(d instanceof DataCompound)
            return (DataCompound) d;
        else if(!strict && d != null && d.canConvertToType(DataType.COMPOUND))
            return (DataCompound) d.convertToType(DataType.COMPOUND);
        else
            return format().newCompound();
    }
    
    @Override
    public DataList getList(int index) {
        IData d = data.get(index);
        if(d instanceof DataList)
            return (DataList) d;
        else if(!strict && d != null && d.canConvertToType(DataType.LIST))
            return (DataList) d.convertToType(DataType.LIST);
        else
            return format().newList();
    }
    
    // A lot of these methods have unnecessary object creation in the relaxed
    // branches by means of the convertToType().get() invocations, but ah well,
    // maybe if we're lucky the JVM will learn to sidestep that.
    
    @Override
    public boolean getBool(int index) {
        IData d = data.get(index);
        if(d instanceof BoolBox)
            return ((BoolBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.BOOL))
            return ((BoolBox) d.convertToType(DataType.BOOL)).get();
        else
            return BoolBox.defaultValue();
    }
    
    @Override
    public byte getI8(int index) {
        IData d = data.get(index);
        if(d instanceof I8Box)
            return ((I8Box) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.I8))
            return ((I8Box) d.convertToType(DataType.I8)).get();
        else
            return I8Box.defaultValue();
    }
    
    @Override
    public short getI16(int index) {
        IData d = data.get(index);
        if(d instanceof I16Box)
            return ((I16Box) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.I16))
            return ((I16Box) d.convertToType(DataType.I16)).get();
        else
            return I16Box.defaultValue();
    }
    
    @Override
    public int getI32(int index) {
        IData d = data.get(index);
        if(d instanceof I32Box)
            return ((I32Box) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.I32))
            return ((I32Box) d.convertToType(DataType.I32)).get();
        else
            return I32Box.defaultValue();
    }
    
    @Override
    public long getI64(int index) {
        IData d = data.get(index);
        if(d instanceof I64Box)
            return ((I64Box) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.I64))
            return ((I64Box) d.convertToType(DataType.I64)).get();
        else
            return I64Box.defaultValue();
    }
    
    @Override
    public float getF32(int index) {
        IData d = data.get(index);
        if(d instanceof F32Box)
            return ((F32Box) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.F32))
            return ((F32Box) d.convertToType(DataType.F32)).get();
        else
            return F32Box.defaultValue();
    }
    
    @Override
    public double getF64(int index) {
        IData d = data.get(index);
        if(d instanceof F64Box)
            return ((F64Box) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.F64))
            return ((F64Box) d.convertToType(DataType.F64)).get();
        else
            return F64Box.defaultValue();
    }
    
    @Override
    public byte[] getI8Arr(int index) {
        IData d = data.get(index);
        if(d instanceof I8ArrBox)
            return ((I8ArrBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.I8ARR))
            return ((I8ArrBox) d.convertToType(DataType.I8ARR)).get();
        else
            return I8ArrBox.defaultValue();
    }
    
    @Override
    public int[] getI32Arr(int index) {
        IData d = data.get(index);
        if(d instanceof I32ArrBox)
            return ((I32ArrBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.BOOL))
            return ((I32ArrBox) d.convertToType(DataType.BOOL)).get();
        else
            return I32ArrBox.defaultValue();
    }
    
    @Override
    public long[] getI64Arr(int index) {
        IData d = data.get(index);
        if(d instanceof I64ArrBox)
            return ((I64ArrBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.I64ARR))
            return ((I64ArrBox) d.convertToType(DataType.I64ARR)).get();
        else
            return I64ArrBox.defaultValue();
    }
    
    @Override
    public float[] getF32Arr(int index) {
        IData d = data.get(index);
        if(d instanceof F32ArrBox)
            return ((F32ArrBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.F32ARR))
            return ((F32ArrBox) d.convertToType(DataType.F32ARR)).get();
        else
            return F32ArrBox.defaultValue();
    }
    
    @Override
    public double[] getF64Arr(int index) {
        IData d = data.get(index);
        if(d instanceof F64ArrBox)
            return ((F64ArrBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.F64ARR))
            return ((F64ArrBox) d.convertToType(DataType.F64ARR)).get();
        else
            return F64ArrBox.defaultValue();
    }
    
    @Override
    public String getString(int index) {
        IData d = data.get(index);
        if(d instanceof StringBox)
            return ((StringBox) d).get();
        else if(!strict && d != null && d.canConvertToType(DataType.STRING))
            return ((StringBox) d.convertToType(DataType.STRING)).get();
        else
            return StringBox.defaultValue();
    }
    
    @Override public DataCompound getCompound() { return getCompound(index++); }
    @Override public DataList getList()         { return getList(index++);     }
    @Override public boolean  getBool()         { return getBool(index++);   }
    @Override public byte     getI8()           { return getI8(index++);     }
    @Override public short    getI16()          { return getI16(index++);    }
    @Override public int      getI32()          { return getI32(index++);    }
    @Override public long     getI64()          { return getI64(index++);    }
    @Override public float    getF32()          { return getF32(index++);    }
    @Override public double   getF64()          { return getF64(index++);    }
    @Override public byte[]   getI8Arr()        { return getI8Arr(index++);  }
    @Override public int[]    getI32Arr()       { return getI32Arr(index++); }
    @Override public long[]   getI64Arr()       { return getI64Arr(index++); }
    @Override public float[]  getF32Arr()       { return getF32Arr(index++); }
    @Override public double[] getF64Arr()       { return getF64Arr(index++); }
    @Override public String   getString()       { return getString(index++); }
    
    @Override
    public DataList setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }
    
    @Override
    public boolean isStrict() {
        return strict;
    }
    
    @Override
    public Iterator<IData> iterator() {
        return data.iterator();
    }
    
    @Override
    public void forEach(Consumer<? super IData> action) {
        // Override to use the (faster) ArrayList.forEach() rather than the
        // default forEach of Iterable.
        data.forEach(action);
    }
    
    
    /**
     * Called when this list is modified. Subclasses (namely JsonList)
     * may use this to set a dirty flag.
     */
    protected void setDirty() {
        // nothing in the default impl
    }
    
}
