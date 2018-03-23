package com.stabilise.util.io.data;

import com.stabilise.util.io.Sendable;

/**
 * Unifying interface for {@link Sendable} and {@code ValueExportable}, which
 * together constitute a Tag.
 */
public interface ITag extends Sendable, ValueExportable {
    
	/**
	 * Returns {@code true} if the given tag holds the same type of data as
	 * this tag.
	 */
	default boolean isSameType(ITag other) {
		return getClass().equals(other.getClass());
	}
	
}
