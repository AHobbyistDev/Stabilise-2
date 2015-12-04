package com.stabilise.util.io.beta;

import static com.stabilise.util.box.Boxes.box;

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
import com.stabilise.util.io.Sendable;


public abstract class AbstractDataList implements DataList {
    
    protected abstract boolean writeMode();
    
    @Override
    public void io(Exportable data) {
        data.io(object(), writeMode());
    }
    
    @Override
    public void io(ValueExportable data) {
        data.io(this, writeMode());
    }
    
    protected abstract void add(Sendable data);
    protected abstract <T extends Sendable> T getNext();
    
    @Override public void add(boolean data) { add(box(data)); }
    @Override public void add(byte data) { add(box(data)); }
    @Override public void add(char data) { add(box(data)); }
    @Override public void add(double data) { add(box(data)); }
    @Override public void add(float data) { add(box(data)); }
    @Override public void add(int data) { add(box(data)); }
    @Override public void add(long data) { add(box(data)); }
    @Override public void add(short data) { add(box(data)); }
    @Override public void add(String data) { add(box(data)); }
    @Override public void add(byte[] data) { add(box(data)); }
    @Override public void add(int[] data) { add(box(data)); }
    
    @Override
    public boolean getBool() {
        return ((BoolBox)getNext()).get();
    }
    
    @Override
    public byte getByte() {
        return ((ByteBox)getNext()).get();
    }
    
    @Override
    public char getChar() {
        return ((CharBox)getNext()).get();
    }
    
    @Override
    public double getDouble() {
        return ((DoubleBox)getNext()).get();
    }
    
    @Override
    public float getFloat() {
        return ((FloatBox)getNext()).get();
    }
    
    @Override
    public int getInt() {
        return ((IntBox)getNext()).get();
    }
    
    @Override
    public long getLong() {
        return ((LongBox)getNext()).get();
    }
    
    @Override
    public short getShort() {
        return ((ShortBox)getNext()).get();
    }
    
    @Override
    public String getString() {
        return ((StringBox)getNext()).get();
    }
    
    @Override
    public byte[] getByteArr() {
        return ((ByteArrBox)getNext()).get();
    }
    
    @Override
    public int[] getIntArr() {
        return ((IntArrBox)getNext()).get();
    }
    
}
