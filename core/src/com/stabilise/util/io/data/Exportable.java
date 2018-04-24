package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * Implementors of this interface may import and export themselves to and from
 * a {@link DataCompound}.
 * 
 * <p>This interface is essentially the one-level-of-abstraction-higher variant
 * of the {@link Sendable} interface. Some background: this entire package more
 * or less exists because past me figured that having every little thing be
 * Sendable is an easy way to accidentally let a bug slip in, and so here we
 * are.
 * 
 * @see Sendable
 */
public interface Exportable {
    
    /**
     * Imports this object from the given DataCompound. This method is
     * complementary to {@link #exportToCompound(DataCompound)}.
     * 
     * @param o The compound to write to. Not null.
     */
    void importFromCompound(DataCompound o);
    
    /**
     * Exports this object to the given DataCompound. This method is
     * complementary to {@link #importFromCompound(DataCompound)}.
     * 
     * @param o The compound to read from. Not null.
     */
    void exportToCompound(DataCompound o);
    
}
