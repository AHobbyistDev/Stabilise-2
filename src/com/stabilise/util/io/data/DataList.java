package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;


public interface DataList extends Sendable {
    
    /**
     * Returns the number of elements in this list.
     */
    int size();
    
    void io(Exportable data);
    void io(ValueExportable data);
    
    DataCompound object();
    DataList   list();
    
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
