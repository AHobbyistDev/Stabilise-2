package com.stabilise.util.io.beta;

import java.io.IOException;

import com.stabilise.util.io.Sendable;


public interface DataObject extends Sendable {
    
    DataObject object(String name);
    DataList   list  (String name);
    
    void io(String name, Exportable data) throws IOException;
    void io(String name, ValueExportable data) throws IOException;
    
    void write(String name, boolean data) throws IOException;
    void write(String name, byte    data) throws IOException;
    void write(String name, char    data) throws IOException;
    void write(String name, double  data) throws IOException;
    void write(String name, float   data) throws IOException;
    void write(String name, int     data) throws IOException;
    void write(String name, long    data) throws IOException;
    void write(String name, short   data) throws IOException;
    void write(String name, String  data) throws IOException;
    void write(String name, byte[]  data) throws IOException;
    void write(String name, int[]   data) throws IOException;
    
    boolean readBool   (String name) throws IOException;
    byte    readByte   (String name) throws IOException;
    char    readChar   (String name) throws IOException;
    double  readDouble (String name) throws IOException;
    float   readFloat  (String name) throws IOException;
    int     readInt    (String name) throws IOException;
    long    readLong   (String name) throws IOException;
    short   readShort  (String name) throws IOException;
    String  readString (String name) throws IOException;
    byte[]  readByteArr(String name) throws IOException;
    int[]   readIntArr (String name) throws IOException;
    
}
