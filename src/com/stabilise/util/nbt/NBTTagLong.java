package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An NBT tag containing a single long as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagLong extends NBTTag {
	
	/** The tag's data. */
	public long data;
	
	
	/**
	 * Creates a new unnamed long tag.
	 */
	public NBTTagLong() {
		super();
	}
	
	/**
	 * Creates a new long tag.
	 * 
	 * @param tagName The tag name.
	 */
	public NBTTagLong(String tagName) {
		super(tagName);
	}
	
	/**
	 * Creates a new long tag.
	 * 
	 * @param tagName The tag's name.
	 * @param data The tag's data payload.
	 */
	public NBTTagLong(String tagName, long data) {
		super(tagName);
		this.data = data;
	}
	
	@Override
	void write(DataOutputStream out) throws IOException {
		out.writeLong(data);
	}
	
	@Override
	void load(DataInputStream in) throws IOException {
		data = in.readLong();
	}
	
	@Override
	public byte getId() {
		return NBTTag.LONG;
	}
	
	@Override
	public NBTTag copy() {
		return new NBTTagLong(name, data);
	}
	
	@Override
	public String toString() {
		return Long.toString(data);
	}
	
}
