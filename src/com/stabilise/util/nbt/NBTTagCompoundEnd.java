package com.stabilise.util.nbt;

import java.io.IOException;

import com.stabilise.util.io.DataInStream;
import com.stabilise.util.io.DataOutStream;

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
	public void writeData(DataOutStream out) throws IOException {
		// nothing to see here, move along
	}
	
	@Override
	public void readData(DataInStream in) throws IOException {
		// nothing to see here, move along
	}
	
	@Override
	public String toString() {
		return "END";
	}
	
	@Override
	byte getId() {
		return NBTTag.COMPOUND_END;
	}
	
	@Override
	public NBTTag copy() {
		return new NBTTagCompoundEnd();
	}
	
}
