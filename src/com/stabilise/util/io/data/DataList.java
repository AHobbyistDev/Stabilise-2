package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * A DataList is a list which may be written to and read from sequentially.
 * Each of the {@code add()} methods append to the end of this list, and
 * each of the {@code get()} methods reads the next element from the list.
 */
public interface DataList extends Sendable, IContainerTag<DataList> {
    
    /**
     * Returns the number of elements in this list.
     */
    int size();
    
    /**
     * Creates a new compound and adds it to this list.
     */
    DataCompound createCompound();
    
    /**
     * Creates a new list and adds it to this list.
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
    
}
