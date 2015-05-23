package com.stabilise.util.collect;

import java.util.Iterator;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * Miscellaneous collection-based utils.
 */
public class CollectionUtils {
	
	private CollectionUtils() {}
	
	/**
	 * Iterates over the specified collection, removing any element for which
	 * the specified predicate returns true.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public static <T> void iterate(Iterable<T> col, Predicate<T> pred) {
		Iterator<T> i = col.iterator();
		while(i.hasNext()) {
			if(pred.test(i.next()))
				i.remove();
		}
	}
	
	/**
	 * Returns an iterator over {@code col} which filters out any null
	 * elements. The returned iterator does not support remove.
	 */
	public static <T> Iterator<T> iteratorNullsFiltered(Iterable<T> col) {
		return iteratorNullsFiltered(col.iterator());
	}
	
	/**
	 * Returns an iterator wrapping {@code ire} which filters out any null
	 * elements. The returned iterator does not support remove.
	 */
	public static <T> Iterator<T> iteratorNullsFiltered(Iterator<T> itr) {
		return Iterators.filter(itr, Predicates.notNull());
	}
	
}
