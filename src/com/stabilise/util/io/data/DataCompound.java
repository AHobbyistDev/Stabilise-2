package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * 
 * @author Adam
 *
 */
public interface DataCompound extends Sendable {
    
    /**
     * Checks for whether or not a compound, list, or data with the specified
     * name is contained within this compound.
     */
    boolean contains(String name);
    
    /**
     * Gets a compound which is a child of this one. If a compound by the
     * specified name already exists, it is returned, otherwise one is
     * created.
     * 
     * @throws NullPointerException if {@code name} is {@code null} and names
     * are not ignored.
     */
    DataCompound getCompound(String name);
    
    /**
     * Gets a list which is a child of this compound. If a list by the
     * specified name already exists, it is returned, otherwise one is created.
     * 
     */
    DataList     getList    (String name);
    
    void io(String name, Exportable data);
    void io(String name, ValueExportable data);
    
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
    
}
