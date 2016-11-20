package com.stabilise.util.io.data;

/**
 * Implementors of this interface may import and export themselves to and from
 * a {@link DataCompound}. {@code Exportable}s exist within their own {@code
 * DataObject}; that is, they export their data to a child {@code DataObject}
 * rather than directly to an object or a list.
 */
public interface Exportable {
    
    /**
     * Imports/exports this object to/from the given DataObject.
     * 
     * @param write {@code true} to write this object to {@code o}; {@code
     * false} to read this object from {@code o}.
     */
    void io(DataCompound o, boolean write);
    
}
