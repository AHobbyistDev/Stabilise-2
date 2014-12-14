package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The compound end tag is placed at the end of every compound tag to indicate
 * the end of the compound.
 * 
 * @see NBTTag
 */
public class NBTTagCompoundEnd extends NBTTag {
	
	/**
	 * Creates a new compound end tag.
	 */
	public NBTTagCompoundEnd() {
		super(null);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		// nothing
	}

	@Override
	public void load(DataInputStream in) throws IOException {
		// nothing
	}
	
	@Override
	public String toString() {
		return "END";
	}
	
	@Override
	public byte getId() {
		return NBTTag.COMPOUND_END;
	}

	@Override
	public NBTTag copy() {
		return new NBTTagCompoundEnd();
	}

}
