package com.stabilise.util.io.beta;

public interface ValueExportable {
    
    void io(String name, DataObject o, boolean write);
    void io(DataList o, boolean write);
    
}
