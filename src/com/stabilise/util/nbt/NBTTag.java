package com.stabilise.util.nbt;

import com.stabilise.util.io.Sendable;

/**
 * Named binary tags (NBT tags) present a convenient method of saving data.
 * 
 * <p>Created by Notch and used by Minecraft - I'm borrowing them because
 * they're so nice.
 */
@Deprecated
public abstract class NBTTag implements Sendable {
    
    //--------------------==========--------------------
    //------------=====Static Constants=====------------
    //--------------------==========--------------------
    
    // I personally would order these differently, but I want this to be
    // compatible with Minecraft.
    static final byte COMPOUND_END = 0x0;
    static final byte BYTE = 0x1;
    static final byte SHORT = 0x2;
    static final byte INT = 0x3;
    static final byte LONG = 0x4;
    static final byte FLOAT = 0x5;
    static final byte DOUBLE = 0x6;
    static final byte BYTE_ARRAY = 0x7;
    static final byte STRING = 0x8;
    static final byte LIST = 0x9;
    static final byte COMPOUND = 0xA;
    static final byte INT_ARRAY = 0xB;
    
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
     * @param tagName The tag name.
     */
    public NBTTag(String tagName) {
        setName(tagName);
    }
    
    /**
     * Sets this tag's name. If {@code name} is {@code null}, the name is set
     * to an empty string.
     * 
     * @param name The desired tag name.
     * 
     * @return This tag.
     */
    public NBTTag setName(String name) {
        this.name = name != null ? name : "";
        return this;
    }
    
    /**
     * Gets this tag's name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets a string representation of the tag, as per the contract of
     * {@link Object#toString()}, but with every new line prefixed with {@code
     * prefix}.
     */
    String toString(String prefix) {
        return toString();
    }
    
    /**
     * @return This tag's ID.
     */
    abstract byte getId();
    
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
    static NBTTag createTag(byte tagType, String tagName) {
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
    static String getTagName(byte tagType) {
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
