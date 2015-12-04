package com.stabilise.util.io.data.nbt;

import java.io.IOException;
import java.util.Map;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;
import com.stabilise.util.io.data.AbstractCompound;
import com.stabilise.util.io.data.AbstractMapCompound;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.Format;
import com.stabilise.util.io.data.Tag;


public class NBTCompound extends AbstractMapCompound {
    
    /**
     * Creates a new compound in write mode.
     */
    public NBTCompound() {
        writeMode = true;
    }
    
    NBTCompound(NBTCompound parent) {
        writeMode = parent.writeMode;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        for(Map.Entry<String, Tag> e : data.entrySet()) {
            String name = e.getKey();
            Tag tag = e.getValue();
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
            Tag tag = NBTType.createTag(id);
            tag.readData(in);
            data.put(name, tag);
        }
    }
    
    // From ValueExportable
    @Override
    public void io(String name, DataCompound o, boolean write) {
        if(write) {
            AbstractCompound c = (AbstractCompound) o.getCompound(name);
            forEachTag((n, t) -> c.put(n, t));
        } else {
            throw new UnsupportedOperationException("NYI");
        }
    }
    
    // From ValueExportable
    @Override
    public void io(DataList l, boolean write) {
        if(write) {
            AbstractCompound c = (AbstractCompound) l.addCompound();
            forEachTag((n, t) -> c.put(n, t));
        } else {
            throw new UnsupportedOperationException("NYI");
        }
    }
    
    @Override
    public DataCompound getCompound(String name) {
        return get(name, NBTCompound.class).orElseGet(() -> put(name, new NBTCompound(this))); 
    }
    
    @Override
    public DataList getList(String name) {
        return get(name, NBTList.class).orElseGet(() -> put(name, new NBTList()));
    }
    
    @Override
    public Format format() {
        return Format.NBT;
    }
    
    @Override
    public DataCompound convert(Format format) {
        if(format == Format.NBT || format == Format.NBT_SIMPLE) return this;
        AbstractCompound c = (AbstractCompound) format.create(true);
        for(Map.Entry<String, Tag> e : data.entrySet())
            c.put(e.getKey(), e.getValue());
        c.setReadMode();
        return c;
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    private String toString(String prefix) {
        String pre = prefix + "    ";
        StringBuilder sb = new StringBuilder("[\n");
        
        for(Map.Entry<String, Tag> e : data.entrySet()) {
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
