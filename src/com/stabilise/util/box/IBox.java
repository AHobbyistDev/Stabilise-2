package com.stabilise.util.box;

import com.stabilise.util.io.Sendable;
import com.stabilise.util.io.beta.DataList;
import com.stabilise.util.io.beta.DataObject;
import com.stabilise.util.io.beta.ValueExportable;

/**
 * A unifying marker interface to indicate that a class functions as a box.
 */
public interface IBox extends Sendable, ValueExportable {
    
    @Override
    default void io(String name, DataObject o, boolean write) {
        if(write) write(name, o); else read(name, o);
    }
    
    void write(String name, DataObject o);
    void read (String name, DataObject o);
    
    @Override
    default void io(DataList l, boolean write) {
        if(write) write(l); else read(l);
    }
    
    void write(DataList l);
    void read (DataList l);
    
}
