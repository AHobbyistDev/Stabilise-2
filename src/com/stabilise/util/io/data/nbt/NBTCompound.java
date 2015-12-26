package com.stabilise.util.io.data.nbt;

import java.io.IOException;
import java.util.Map;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractMapCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.ITag;


public class NBTCompound extends AbstractMapCompound {
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        for(Map.Entry<String, ITag> e : data.entrySet()) {
            String name = e.getKey();
            ITag tag = e.getValue();
            //if(NBTType.tagID(tag) == -1)
            //    System.out.println("Writing -1 for " + name + "(" + tag.getClass().getSimpleName() + ")");
            out.writeByte(NBTType.tagID(tag));
            out.writeUTF(name);
            tag.writeData(out);
        }
        
        out.writeByte(0); // 0 == compound end
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        byte id;
        while((id = in.readByte()) != 0) { // 0 == compound end
            String name = in.readUTF();
            ITag tag = NBTType.createTag(id);
            tag.readData(in);
            data.put(name, tag);
        }
    }
    
    @Override
    public Format format() {
        return Format.NBT;
    }
    
}
