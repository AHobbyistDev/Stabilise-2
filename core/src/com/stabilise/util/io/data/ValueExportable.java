package com.stabilise.util.io.data;

/**
 * Implementors of this interface may import and export themselves to and from
 * a {@link DataCompound}.
 * 
 * <p>Unlike, {@code Exportables}, {@code ValueExportables} are not assigned
 * their own {@code DataObject}; that is, they can read and write from a parent
 * object or list without being encapsulated in their own.
 */
public interface ValueExportable {
    
    /**
     * Imports/exports this object to/from the given DataObject.
     * 
     * @param name The name assigned to this ValueExportable by its owner.
     * @param write {@code true} to write this object to {@code o}; {@code
     * false} to read this object from {@code o}.
     */
    void io(String name, DataCompound o, boolean write);
    
    /**
     * Imports/exports this object to/from the given DataList.
     * 
     * @param write {@code true} to write this object to {@code l}; {@code
     * false} to read this object from {@code l}.
     */
    void io(DataList l, boolean write);
    
}
