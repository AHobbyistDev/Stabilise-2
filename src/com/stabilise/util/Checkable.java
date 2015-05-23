package com.stabilise.util;

import com.stabilise.util.collect.CollectionUtils;

/**
 * Defines {@link #updateAndCheck()} and {@link #updateCheckables(Collection)},
 * convenience methods for iterated removal checking on a collection of objects.
 */
public interface Checkable {
	
	/**
	 * Updates this Checkable, and returns {@code true} if it should be
	 * considered 'destroyed' and removed from its owning collection.
	 * 
	 * @return {@code true} if this Checkable is considered destroyed and
	 * should be removed ASAP; {@code false} otherwise.
	 */
	boolean updateAndCheck();
	
	/**
	 * Invokes {@link #updateAndCheck()} on a collection of Checkables,
	 * removing each object that returns {@code true} from the collection.
	 * 
	 * @param c The collection.
	 */
	public static <E extends Checkable> void updateCheckables(Iterable<E> c) {
		CollectionUtils.iterate(c, e -> e.updateAndCheck());
	}
	
}
