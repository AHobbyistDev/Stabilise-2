package com.stabilise.util.io.data;

import static com.stabilise.util.box.Boxes.box;

import java.util.function.Consumer;

import com.stabilise.util.box.BoolBox;
import com.stabilise.util.box.ByteArrBox;
import com.stabilise.util.box.ByteBox;
import com.stabilise.util.box.CharBox;
import com.stabilise.util.box.DoubleBox;
import com.stabilise.util.box.FloatBox;
import com.stabilise.util.box.IntArrBox;
import com.stabilise.util.box.IntBox;
import com.stabilise.util.box.LongBox;
import com.stabilise.util.box.ShortBox;
import com.stabilise.util.box.StringBox;


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
    public DataCompound createCompound() {
        DataCompound c = format().newCompound();
        addData((ITag) c);
        return c;
    }
    
    @Override
    public DataList createList() {
        DataList l = format().newList();
        addData((ITag) l);
        return l;
    }
    
    // WOW, TRULY I AM A BUG FAN OF REPETITION
    
    @Override public void add(DataCompound data) { addData((ITag) data.convert(format())); }
    @Override public void add(DataList data)     { addData((ITag) data.convert(format())); }
    @Override public void add(boolean data)      { addData(box(data)); }
    @Override public void add(byte data)         { addData(box(data)); }
    @Override public void add(char data)         { addData(box(data)); }
    @Override public void add(double data)       { addData(box(data)); }
    @Override public void add(float data)        { addData(box(data)); }
    @Override public void add(int data)          { addData(box(data)); }
    @Override public void add(long data)         { addData(box(data)); }
    @Override public void add(short data)        { addData(box(data)); }
    @Override public void add(String data)       { addData(box(data)); }
    @Override public void add(byte[] data)       { addData(box(data)); }
    @Override public void add(int[] data)        { addData(box(data)); }
    
    @Override public DataCompound getCompound(int i) { return (DataCompound)getTag(i);        }
    @Override public DataList getList(int i)         { return (DataList)    getTag(i);        }
    @Override public boolean getBool(int i)          { return ((BoolBox)    getTag(i)).get(); }
    @Override public byte    getByte(int i)          { return ((ByteBox)    getTag(i)).get(); }
    @Override public char    getChar(int i)          { return ((CharBox)    getTag(i)).get(); }
    @Override public double  getDouble(int i)        { return ((DoubleBox)  getTag(i)).get(); }
    @Override public float   getFloat(int i)         { return ((FloatBox)   getTag(i)).get(); }
    @Override public int     getInt(int i)           { return ((IntBox)     getTag(i)).get(); }
    @Override public long    getLong(int i)          { return ((LongBox)    getTag(i)).get(); }
    @Override public short   getShort(int i)         { return ((ShortBox)   getTag(i)).get(); }
    @Override public String  getString(int i)        { return ((StringBox)  getTag(i)).get(); }
    @Override public byte[]  getByteArr(int i)       { return ((ByteArrBox) getTag(i)).get(); }
    @Override public int[]   getIntArr(int i)        { return ((IntArrBox)  getTag(i)).get(); }
    
    @Override public DataCompound getCompound() { return (DataCompound)getNext();        }
    @Override public DataList getList()         { return (DataList)    getNext();        }
    @Override public boolean getBool()          { return ((BoolBox)    getNext()).get(); }
    @Override public byte    getByte()          { return ((ByteBox)    getNext()).get(); }
    @Override public char    getChar()          { return ((CharBox)    getNext()).get(); }
    @Override public double  getDouble()        { return ((DoubleBox)  getNext()).get(); }
    @Override public float   getFloat()         { return ((FloatBox)   getNext()).get(); }
    @Override public int     getInt()           { return ((IntBox)     getNext()).get(); }
    @Override public long    getLong()          { return ((LongBox)    getNext()).get(); }
    @Override public short   getShort()         { return ((ShortBox)   getNext()).get(); }
    @Override public String  getString()        { return ((StringBox)  getNext()).get(); }
    @Override public byte[]  getByteArr()       { return ((ByteArrBox) getNext()).get(); }
    @Override public int[]   getIntArr()        { return ((IntArrBox)  getNext()).get(); }
    
    // From ValueExportable
    @Override
    public void io(String name, DataCompound o, boolean write) {
        if(write) o.put(name, this);
        else      o.optList(name).peek(l -> ((AbstractDataList)l).putAll(this));
    }
    
    // From ValueExportable
    @Override
    public void io(DataList l, boolean write) {
        if(write) l.add(this);
        else      ((AbstractDataList) l.createList()).putAll(this);
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
