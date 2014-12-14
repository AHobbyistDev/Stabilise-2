package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * An NBT tag containing a string as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagString extends NBTTag {
	
	/** The tag's data. */
	public String data;
	
	
	/**
	 * Creates a new unnamed string tag.
	 */
	public NBTTagString() {
		super();
	}
	
	/**
	 * Creates a new string tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTagString(String tagName) {
		super(tagName);
	}
	
	/**
	 * Creates a new string tag.
	 * 
	 * @param tagName The tag's name.
	 * @param data The tag's data payload.
	 * 
	 * @throws IllegalArgumentException Thrown if the given string is either
	 * null or empty.
	 */
	public NBTTagString(String tagName, String data) {
		super(tagName);
		this.data = data;
		if(data == null || data == "")
			throw new IllegalArgumentException("An NBT String may not be empty!");
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		if(data != null)
			out.writeUTF(data);
		else
			throw new IOException("Attempting to write an undefined string! (" + name + ")");
	}

	@Override
	public void load(DataInputStream in) throws IOException {
		data = in.readUTF();
	}
	
	@Override
	public String toString() {
		return "" + data;
	}

	@Override
	public byte getId() {
		return NBTTag.STRING;
	}

	@Override
	public NBTTag copy() {
		return new NBTTagString(name, data);
	}

}
