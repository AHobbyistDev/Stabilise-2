package com.stabilise.util.io.beta.bytes;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.Sendable;
import com.stabilise.util.io.beta.AbstractDataList;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;


public class ByteList extends AbstractDataList {
    
    private final ByteObject backer;
    
    ByteList(ByteObject backer) {
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
    
    @Override
    public int size() {
        return 0;
    }
    
    @Override
    protected void add(Sendable o) {
        backer.checkCanWrite2();
        try {
            o.writeData(backer.writer);
        } catch(IOException e) {
            throw new AssertionError("This shouldn't ever happen", e);
        }
    }
    
    @Override
    protected <T extends Sendable> T getNext() {
        throw new UnsupportedOperationException();
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
    public DataObject object() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public DataList list() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean writeMode() {
        // TODO Auto-generated method stub
        return false;
    }

}
