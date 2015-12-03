package com.stabilise.util.box;

import java.io.IOException;

import com.stabilise.util.io.Sendable;
import com.stabilise.util.io.beta.DataObject;
import com.stabilise.util.io.beta.ValueExportable;

/**
 * A unifying marker interface to indicate that a class functions as a box.
 */
public interface IBox extends Sendable, ValueExportable {
    
    @Override
    default void io(String name, DataObject o, boolean write) throws IOException {
        if(write) write(name, o); else read(name, o);
    }
    
    void write(String name, DataObject o) throws IOException;
    void read (String name, DataObject o) throws IOException;
    
}
