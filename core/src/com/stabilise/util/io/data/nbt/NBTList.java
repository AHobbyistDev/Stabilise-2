package com.stabilise.util.io.data.nbt;

import java.io.IOException;

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
    protected void checkType(IData d) {
        if(size() > 0) {
            if(NBTType.tagID(d) != type) {
                throw new IllegalArgumentException("Invalid type! (Tried adding "
                        + d.type() + " to a list containing " + type + ")");
            }
        } else {
            type = NBTType.tagID(d);
        }
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
