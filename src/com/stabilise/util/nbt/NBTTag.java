package com.stabilise.util.nbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Named binary tags (NBT tags) present a convenient method of saving data.
 * 
 * <p>Created by Notch and used by Minecraft - I'm borrowing them because
 * they're so nice.
 */
public abstract class NBTTag {
	
	//--------------------==========--------------------
	//------------=====Static Constants=====------------
	//--------------------==========--------------------
	
	// I personally would order these differently, but I want this to be
	// compatible with Minecraft.
	public static final byte COMPOUND_END = 0x0;
	public static final byte BYTE = 0x1;
	public static final byte SHORT = 0x2;
	public static final byte INT = 0x3;
	public static final byte LONG = 0x4;
	public static final byte FLOAT = 0x5;
	public static final byte DOUBLE = 0x6;
	public static final byte BYTE_ARRAY = 0x7;
	public static final byte STRING = 0x8;
	public static final byte LIST = 0x9;
	public static final byte COMPOUND = 0xA;
	public static final byte INT_ARRAY = 0xB;
	
	//--------------------==========--------------------
	//------------=====Member Variables=====------------
	//--------------------==========--------------------
	
	/** The name of the tag. */
	protected String name;
	
	
	/**
	 * Creates a new unnamed NBT tag.
	 */
	public NBTTag() {
		setName("");
	}
	
	/**
	 * Creates a new NBT tag.
	 * 
	 * @param tagName The tag's name.
	 */
	public NBTTag(String tagName) {
		setName(tagName);
	}
	
	/**
	 * Sets the tag's name.
	 * 
	 * @param name The desired tag name.
	 * 
	 * @return The tag, for chaining operations.
	 */
	public NBTTag setName(String name) {
		if(name != null)
			this.name = name;
		else
			name = "";
		return this;
	}
	
	/**
	 * Gets the tag's name.
	 * 
	 * @return This NBTTag's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Writes the tag to the specified output.
	 * 
	 * @param out The output stream to which to write the tag.
	 * 
	 * @throws IOException Thrown if there is an I/O exception while writing to
	 * the output stream.
	 */
	public abstract void write(DataOutputStream out) throws IOException;
	
	/**
	 * Reads the tag from the specified input.
	 * 
	 * @param in The input stream from which to read the tag.
	 * 
	 * @throws IOException Thrown if there is an I/O exception while reading
	 * from the input stream.
	 */
	public abstract void load(DataInputStream in) throws IOException;
	
	/**
	 * Gets a string representation of the tag, as per the contract of
	 * {@link Object#toString()}.
	 * 
	 * @param prefix The string with which to prefix an ordinary invocation of
	 * {@code toString()}.
	 * 
	 * @return A string representation of the tag, as per the contract of
	 * {@link Object#toString()}.
	 */
	String toString(String prefix) {
		return toString();
	}
	
	/**
	 * Gets the type byte for the tag.
	 * 
	 * @return The tag's type byte.
	 */
	public abstract byte getId();
	
	/**
	 * Clones the tag and returns the duplicate.
	 * 
	 * @return The duplicate tag.
	 */
	public abstract NBTTag copy();
	
	//--------------------==========--------------------
	//-----------------=====Output=====-----------------
	//--------------------==========--------------------
	
	/**
	 * Gets an NBTTag object of the type specified by tagType.
	 * 
	 * @param tagType The tag's type.
	 * @param tagName The tag's name.
	 * 
	 * @return The new tag, or {@code null} if the tag type is invalid.
	 */
	public static NBTTag createTag(byte tagType, String tagName) {
		switch(tagType) {
			case BYTE:
				return new NBTTagByte(tagName);
			case BYTE_ARRAY:
				return new NBTTagByteArray(tagName);
			case SHORT:
				return new NBTTagShort(tagName);
			case INT:
				return new NBTTagInt(tagName);
			case INT_ARRAY:
				return new NBTTagIntArray(tagName);
			case LONG:
				return new NBTTagLong(tagName);
			case FLOAT:
				return new NBTTagFloat(tagName);
			case DOUBLE:
				return new NBTTagDouble(tagName);
			case STRING:
				return new NBTTagString(tagName);
			case LIST:
				return new NBTTagList(tagName);
			case COMPOUND:
				return new NBTTagCompound(tagName);
			case COMPOUND_END:
				return new NBTTagCompoundEnd();
		}
		return null;
	}
	
	/**
	 * Gets the name of a tag type.
	 * 
	 * @param tagType The ID of the tag type.
	 * 
	 * @return The name of a tag type.
	 */
	public static String getTagName(byte tagType) {
		switch(tagType) {
			case BYTE:
				return "Tag_Byte";
			case BYTE_ARRAY:
				return "Tag_ByteArray";
			case SHORT:
				return "Tag_Short";
			case INT:
				return "Tag_Int";
			case INT_ARRAY:
				return "Tag_IntArray";
			case LONG:
				return "Tag_Long";
			case FLOAT:
				return "Tag_Float";
			case DOUBLE:
				return "Tag_Double";
			case STRING:
				return "Tag_String";
			case LIST:
				return "Tag_List";
			case COMPOUND:
				return "Tag_Compound";
			case COMPOUND_END:
				return "Tag_CompoundEnd";
		}
		return "Undefined Tag";
	}

}
