package com.stabilise.util.io.data;


/**
 * Share parent interface for {@link DataCompound} and {@link DataList}.
 */
public interface IContainerTag<T extends IContainerTag<T>> {
    
    /**
     * Returns the format of this tag.
     * 
     * @see Format
     */
    Format format();
    
    /**
     * Converts this tag to the specified format. If this tag is already in the
     * specified format, this compound is returned. The returned
     * compound will be in read mode.
     * 
     * @throws UnsupportedOperationException if this tag is of a format which
     * cannot be converted for whatever reason.
     */
    T convert(Format format);
    
}
