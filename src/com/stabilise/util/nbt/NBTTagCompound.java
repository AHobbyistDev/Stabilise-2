package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.stabilise.util.Log;

/**
 * A compound tag is essentially a tag 'group' which may contain any number
 * of tags of any type.
 * 
 * @see NBTTag
 */
public class NBTTagCompound extends NBTTag {
	
	/** The tag's data. */		// Linked as to allow for consistent iteration
	public HashMap<String, NBTTag> data = new LinkedHashMap<String, NBTTag>();
	
	
	/**
	 * Creates a new unnamed compound tag.
	 */
	public NBTTagCompound() {
		super();
	}
	
	/**
	 * Creates a new compound tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTagCompound(String tagName) {
		super(tagName);
	}
	
	@Override
	void write(DataOutputStream out) throws IOException {
		for(NBTTag tag : data.values())
			NBTIO.writeTag(out, tag);
		
		out.writeByte(NBTTag.COMPOUND_END);
	}
	
	@Override
	void load(DataInputStream in) throws IOException {
		data.clear();
		NBTTag tag;
		
		while((tag = NBTIO.readTag(in)).getId() != NBTTag.COMPOUND_END)
			data.put(tag.name, tag);
	}
	
	/**
	 * Adds an NBTTag object to the tag compound. If a tag by the given name
	 * already exists within the compound, it will be overwritten.
	 * 
	 * @param tag The tag to add to the compound.
	 */
	public void addTag(NBTTag tag) {
		data.put(tag.name, tag);
	}
	
	/**
	 * Adds an NBTTag object to the tag compound. If a tag by the given name
	 * already exists within the compound, it will be overwritten.
	 * 
	 * @param tagName The name under which to add the tag.
	 * @param tag The tag to add to the compound.
	 */
	public void addTag(String tagName, NBTTag tag) {
		data.put(tagName, tag.setName(tagName));
	}
	
	/**
	 * Checks for whether or not the compound contains a tag.
	 * 
	 * @param tagName The tag's name.
	 * 
	 * @return {@code true} if the compound contains a tag of the specified
	 * name; {@code false} otherwise.
	 */
	public boolean hasTag(String tagName) {
		return data.containsKey(tagName);
	}
	
	/**
	 * Removes a tag from the tag compound.
	 * 
	 * @param The name of the tag to remove from the compound.
	 */
	public void removeTag(String tagName) {
		data.remove(tagName);
	}
	
	//---------------=====Adding Tags=====--------------
	
	/**
	 * Adds an NBTTagByte tag representing a byte value to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addByte(String tagName, byte data) {
		this.data.put(tagName, new NBTTagByte(tagName, data));
	}
	
	/**
	 * Adds an NBTTagByte tag representing a boolean value to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addBoolean(String tagName, boolean data) {
		addByte(tagName, (byte)(data ? 0x1 : 0x0));
	}
	
	/**
	 * Adds an NBTTagByteArray tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addByteArray(String tagName, byte[] data) {
		this.data.put(tagName, new NBTTagByteArray(tagName, data));
	}
	
	/**
	 * Adds an NBTTagShort tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addShort(String tagName, short data) {
		this.data.put(tagName, new NBTTagShort(tagName, data));
	}
	
	/**
	 * Adds an NBTTagInt tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addInt(String tagName, int data) {
		this.data.put(tagName, new NBTTagInt(tagName, data));
	}
	
	/**
	 * Adds an NBTTagIntArray tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addIntArray(String tagName, int[] data) {
		this.data.put(tagName, new NBTTagIntArray(tagName, data));
	}
	
	/**
	 * Adds an NBTTagLong tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addLong(String tagName, long data) {
		this.data.put(tagName, new NBTTagLong(tagName, data));
	}
	
	/**
	 * Adds an NBTTagFloat tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addFloat(String tagName, float data) {
		this.data.put(tagName, new NBTTagFloat(tagName, data));
	}
	
	/**
	 * Adds an NBTTagDouble tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addDouble(String tagName, double data) {
		this.data.put(tagName, new NBTTagDouble(tagName, data));
	}
	
	/**
	 * Adds an NBTTagString tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param data The tag's payload.
	 */
	public void addString(String tagName, String data) {
		this.data.put(tagName, new NBTTagString(tagName, data));
	}
	
	/**
	 * Adds an {@link NBTTagList} tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param tag The tag.
	 */
	public void addList(String tagName, NBTTagList tag) {
		this.data.put(tagName, tag.setName(tagName));
	}
	
	/**
	 * Adds an {@link NBTTagCompound} tag to the tag compound.
	 * If a tag by the given name already exists within the compound, it will
	 * be overwritten.
	 * 
	 * @param tagName The name of the tag.
	 * @param tag The tag.
	 */
	public void addCompound(String tagName, NBTTagCompound tag) {
		this.data.put(tagName, tag.setName(tagName));
	}
	
	//--------------=====Getting Tags=====--------------
	
	/**
	 * Gets the byte value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The byte value held by the specified tag, or {@code 0x0} if the
	 * tag does not exist or contains a different data format.
	 */
	public byte getByte(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagByte)data.get(tagName)).data : 0;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading byte \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return 0;
		}
	}
	
	/**
	 * Gets the boolean value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The boolean value held by the specified tag, or {@code false} if
	 * the tag does not exist or contains a different data format.
	 */
	public boolean getBoolean(String tagName) {
		try {
			return hasTag(tagName) ? (((NBTTagByte)data.get(tagName)).data == 1 ? true : false) : false;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading byte \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return false;
		}
	}
	
	/**
	 * Gets the byte array held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The byte array held by the specified tag, or an empty byte array
	 * if the tag does not exist or contains a different data format.
	 */
	public byte[] getByteArray(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagByteArray)data.get(tagName)).data : new byte[0];
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading byte array \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return new byte[0];
		}
	}
	
	/**
	 * Gets the short value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The short value held by the specified tag, or {@code 0} if the
	 * tag does not exist or contains a different data format.
	 */
	public short getShort(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagShort)data.get(tagName)).data : 0;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading short \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return 0;
		}
	}
	
	/**
	 * Gets the integer value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The integer value held by the specified tag, or {@code 0} if the
	 * tag does not exist or contains a different data format.
	 */
	public int getInt(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagInt)data.get(tagName)).data : 0;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading int \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return 0;
		}
	}
	
	/**
	 * Gets the integer array held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The integer array held by the specified tag, or an empty int
	 * array if the tag does not exist or contains a different data format.
	 */
	public int[] getIntArray(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagIntArray)data.get(tagName)).data : new int[0];
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading int array \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return new int[0];
		}
	}
	
	/**
	 * Gets the long value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The long value held by the specified tag, or {@code 0} if the
	 * tag does not exist or contains a different data format.
	 */
	public long getLong(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagLong)data.get(tagName)).data : 0L;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading long \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return 0L;
		}
	}
	
	/**
	 * Gets the float value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The byte float held by the specified tag, or {@code 0.0} if the
	 * tag does not exist or contains a different data format.
	 */
	public float getFloat(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagFloat)data.get(tagName)).data : 0.0F;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading short \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return 0.0F;
		}
	}
	
	/**
	 * Gets the double value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The double value held by the specified tag, or {@code 0.0} if
	 * the tag does not exist or contains a different data format.
	 */
	public double getDouble(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagDouble)data.get(tagName)).data : 0.0D;
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading short \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return 0.0D;
		}
	}
	
	/**
	 * Gets the string value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The string value held by the specified tag, or an empty string
	 * if the tag does not exist or contains a different data format.
	 */
	public String getString(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagString)data.get(tagName)).data : "";
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading short \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return "";
		}
	}
	
	/**
	 * Gets the {@link NBTTagList} object of the specified name.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The tag list, or an empty list tag with the given name if the
	 * tag does not exist or contains a different data format.
	 */
	public NBTTagList getList(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagList)data.get(tagName)) : new NBTTagList(tagName);
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading short \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return new NBTTagList(tagName);
		}
	}
	
	/**
	 * Gets the {@link NBTTagCompound} object of the specified name.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The tag compound, or an empty compound tag with the given name
	 * if the tag does not exist or contains a different data format.
	 */
	public NBTTagCompound getCompound(String tagName) {
		try {
			return hasTag(tagName) ? ((NBTTagCompound)data.get(tagName)) : new NBTTagCompound(tagName);
		} catch(ClassCastException e) {
			Log.get().postWarning("Error reading short \"" + tagName + "\" from compound tag \"" + name + "\"!");
			return new NBTTagCompound(tagName);
		}
	}
	
	// ---------- Unsafe Getters ----------
	
	/**
	 * Gets the byte value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The byte value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public byte getByteUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagByte)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the boolean value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The boolean value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public boolean getBooleanUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagByte)data.get(tagName)).data == 1 ? true : false;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the byte array held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The byte array held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public byte[] getByteArrayUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagByteArray)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the short value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The short value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public short getShortUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagShort)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the integer value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The integer value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public int getIntUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagInt)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the integer array held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The integer array held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public int[] getIntArrayUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagIntArray)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the long value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The long value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public long getLongUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagLong)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the float value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The byte float held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public float getFloatUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagFloat)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the double value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The double value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public double getDoubleUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagDouble)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the string value held by the specified tag.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The string value held by the specified tag.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public String getStringUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagString)data.get(tagName)).data;
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the {@link NBTTagList} object of the specified name.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The tag list.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public NBTTagList getListUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagList)data.get(tagName));
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * Gets the {@link NBTTagCompound} object of the specified name.
	 * 
	 * @param tagName The name of the tag.
	 * 
	 * @return The tag compound.
	 * @throws IOException if the tag does not exist or contains a different
	 * data format.
	 */
	public NBTTagCompound getCompoundUnsafe(String tagName) throws IOException {
		if(hasTag(tagName)) {
			try {
				return ((NBTTagCompound)data.get(tagName));
			} catch(ClassCastException ignored) {}
		}
		throw new IOException();
	}
	
	/**
	 * @return A Collection of the tags held by this compound tag.
	 */
	public Collection<NBTTag> getTags() {
		return data.values();
	}
	
	/**
	 * @return {@code true} if this compound is empty; {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return data.size() == 0;
	}
	
	@Override
	byte getId() {
		return NBTTag.COMPOUND;
	}
	
	@Override
	public NBTTagCompound copy() {
		NBTTagCompound clone = new NBTTagCompound(name);
		Iterator<Entry<String, NBTTag>> iterator = data.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Entry<String, NBTTag> entry = iterator.next();
			clone.addTag(entry.getKey(), entry.getValue().copy());
		}
		
		return clone;
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	@Override
	String toString(String prefix) {
		String pre = prefix + "    ";
		StringBuilder sb = new StringBuilder("[\n");
		
		for(String tagName : data.keySet()) {
			sb.append(pre);
			sb.append("\"");
			sb.append(tagName);
			sb.append("\": ");
			sb.append(data.get(tagName).toString(pre));
			sb.append(",\n");
		}
		
		sb.append(prefix);
		sb.append("]");

        return sb.toString();
	}
	
}
