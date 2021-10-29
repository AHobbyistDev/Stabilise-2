package com.stabilise.util.io.data.nbt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.IData;


public class NBTList extends AbstractDataList {
    
    private byte type = NBTType.BYTE.id; // arbitrary default value
    
    
    public NBTList() {
        super();
    }
    
    public NBTList(int initialCapacity) {
        super(initialCapacity);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        type = in.readByte();
        int length = in.readInt();
        
        for(int i = 0; i < length; i++) {
            IData t = NBTType.createTag(type);
            t.readData(in);
            data.add(t);
        }
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(type);
        out.writeInt(data.size());
        
        for(IData t : data)
            t.writeData(out);
    }
    
    // All entries of an NBT list must be of the same type.
    private void checkType(IData t) {
        if(data.size() != 0) {
            if(NBTType.tagID(t) != type)
                throw new IllegalArgumentException("Attempting to append to a list"
                        + " a tag of the wrong type!");
        } else {
            type = NBTType.tagID(t);
        }
    }
    
    @Override
    public void addData(IData t) {
        checkType(t);
        super.addData(t);
    }
    
    @Override
    protected void addData2(IData t) {
        checkType(t);
        super.addData2(t);
    }
    
    @Override
    public Format format() {
        return Format.NBT;
    }
    
    @Override
    public String toString() {
        if(data.size() == 1)
            return data.size() + " entry of type " + NBTType.name(type);
        else
            return data.size() + " entries of type " + NBTType.name(type);
    }

}
