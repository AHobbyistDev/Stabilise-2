package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * Generic interface for a piece of data, which is anything that can live within
 * a {@link DataCompound} or {@link DataList}.
 */
public interface IData extends Sendable {
	
	enum DataType {
		COMPOUND, LIST,
		BOOL,
		I8, I16, I32, I64,
		F32, F64,
		I8ARR, I32ARR, I64ARR,
		F32ARR, F64ARR,
		STRING
	}
    
    /**
     * Reads data from the given {@code DataCompound}. In pseudocode, this
	 * generically does the following:
	 *
	 * <pre>
	 *     this.set(c.get(name))
	 * </pre>
	 *
	 * This method is provided so that, in the occasional situations in which
	 * you're working with an IData object directly, you can invoke this method
	 * rather than concerning yourself with the type of data. (For example, if
	 * this is an {@code F32Box}, it might be more appealing to use this method
	 * rather than calling {@code box.set(c.getF32(name))}).
     * 
     * @param name The name associated to the data to read.
     * @param c The DataCompound to read from.
     */
    void read(String name, DataCompound c);
    
    /**
     * Writes data to the given {@code DataCompound}. In pseudocode, this
	 * generically does the following:
	 *
	 * <pre>
	 *     c.put(name, this.get())
	 * </pre>
	 *
	 * This method is provided so that, in the occasional situations in which
	 * you're working with an IData object directly, you can invoke this method
	 * rather than concerning yourself with the type of data. (For example, if
	 * this is an {@code F32Box}, it might be more appealing to use this method
	 * rather than calling {@code c.put(name, this.get())}. Granted, unlike
	 * {@link #read(String, DataCompound) read()}, the alternative to this
	 * method isn't so bad thanks to method overloading in that it's {@code
	 * c.put()} and not {@code c.putF32()}.)
     * 
     * @param name The name assigned to this tag. This tag should write its
     * data under this name.
     * @param c The DataCompound to write to.
     */
    void write(String name, DataCompound c);
	
	/**
	 * Reads data from the given {@code DataCompound}. In pseudocode, this
	 * generically does the following:
	 *
	 * <pre>
	 *     this.set(c.get())
	 * </pre>
	 *
	 * This method is provided so that, in the occasional situations in which
	 * you're working with an IData object directly, you can invoke this method
	 * rather than concerning yourself with the type of data. (For example, if
	 * this is an {@code F32Box}, it might be more appealing to use this method
	 * rather than calling {@code box.set(l.getF32())}).
	 *
	 * @param l The DataList to read from.
	 */
    void read(DataList l);
	
	/**
	 * Writes data to the given {@code DataList}. In pseudocode, this
	 * generically does the following:
	 *
	 * <pre>
	 *     c.add(this.get())
	 * </pre>
	 *
	 * This method is provided so that, in the occasional situations in which
	 * you're working with an IData object directly, you can invoke this method
	 * rather than concerning yourself with the type of data. (For example, if
	 * this is an {@code F32Box}, it might be more appealing to use this method
	 * rather than calling {@code l.add(this.get())}. Granted, unlike {@link
	 * #read(DataList) read()}, the alternative to this method isn't so bad
	 * thanks to method overloading in that it's {@code l.add()} and not {@code
	 * l.addF32()}.)
	 *
	 * @param l The DataList to write to.
	 */
    void write(DataList l);
	
	/**
	 * Returns the {@link DataType} of the contained data.
	 */
	DataType type();
	
	/**
	 * Returns {@code this.type() == type}.
	 */
	default boolean isType(DataType type) {
		return this.type().equals(type);
	}
 
	/**
	 * Returns {@code true} if the given IData object holds the same type of
	 * data as this one. Equivalent to {@code this.type() == other.type()}.
	 *
	 * @throws NullPointerException if {@code other} is {@code null}.
	 */
	default boolean isSameTypeAs(IData other) {
		return type().equals(other.type());
	}
	
	// We need some methods to be able to test and convert between compatible
	// types to accommodate plaintext data formats such as JSON, in which it is
	// ambiguous as to the type of e.g. a numerical value.
	
	/**
	 * Returns {@code true} if the contained data if either of the type
	 * specified by {@code type}, or if it's of a type which may be converted to
	 * {@code type}. This is a weaker condition than {@link #isType(DataType)}.
	 */
	boolean canConvertToType(DataType type);
	
	/**
	 * Returns {@code true} if the contained data is of the same type as {@code
	 * other}'s, or of a type which may converted to the same type as {@code
	 * other}'s. This is a weaker condition than {@link #isSameTypeAs(IData)}.
	 *
	 * @throws NullPointerException if {@code other} is {@code null}.
	 */
	default boolean canConvertToTypeOf(IData other) {
		return canConvertToType(other.type());
	}
	
	/**
	 * Converts the contained data to the specified type and returns it. The
	 * returned object will be newly-created, even if the {@code type} parameter
	 * matches this {@code IData}'s {@link #type type}.
	 *
	 * @throws RuntimeException if the contained data can not be converted to
	 * {@code type}. Make sure you check {@link #canConvertToType(DataType)
	 * canConvertToType()} first!
	 */
	IData convertToType(DataType type);
    
    /**
     * Converts the given tag to a tag of this type.
     * 
     * @throws RuntimeException if the contained data can not be converted to
	 * the type of {@code other}. Make sure you check {@link
	 * #canConvertToTypeOf(IData)} first!
     */
    default IData convertToTypeOf(IData other) {
    	return convertToType(other.type());
	}
	
	/**
	 * Returns a clone of this data object. The clone is deep-copied, which
	 * means that later modifications to this won't affect the clone, and
	 * vice-versa (this is mainly pertinent to Compounds and Lists).
	 */
	// Gotta name this duplicate() instead of clone() to get around annoying
	// things that can happen due to clone() already being defined in the
	// Object superclass. Thanks, Java.
	IData duplicate();
	
}
