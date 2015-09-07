package com.stabilise.util.nbt;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * An NBT tag containing a single byte as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagByte extends NBTTag {
    
    /** The tag's data. */
    public byte data;
    
    
    /**
     * Creates a new unnamed byte tag.
     */
    public NBTTagByte() {
        super();
    }

    /**
     * Creates a new byte tag.
     * 
     * @param tagName The tag's name.
     */
    public NBTTagByte(String tagName) {
        super(tagName);
    }
    
    /**
     * Creates a new byte tag.
     * 
     * @param tagName The tag's name.
     * @param data The tag's data payload.
     */
    public NBTTagByte(String tagName, byte data) {
        super(tagName);
        this.data = data;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeByte(data);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data = in.readByte();
    }
    
    @Override
    byte getId() {
        return NBTTag.BYTE;
    }
    
    @Override
    public NBTTagByte copy() {
        return new NBTTagByte(name, data);
    }
    
    @Override
    public String toString() {
        return Byte.toString(data);
    }
    
}
