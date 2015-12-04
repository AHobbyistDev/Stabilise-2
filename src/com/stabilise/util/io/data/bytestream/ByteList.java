package com.stabilise.util.io.data.bytestream;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Tag;


public class ByteList extends AbstractDataList {
    
    private final ByteCompound backer;
    
    ByteList(ByteCompound backer) {
        this.backer = backer;
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        throw new UnsupportedOperationException("Cannot directly write a list!");
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        throw new UnsupportedOperationException("Cannot directly read a list!");
    }
    
    // From ValueExportable
    @Override
    public void io(String name, DataCompound o, boolean write) {
        throw new UnsupportedOperationException("NYI");
    }
    
    // From ValueExportable
    @Override
    public void io(DataList l, boolean write) {
        throw new UnsupportedOperationException("NYI");
    }
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    public void add(Tag o) {
        backer.checkCanWrite2();
        try {
            o.writeData(backer.writer);
        } catch(IOException e) {
            throw new AssertionError("This shouldn't ever happen", e);
        }
    }
    
    @Override
    public Tag getNext() {
        throw new UnsupportedOperationException("A ByteList requires context!");
    }
    
    @Override
    public boolean getBool() {
        return backer.getBool(null);
    }
    
    @Override
    public byte getByte() {
        return backer.getByte(null);
    }
    
    @Override
    public char getChar() {
        return backer.getChar(null);
    }
    
    @Override
    public double getDouble() {
        return backer.getDouble(null);
    }
    
    @Override
    public float getFloat() {
        return backer.getFloat(null);
    }
    
    @Override
    public int getInt() {
        return backer.getInt(null);
    }
    
    @Override
    public long getLong() {
        return backer.getLong(null);
    }
    
    @Override
    public short getShort() {
        return backer.getShort(null);
    }
    
    @Override
    public String getString() {
        return backer.getString(null);
    }
    
    @Override
    public byte[] getByteArr() {
        return backer.getByteArr(null);
    }
    
    @Override
    public int[] getIntArr() {
        return backer.getIntArr(null);
    }
    
    @Override
    public DataCompound addCompound() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public DataList addList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean writeMode() {
        return false;
    }

}
