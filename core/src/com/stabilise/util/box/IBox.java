package com.stabilise.util.box;

import com.stabilise.util.io.data.DataCompound;
import com.stabilise.util.io.data.DataList;
import com.stabilise.util.io.data.ITag;

/**
 * A unifying marker interface to indicate that a class functions as a box.
 */
public interface IBox extends ITag {
    
    @Override
    default void io(String name, DataCompound o, boolean write) {
        if(write) write(name, o);
        else      read(name, o);
    }
    
    void write(String name, DataCompound o);
    void read (String name, DataCompound o);
    
    @Override
    default void io(DataList l, boolean write) {
        if(write) write(l);
        else      read(l);
    }
    
    void write(DataList l);
    void read (DataList l);
    
}
