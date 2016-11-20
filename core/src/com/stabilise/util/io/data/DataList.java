package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * A DataList is a list which may be written to and read from sequentially.
 * Each of the {@code add()} methods append to the end of this list, and
 * each of the {@code get()} methods reads the next element from the list.
 */
public interface DataList extends Sendable, IContainerTag<DataList> {
    
    /**
     * Creates a DataCompound of the format determined the current thread's
     * default value.
     * 
     * @see Format#getDefaultFormat()
     * @see Format#setDefaultFormat(Format)
     */
    public static DataList create() {
        return Format.getDefaultFormat().newList();
    }
    
    
    
    /**
     * Returns the number of elements in this list.
     */
    int size();
    
    /**
     * Returns if another invocation of a {@code get()} method is valid.
     */
    boolean hasNext();
    
    /**
     * Creates a new compound and adds it to this list.
     * 
     * @return The created compound.
     */
    DataCompound createCompound();
    
    /**
     * Creates a new list and adds it to this list.
     * 
     * @return The created list.
     */
    DataList createList();
    
    /** If {@code data} is of a different format to this list, it will be
     * converted first. */
    void add(DataCompound data);
    /** If {@code data} is of a different format to this list, it will be
     * converted first. */
    void add(DataList     data);
    void add(boolean data);
    void add(byte    data);
    void add(char    data);
    void add(double  data);
    void add(float   data);
    void add(int     data);
    void add(long    data);
    void add(short   data);
    void add(String  data);
    void add(byte[]  data);
    void add(int[]   data);
    
    /** Gets the {@code index-th} tag in this list. This method should
     * generally be ignored in favour of the specific getter methods. */
    ITag getTag(int index) throws IndexOutOfBoundsException;
    
    DataCompound getCompound(int index) throws IndexOutOfBoundsException;
    DataList     getList    (int index) throws IndexOutOfBoundsException;
    boolean getBool   (int index) throws IndexOutOfBoundsException;
    byte    getByte   (int index) throws IndexOutOfBoundsException;
    char    getChar   (int index) throws IndexOutOfBoundsException;
    double  getDouble (int index) throws IndexOutOfBoundsException;
    float   getFloat  (int index) throws IndexOutOfBoundsException;
    int     getInt    (int index) throws IndexOutOfBoundsException;
    long    getLong   (int index) throws IndexOutOfBoundsException;
    short   getShort  (int index) throws IndexOutOfBoundsException;
    String  getString (int index) throws IndexOutOfBoundsException;
    byte[]  getByteArr(int index) throws IndexOutOfBoundsException;
    int[]   getIntArr (int index) throws IndexOutOfBoundsException;
    
    DataCompound getCompound();
    DataList     getList();
    boolean getBool   ();
    byte    getByte   ();
    char    getChar   ();
    double  getDouble ();
    float   getFloat  ();
    int     getInt    ();
    long    getLong   ();
    short   getShort  ();
    String  getString ();
    byte[]  getByteArr();
    int[]   getIntArr ();
    
    /**
     * Wraps this {@code DataList} in an {@code ImmutableList}, or returns this
     * list if it is already immutable.
     */
    default ImmutableList immutable() {
        return ImmutableList.wrap(this);
    }
    
}
