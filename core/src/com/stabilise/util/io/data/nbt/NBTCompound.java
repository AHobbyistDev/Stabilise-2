package com.stabilise.util.io.data.nbt;

import java.io.IOException;
import java.util.Map;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractDataCompound;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.IData;


public class NBTCompound extends AbstractDataCompound {
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        for(Map.Entry<String, IData> e : data.entrySet()) {
            String name = e.getKey();
            IData tag = e.getValue();
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
            IData tag = NBTType.createTag(id);
            tag.readData(in);
            data.put(name, tag);
        }
    }
    
    @Override
    public Format format() {
        return Format.NBT;
    }
    
}
