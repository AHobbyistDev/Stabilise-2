package com.stabilise.util.nbt;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * An NBT tag containing a single float as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagFloat extends NBTTag {
    
    /** The tag's data. */
    public float data;
    
    
    /**
     * Creates a new unnamed float tag.
     */
    public NBTTagFloat() {
        super();
    }
    
    /**
     * Creates a new float tag.
     * 
     * @param tagName The tag's name.
     */
    public NBTTagFloat(String tagName) {
        super(tagName);
    }
    
    /**
     * Creates a new float tag.
     * 
     * @param tagName The tag's name.
     * @param data The tag's data payload.
     */
    public NBTTagFloat(String tagName, float data) {
        super(tagName);
        this.data = data;
    }
    
    @Override
    public void writeData(DataOutStream out) throws IOException {
        out.writeFloat(data);
    }
    
    @Override
    public void readData(DataInStream in) throws IOException {
        data = in.readFloat();
    }
    
    @Override
    byte getId() {
        return NBTTag.FLOAT;
    }
    
    @Override
    public NBTTag copy() {
        return new NBTTagFloat(name, data);
    }
    
    @Override
    public String toString() {
        return Float.toString(data);
    }
    
}
