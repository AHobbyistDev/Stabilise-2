package com.stabilise.util.nbt;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * An NBT tag containing a single short as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagShort extends NBTTag {
	
	/** The tag's data. */
	public short data;
	
	
	/**
	 * Creates a new unnamed short tag.
	 */
	public NBTTagShort() {
		super();
	}
	
	/**
	 * Creates a new short tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTagShort(String tagName) {
		super(tagName);
	}
	
	/**
	 * Creates a new short tag.
	 * 
	 * @param tagName The tag's name.
	 * @param data The tag's data payload.
	 */
	public NBTTagShort(String tagName, short data) {
		super(tagName);
		this.data = data;
	}
	
	@Override
	public void writeData(DataOutStream out) throws IOException {
		out.writeShort(data);
	}
	
	@Override
	public void readData(DataInStream in) throws IOException {
		data = in.readShort();
	}
	
	@Override
	public byte getId() {
		return NBTTag.SHORT;
	}
	
	@Override
	public NBTTag copy() {
		return new NBTTagShort(name, data);
	}
	
	@Override
	public String toString() {
		return Short.toString(data);
	}
	
}
