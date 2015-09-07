package com.stabilise.util.nbt;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * An NBT tag containing a single integer as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagInt extends NBTTag {
    
    /** The tag's data. */
    public int data;
    
    
    /**
     * Creates a new unnamed integer tag.
     */
    public NBTTagInt() {
        super();
    }
    
    /**
     * Creates a new integer tag.
     * 
     * @param tagName The tag's name.
     */
    public NBTTagInt(String tagName) {
        super(tagName);
    }
    
    /**
     * Creates a new integer tag.
     * 
     * @param tagName The tag's name.
     * @param data The tag's data payload.
     */
    public NBTTagInt(String tagName, int data) {
        super(tagName);
        this.data = data;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeInt(data);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data = in.readInt();
    }
    
    @Override
    byte getId() {
        return NBTTag.INT;
    }
    
    @Override
    public NBTTag copy() {
        return new NBTTagInt(name, data);
    }
    
    @Override
    public String toString() {
        return Integer.toString(data);
    }
    
}
