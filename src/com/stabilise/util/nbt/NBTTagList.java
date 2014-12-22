package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An NBT tag containing an arbitrary-length list of tags of the same tag type.
 * Note that the contents of an NBTTagList need not be assigned names.
 * 
 * @see NBTTag
 */
public class NBTTagList extends NBTTag implements Iterable<NBTTag> {
	
	/** The tag's data. */
	private List<NBTTag> data = new ArrayList<NBTTag>();
	/** The tag's data type. */
	private byte dataType;
	
	
	/**
	 * Creates a new unnamed list tag.
	 */
	public NBTTagList() {
		super();
	}
	
	/**
	 * Creates a new list tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTagList(String tagName) {
		super(tagName);
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		if(!data.isEmpty()) {
			dataType = ((NBTTag)data.get(0)).getId();
		} else {
			dataType = NBTTag.BYTE;
		}
		
		out.writeByte(dataType);
		out.writeInt(data.size());
		
		for(NBTTag t : data)
			t.write(out);
	}

	@Override
	public void load(DataInputStream in) throws IOException {
		dataType = in.readByte();
		int length = in.readInt();
		data = new ArrayList<NBTTag>();
		
		for(int i = 0; i < length; i++) {
			NBTTag tag = NBTTag.createTag(dataType, null);
			tag.load(in);
			data.add(tag);
		}
	}
	
	/**
	 * Appends a tag to the list.
	 * If the list is empty, it's tag type will become that of the given tag.
	 * If the given tag is not the same type as all pre-existing tags, this
	 * method will throw an exception.
	 * 
	 * @param tag The tag to append to the list.
	 * 
	 * @throws IllegalArgumentException Thrown if the given tag's type is not
	 * the same as the rest of the list.
	 */
	public void appendTag(NBTTag tag) {
		if(data.size() != 0) {
			if(tag.getId() != dataType)
				throw new IllegalArgumentException("Attempting to append to a list (" + name + ") a tag of a different data type!");
			else
				data.add(tag);
		} else {
			dataType = tag.getId();
			data.add(tag);
		}
	}
	
	/**
	 * Removes a tag from the list.
	 * 
	 * @param index The index from which to remove a tag.
	 */
	public void removeTagAt(int index) {
		data.remove(index);
	}
	
	/**
	 * Gets a tag from the list.
	 * 
	 * @param index The index from which to get the tag.
	 * 
	 * @return The tag at the given index, or null if there is no tag at that
	 * index.
	 */
	public NBTTag getTagAt(int index) {
		return data.get(index);
	}
	
	/**
	 * Gets the number of tags in the list.
	 * 
	 * @return The number of tags in the list.
	 */
	public int size() {
		return data.size();
	}
	
	@Override
	public String toString() {
		if(data.size() == 1)
			return data.size() + " entry of type " + NBTTag.getTagName(dataType);
		else
			return data.size() + " entries of type " + NBTTag.getTagName(dataType);
	}
	
	@Override
	public byte getId() {
		return NBTTag.LIST;
	}
	
	@Override
	public NBTTag copy() {
		NBTTagList clone = new NBTTagList(name);
		for(NBTTag t : data)
			clone.appendTag(t.copy());
		return clone;
	}
	
	@Override
	public Iterator<NBTTag> iterator() {
		return data.iterator();
	}
	
}
