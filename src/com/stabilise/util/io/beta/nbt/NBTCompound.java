package com.stabilise.util.io.beta.nbt;

import java.io.IOException;
import java.util.Map;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.Sendable;
import com.stabilise.util.io.beta.AbstractMapObject;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;


public class NBTCompound extends AbstractMapObject {
    
    public NBTCompound() {
        writeMode = true;
    }
    
    NBTCompound(NBTCompound parent) {
        writeMode = parent.writeMode;
    }
    
    public static void writeTag(DataOutStream out, String name, Sendable tag) throws IOException {
        out.writeByte(NBTType.tagID(tag));
        out.writeUTF(name);
        tag.writeData(out);
    }
    
    public static NBTCompound readTag(DataInStream in) throws IOException {
        NBTCompound c = new NBTCompound();
        if(in.readByte() != NBTType.tagID(c))
            throw new IOException("Root tag must be a named compound");
        in.readUTF(); // get rid of unwanted name
        c.readData(in);
        return c;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        for(Map.Entry<String, Sendable> tag : data.entrySet()) {
            writeTag(out, tag.getKey(), tag.getValue());
        }
        
        out.writeByte(0); // 0 == compound end
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data.clear();
        
        byte id;
        while((id = in.readByte()) != 0) { // 0 == compound end
            String name = in.readUTF();
            Sendable tag = NBTType.createTag(id);
            tag.readData(in);
            data.put(name, tag);
        }
    }
    
    @Override
    public DataObject object(String name) {
        return get(name, NBTCompound.class).orElseGet(() -> put(name, new NBTCompound(this))); 
    }
    
    @Override
    public DataList list(String name) {
        return get(name, NBTList.class).orElseGet(() -> put(name, new NBTList()));
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Map.Entry<String, Sendable> e : data.entrySet()) {
            sb.append(pre);
            sb.append("\"");
            sb.append(e.getKey());
            sb.append("\": ");
            if(e.getValue() instanceof NBTCompound)
                sb.append(((NBTCompound) e.getValue()).toString(pre));
            else
                sb.append(e.getValue().toString());
            sb.append(",\n");
        }
        
        sb.append(prefix);
        sb.append("]");

        return sb.toString();
    }
    
}
