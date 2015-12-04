package com.stabilise.util.io.data.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Tag;


public class NBTList extends AbstractDataList {
    
    private final List<Tag> data = new ArrayList<>();
    private byte type = NBTType.BYTE.id; // default value
    private int index = 0;
    
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        type = in.readByte();
        int length = in.readInt();
        
        for(int i = 0; i < length; i++) {
            Tag s = NBTType.createTag(type);
            s.readData(in);
            data.add(s);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(type);
        out.writeInt(data.size());
        
        for(Tag t : data)
            t.writeData(out);
    }
    
    // From ValueExportable
    @Override
    public void io(String name, DataCompound o, boolean write) {
        if(write) {
            AbstractDataList l = (AbstractDataList) o.getList(name);
            data.forEach(t -> l.add(t));
        } else {
            throw new UnsupportedOperationException("NYI");
        }
    }
    
    // From ValueExportable
    @Override
    public void io(DataList l, boolean write) {
        if(write) {
            AbstractDataList l2 = (AbstractDataList) l.addList();
            data.forEach(t -> l2.add(t));
        } else {
            throw new UnsupportedOperationException("NYI");
        }
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public void add(Tag t) {
        if(data.size() != 0 && NBTType.tagID(t) != type)
            throw new IllegalArgumentException("Attempting to append to a list"
                    + " a tag of the wrong type!");
        else
            type = NBTType.tagID(t);
        data.add(t);
    }
    
    @Override
    public Tag getNext() {
        return data.get(index++);
    }
    
    @Override
    public DataCompound addCompound() {
        return new NBTCompound();
    }
    
    @Override
    public DataList addList() {
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
