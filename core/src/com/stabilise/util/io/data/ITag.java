package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * Generic interface for a Tag, which is anything that can live within a
 * {@link DataCompound}.
 */
public interface ITag extends Sendable {
    
    /**
     * Reads this tag to the given DataCompound.
     * 
     * @param name The name assigned to this tag. This tag should read from
     * the data with this name.
     * @param o The DataCompound to read from.
     */
    void read(String name, DataCompound o);
    
    /**
     * Writes this tag to the given DataCompound.
     * 
     * @param name The name assigned to this tag. This tag should write its
     * data under this name.
     * @param o The DataCompound to write this tag to.
     */
    void write(String name, DataCompound o);
    
    /**
     * Reads this tag from the given DataList.
     * @param l
     */
    void read(DataList l);
    
    /**
     * Writes this tag to the given DataList.
     */
    void write(DataList l);
    
	/**
	 * Returns {@code true} if the given tag holds the same type of data as
	 * this tag.
	 */
	default boolean isSameType(ITag other) {
		return getClass().equals(other.getClass());
	}
	
	/**
     * Returns {@code true} if the given tag holds either the same type of
     * data as this, or holds a type which may be converted to this type. This
     * is a weaker condition than {@link #isSameType(ITag)}.
     */
    default boolean isCompatibleType(ITag other) {
        return isSameType(other);
    }
    
    /**
     * Converts the given tag to a tag of this type.
     * 
     * @throws IllegalStateException if the other tag is {@link
     * #isCompatibleType(ITag) not compatible}.
     */
    ITag convertToSameType(ITag other);
	
	
	/**
	 * Returns {@code true} if this tag's value may be converted to an
	 * appropriate boolean.
	 * 
	 * <p>Works for: boolean, byte, short, int, long, float, double, char.
	 */
	boolean isBoolean();
	
	/**
	 * Returns {@code true} if this tag's value may be converted to an
	 * appropriate int.
	 * 
	 * <p>Works for: boolean, byte, short, int, long, float, double, char.
	 */
	default boolean isInt() { return isLong(); }
	
	/**
     * Returns {@code true} if this tag's value may be converted to an
     * appropriate long.
     * 
     * <p>Works for: boolean, byte, short, int, long, float, double, char.
     */
	boolean isLong();
	
	/**
     * Returns {@code true} if this tag's value may be converted to an
     * appropriate float.
     * 
     * <p>Works for: boolean, byte, short, int, long, float, double, char.
     */
	default boolean isFloat() {
	    return isDouble();
	}
	
	/**
     * Returns {@code true} if this tag's value may be converted to an
     * appropriate double.
     * 
     * <p>Works for: boolean, byte, short, int, long, float, double, char.
     */
	boolean isDouble();
	
	/**
     * Returns {@code true} if this tag's value may be converted to an
     * appropriate String.
     * 
     * <p>Works for: boolean, byte, short, int, long, float, double, char.
     */
	boolean isString();
	
	
	/**
	 * Converts if possible and necessary, and returns a boolean representation
	 * of this tag's data.
	 * 
	 * @throws IllegalStateException if this tag's data cannot be converted to
	 * a boolean.
	 */
	boolean getAsBoolean();
	
	/**
     * Converts if possible and necessary, and returns an int representation
     * of this tag's data.
     * 
     * @throws IllegalStateException if this tag's data cannot be converted to
     * an int.
     */
	default int getAsInt() {
	    return (int)getAsLong();
	}
	
	/**
     * Converts if possible and necessary, and returns a long representation
     * of this tag's data.
     * 
     * @throws IllegalStateException if this tag's data cannot be converted to
     * a long.
     */
	long getAsLong();
	
	/**
     * Converts if possible and necessary, and returns a float representation
     * of this tag's data.
     * 
     * @throws IllegalStateException if this tag's data cannot be converted to
     * a float.
     */
	default float getAsFloat() {
	    return (float)getAsDouble();
	}
	
	/**
     * Converts if possible and necessary, and returns a double representation
     * of this tag's data.
     * 
     * @throws IllegalStateException if this tag's data cannot be converted to
     * a double.
     */
	double getAsDouble();
	
	/**
     * Converts if possible and necessary, and returns a String representation
     * of this tag's data.
     * 
     * @throws IllegalStateException if this tag's data cannot be converted to
     * a String.
     */
	String getAsString();
	
}
