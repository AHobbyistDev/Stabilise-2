package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An NBT tag containing an arbitrary-length integer array as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagIntArray extends NBTTag {
	
	/** The tag's data. */
	public int[] data;
	
	
	/**
	 * Creates a new unnamed integer array tag.
	 */
	public NBTTagIntArray() {
		super();
	}
	
	/**
	 * Creates a new integer array tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTagIntArray(String tagName) {
		super(tagName);
	}
	
	/**
	 * Creates a new integer array tag.
	 * 
	 * @param tagName The tag's name.
	 * @param data The tag's data payload.
	 */
	public NBTTagIntArray(String tagName, int[] data) {
		super(tagName);
		this.data = data;
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(data.length);

        for(int i = 0; i < data.length; i++)
            out.writeInt(data[i]);
	}

	@Override
	public void load(DataInputStream in) throws IOException {
		int length = in.readInt();
        data = new int[length];

        for(int i = 0; i < length; i++)
            data[i] = in.readInt();
	}
	
	@Override
	public String toString() {
		return "[" + data.length + " ints]";
	}

	@Override
	public byte getId() {
		return NBTTag.INT_ARRAY;
	}

	@Override
	public NBTTag copy() {
		return new NBTTagIntArray(name, data);
	}

}
