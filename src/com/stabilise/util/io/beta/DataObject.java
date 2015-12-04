package com.stabilise.util.io.beta;

import com.stabilise.util.io.Sendable;


public interface DataObject extends Sendable {
    
    DataObject object(String name);
    DataList   list  (String name);
    
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
