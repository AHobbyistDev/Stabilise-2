package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * Implementors of this interface may import and export themselves to and from
 * a {@link DataCompound}. 
 * 
 * <!-- {@code Exportable}s exist within their own {@code
 * DataObject}; that is, they export their data to a child {@code DataObject}
 * rather than directly to an object or a list. -->
 * 
 * <p>One may also refer to {@link ValueExportable}, which is similar to this
 * interface. For most uses, however using this interface is probably
 * preferable.
 * 
 * <p>This interface is essentially the one-level-of-abstraction-higher variant
 * of the {@link Sendable} interface. Some background: this entire package more
 * or less exists because past me figured that having every little thing be
 * Sendable is an easy way to accidentally let a bug slip in, and so here we
 * are.
 * 
 * @see ValueExportable
 * @see Sendable
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
