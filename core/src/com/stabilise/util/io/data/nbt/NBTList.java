package com.stabilise.util.io.data.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ITag;


public class NBTList extends AbstractDataList {
    
    private final List<ITag> data;
    private byte type = NBTType.BYTE.id; // arbitrary default value
    private int index = 0;
    
    
    public NBTList() {
        data = new ArrayList<>();
    }
    
    public NBTList(int initialCapacity) {
        data = new ArrayList<>(initialCapacity);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        type = in.readByte();
        int length = in.readInt();
        
        for(int i = 0; i < length; i++) {
            ITag t = NBTType.createTag(type);
            t.readData(in);
            data.add(t);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(type);
        out.writeInt(data.size());
        
        for(ITag t : data)
            t.writeData(out);
    }
    
    @Override
    public int size() {
        return data.size();
    }
    
    @Override
    public boolean hasNext() {
        return index < size();
    }
    
    @Override
    protected void addData(ITag t) {
        if(data.size() != 0) {
            if(NBTType.tagID(t) != type)
                throw new IllegalArgumentException("Attempting to append to a list"
                        + " a tag of the wrong type!");
        } else {
            type = NBTType.tagID(t);
        }
            
        data.add(t);
    }
    
    @Override
    public ITag getTag(int index) {
        return data.get(index);
    }
    
    @Override
    protected ITag getNext() {
        return data.get(index++);
    }
    
    @Override
    public Format format() {
        return Format.NBT;
    }
    
    @Override
    protected void forEach(Consumer<ITag> action) {
        data.forEach(action);
    }
    
    @Override
    public String toString() {
        if(data.size() == 1)
            return data.size() + " entry of type " + NBTType.name(type);
        else
            return data.size() + " entries of type " + NBTType.name(type);
    }
    
}
