package com.stabilise.util.nbt;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

/**
 * An NBT tag containing a single double as its payload.
 * 
 * @see NBTTag
 */
public class NBTTagDouble extends NBTTag {
	
	/** The tag's data. */
	public double data;
	
	
	/**
	 * Creates a new unnamed double tag.
	 */
	public NBTTagDouble() {
		super();
	}
	
	/**
	 * Creates a new double tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTagDouble(String tagName) {
		super(tagName);
	}
	
	/**
	 * Creates a new double tag.
	 * 
	 * @param tagName The tag's name.
	 * @param data The tag's data payload.
	 */
	public NBTTagDouble(String tagName, double data) {
		super(tagName);
		this.data = data;
	}
	
	@Override
	public void writeData(DataOutStream out) throws IOException {
		out.writeDouble(data);
	}
	
	@Override
	public void readData(DataInStream in) throws IOException {
		data = in.readDouble();
	}
	
	@Override
	byte getId() {
		return NBTTag.DOUBLE;
	}
	
	@Override
	public NBTTag copy() {
		return new NBTTagDouble(name, data);
	}
	
	@Override
	public String toString() {
		return Double.toString(data);
	}
	
}
