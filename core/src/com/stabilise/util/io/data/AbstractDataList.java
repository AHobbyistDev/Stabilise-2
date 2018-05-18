package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

import java.util.function.Consumer;

import com.stabilise.util.box.*;


public abstract class AbstractDataList implements DataList, ITag {
    
    /**
     * Gets the next tag in this list.
     * 
     * @throws IndexOutOfBoundsException if there isn't another tag.
     */
    protected abstract ITag getNext();
    
    /**
     * Gets a tag from this list.
     * 
     * @throws IndexOutOfBoundsException if {@code index} is out of range.
     */
    public abstract ITag getTag(int index);
    
    /**
     * Adds a tag to this list. Every insertion into this list must go through
     * this method.
     */
    protected abstract void addData(ITag data);
    
    /**
     * Performs the given action for every tag in this list.
     */
    protected abstract void forEach(Consumer<ITag> action);
    
    /**
     * Puts all tags in this list into the given list.
     */
    protected void putAll(AbstractDataList l) {
        forEach(l::addData);
    }
    
    @Override
    public DataCompound childCompound() {
        DataCompound c = format().newCompound();
        addData((ITag) c);
        return c;
    }
    
    @Override
    public DataList childList() {
        DataList l = format().newList();
        addData((ITag) l);
        return l;
    }
    
    // WOW, TRULY I AM A BUG FAN OF REPETITION
    
    @Override public void add(DataCompound data) { addData((ITag) data.convert(format())); }
    @Override public void add(DataList data)     { addData((ITag) data.convert(format())); }
    @Override public void add(boolean data)      { addData(box(data)); }
    @Override public void add(byte data)         { addData(box(data)); }
    @Override public void add(short data)        { addData(box(data)); }
    @Override public void add(int data)          { addData(box(data)); }
    @Override public void add(long data)         { addData(box(data)); }
    @Override public void add(float data)        { addData(box(data)); }
    @Override public void add(double data)       { addData(box(data)); }
    @Override public void add(byte[] data)       { addData(box(data)); }
    @Override public void add(int[] data)        { addData(box(data)); }
    @Override public void add(long[] data)       { addData(box(data)); }
    @Override public void add(float[] data)      { addData(box(data)); }
    @Override public void add(double[] data)     { addData(box(data)); }
    @Override public void add(String data)       { addData(box(data)); }
    
    @Override public DataCompound getCompound(int i) { return (DataCompound)getTag(i);        }
    @Override public DataList getList(int i)         { return (DataList)    getTag(i);        }
    @Override public boolean  getBool(int i)         { return ((BoolBox)    getTag(i)).get(); }
    @Override public byte     getI8(int i)           { return ((I8Box)      getTag(i)).get(); }
    @Override public short    getI16(int i)          { return ((I16Box)     getTag(i)).get(); }
    @Override public int      getI32(int i)          { return ((I32Box)     getTag(i)).get(); }
    @Override public long     getI64(int i)          { return ((I64Box)     getTag(i)).get(); }
    @Override public float    getF32(int i)          { return ((F32Box)     getTag(i)).get(); }
    @Override public double   getF64(int i)          { return ((F64Box)     getTag(i)).get(); }
    @Override public byte[]   getI8Arr(int i)        { return ((I8ArrBox)   getTag(i)).get(); }
    @Override public int[]    getI32Arr(int i)       { return ((I32ArrBox)  getTag(i)).get(); }
    @Override public long[]   getI64Arr(int i)       { return ((I64ArrBox)  getTag(i)).get(); }
    @Override public float[]  getF32Arr(int i)       { return ((F32ArrBox)  getTag(i)).get(); }
    @Override public double[] getF64Arr(int i)       { return ((F64ArrBox)  getTag(i)).get(); }
    @Override public String   getString(int i)       { return ((StringBox)  getTag(i)).get(); }
    
    @Override public DataCompound getCompound() { return (DataCompound)getNext();        }
    @Override public DataList getList()         { return (DataList)    getNext();        }
    @Override public boolean  getBool()         { return ((BoolBox)    getNext()).get(); }
    @Override public byte     getI8()           { return ((I8Box)      getNext()).get(); }
    @Override public short    getI16()          { return ((I16Box)     getNext()).get(); }
    @Override public int      getI32()          { return ((I32Box)     getNext()).get(); }
    @Override public long     getI64()          { return ((I64Box)     getNext()).get(); }
    @Override public float    getF32()          { return ((F32Box)     getNext()).get(); }
    @Override public double   getF64()          { return ((F64Box)     getNext()).get(); }
    @Override public byte[]   getI8Arr()        { return ((I8ArrBox)   getNext()).get(); }
    @Override public int[]    getI32Arr()       { return ((I32ArrBox)  getNext()).get(); }
    @Override public long[]   getI64Arr()       { return ((I64ArrBox)  getNext()).get(); }
    @Override public float[]  getF32Arr()       { return ((F32ArrBox)  getNext()).get(); }
    @Override public double[] getF64Arr()       { return ((F64ArrBox)  getNext()).get(); }
    @Override public String   getString()       { return ((StringBox)  getNext()).get(); }
    
    
    @Override
    public void read(String name, DataCompound o) {
        o.optList(name).peek(l -> ((AbstractDataList)l).putAll(this));
    }
    
    @Override
    public void write(String name, DataCompound o) {
        o.put(name, this);
    }
    
    @Override
    public void read(DataList l) {
        ((AbstractDataList) l.childList()).putAll(this);
    }
    
    @Override
    public void write(DataList l) {
        l.add(this);
    }
    
    @Override
    public DataList convert(Format format) {
        if(format.sameTypeAs(format()))
            return this;
        AbstractDataList l = (AbstractDataList) format.newList();
        putAll(l);
        return l;
    }
    
}
