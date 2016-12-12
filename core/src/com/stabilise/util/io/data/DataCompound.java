package com.stabilise.util.io.data;

import javaslang.control.Option;

import com.stabilise.util.io.Sendable;

/**
 * A DataCompound is the basic unifying building block for this package. A
 * DataCompound is essentially equivalent to any old object - it encapsulates
 * data, and may be saved in a variety of {@link Format}{@code s}.
 * 
 * <p>There are three primary data-interaction methods for a DataCompound:
 *  
 * <ul>
 * <li>{@code put()} methods. Each of these methods inserts data into this tag;
 *     if data with the specified name already exists, it will be overwritten.
 * <li>{@code get()} methods. Each of these methods get data from this tag. For
 *     each of the {@code get} methods, if data is not present or is present in
 *     a different format (e.g. invoking {@code getBool("foo")} when the data
 *     type of {@code "foo"} is {@code int}), a suitable default will be
 *     returned.
 * <li>{@code opt()} methods. These methods behave like the {@code get()}
 *     methods, but return an {@code Option} instead of a default value.
 * </ul>
 */
public interface DataCompound extends Sendable, IContainerTag<DataCompound> {
    
    /**
     * Creates a DataCompound of the format determined the current thread's
     * default value.
     * 
     * @see Format#getDefaultFormat()
     * @see Format#setDefaultFormat(Format)
     */
    public static DataCompound create() {
        return Format.getDefaultFormat().newCompound();
    }
    
    
    
    /**
     * Checks for whether or not a tag with the specified name is contained
     * within this compound.
     * 
     * <p>This method is meaningless for {@link Format#BYTE_STREAM}.
     */
    boolean contains(String name);
    
    /**
     * Gets a compound which is a child of this one. If a compound by the
     * specified name already exists, it is returned, otherwise one is
     * created. If another data type under the specified name is present,
     * it will be overwritten.
     * 
     * <p>The returned compound will be of the same format as this one.
     */
    DataCompound createCompound(String name);
    
    /**
     * Gets a list which is a child of this compound. If a list by the
     * specified name already exists, it is returned, otherwise one is created.
     * If another data type under the specified name is present, it will be
     * overwritten.
     * 
     * <p>The returned list will be of the same format as this compound.
     */
    DataList createList(String name);
    
    // <----- PUT METHODS ----->
    // Insert the data into this tag; if data with the specified name already
    // exists, it'll be overwritten.
    
    /**
     * If the given compound is of a different format to this one, it will be
     * converted before being added.
     */
    void put(String name, DataCompound data);
    
    /**
     * If the given list is of a different format to this compound, it will be
     * converted before being added.
     */
    void put(String name, DataList     data);
    
    void put(String name, boolean data);
    void put(String name, byte    data);
    void put(String name, char    data);
    void put(String name, double  data);
    void put(String name, float   data);
    void put(String name, int     data);
    void put(String name, long    data);
    void put(String name, short   data);
    void put(String name, String  data);
    void put(String name, byte[]  data);
    void put(String name, int[]   data);
    
    // <----- GET METHODS ----->
    // Gets data from this compound. If data with the specified name is not
    // present or is of another type, suitable defaults are returned.
    
    /**
     * If a compound with the specified name is not present, an empty one is
     * created and returned, but not added to this compound.
     */
    DataCompound getCompound(String name);
    
    /**
     * If a list with the specified name is not present, an empty one is
     * created and returned, but not added to this compound.
     */
    DataList getList(String name);
    
    boolean getBool   (String name);
    byte    getByte   (String name);
    char    getChar   (String name);
    double  getDouble (String name);
    float   getFloat  (String name);
    int     getInt    (String name);
    long    getLong   (String name);
    short   getShort  (String name);
    String  getString (String name);
    byte[]  getByteArr(String name);
    int[]   getIntArr (String name);
    
    // <----- OPTION GETTERS ----->
    // Gets data from this compound. If data with the specified name is
    // present, a Some<T> is returned.
    
    Option<DataCompound> optCompound(String name);
    Option<DataList>     optList    (String name);
    Option<Boolean>      optBool    (String name);
    Option<Byte>         optByte    (String name);
    Option<Character>    optChar    (String name);
    Option<Double>       optDouble  (String name);
    Option<Float>        optFloat   (String name);
    Option<Integer>      optInt     (String name);
    Option<Long>         optLong    (String name);
    Option<Short>        optShort   (String name);
    Option<String>       optString  (String name);
    Option<byte[]>       optByteArr (String name);
    Option<int[]>        optIntArr  (String name);
    
    /**
     * Sets this compound into read mode. In read mode, users can always get
     * data from this compound, and the {@code io} methods will automatically
     * read from the given exportables.
     * 
     * <p>In general, it is not necessary to have a compound in read mode to
     * read data (the only current exception is {@link Format#BYTE_STREAM}),
     * but it's better to play it safe.
     * 
     * @see #io(String, Exportable)
     * @see #io(String, ValueExportable)
     */
    void setReadMode();
    
    /**
     * Sets this compound into write mode. In write mode, can always write data
     * from this compound, and the {@code io} methods will automatically write
     * to the given exportables.
     * 
     * <p>In general, it is not necessary to have a compound in write mode to
     * add to it (the only current exception is {@link Format#BYTE_STREAM}),
     * but it's better to play it safe.
     * 
     * @see #io(String, Exportable)
     * @see #io(String, ValueExportable)
     */
    void setWriteMode();
    
    /**
     * Clones this DataCompound.
     */
    default DataCompound copy() {
        return copy(format());
    }
    
    /**
     * Clones this DataCompound.
     * 
     * @param format The desired format of the clone.
     */
    DataCompound copy(Format format);
    
    /**
     * Wraps this {@code DataCompound} in an {@code ImmutableCompound}, or
     * returns this compound if it is already immutable.
     */
    default ImmutableCompound immutable() {
        return ImmutableCompound.wrap(this);
    }
    
}
