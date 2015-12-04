package com.stabilise.util.io.beta.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.Sendable;
import com.stabilise.util.io.beta.AbstractDataList;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;


public class NBTList extends AbstractDataList {
    
    private final List<Sendable> data = new ArrayList<>();
    private byte type = NBTType.BYTE.id; // default value
    private int index = 0;
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        type = in.readByte();
        int length = in.readInt();
        
        for(int i = 0; i < length; i++) {
            Sendable s = NBTType.createTag(type);
            s.readData(in);
            data.add(s);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(type);
        out.writeInt(data.size());
        
        for(Sendable s : data)
            s.writeData(out);
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public void add(Sendable s) {
        if(data.size() != 0 && NBTType.tagID(s) != type)
            throw new IllegalArgumentException("Attempting to append to a list"
                    + " a tag of the wrong type!");
        else
            type = NBTType.tagID(s);
        data.add(s);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <T extends Sendable> T getNext() {
        return (T) data.get(index++);
    }
    
    @Override
    public DataObject object() {
        return new NBTCompound();
    }
    
    @Override
    public DataList list() {
        return new NBTList();
    }
    
    @Override
    protected boolean writeMode() {
        return false;
    }
    
    @Override
    public String toString() {
        if(data.size() == 1)
            return data.size() + " entry of type " + NBTType.name(type);
        else
            return data.size() + " entries of type " + NBTType.name(type);
    }
    
}
