package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
	public void write(DataOutputStream out) throws IOException {
		out.writeByte(data);
	}

	@Override
	public void load(DataInputStream in) throws IOException {
		data = in.readByte();
	}
	
	@Override
	public String toString() {
		return "" + data;
	}

	@Override
	public byte getId() {
		return NBTTag.BYTE;
	}

	@Override
	public NBTTagByte copy() {
		return new NBTTagByte(name, data);
	}

}
