package com.stabilise.util.collect;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;

/**
 * Miscellaneous iterator-based utils.
 */
public class IteratorUtils {
	
	private IteratorUtils() {}
	
	/**
	 * Runs the given predicate for every element in the {@code Iterable},
	 * removing each element for which {@code pred} returns {@code true}.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public static <T> void forEach(Iterable<T> col, Predicate<T> pred) {
		forEach(col.iterator(), pred);
	}
	
	/**
	 * Runs the given predicate for every remaining element in the {@code
	 * Iterator}, removing every element for which {@code pred} returns {@code
	 * true}.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public static <T> void forEach(Iterator<T> i, Predicate<T> pred) {
		Objects.requireNonNull(pred); // fail-fast
		while(i.hasNext()) {
			if(pred.test(i.next()))
				i.remove();
		}
	}
	
	/**
	 * Removes the first item in {@code i} which satisfies the given predicate.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public static <T> void removeFirst(Iterable<T> i, Predicate<T> pred) {
		removeFirst(i.iterator(), pred);
	}
	
	/**
	 * Removes the first item from {@code i} which satisfies the given
	 * predicate.
	 * 
	 * @throws NullPointerException if either argument is null.
	 */
	public static <T> void removeFirst(Iterator<T> i, Predicate<T> pred) {
		Objects.requireNonNull(pred); // fail-fast
		while(i.hasNext()) {
			if(pred.test(i.next())) {
				i.remove();
				return;
			}
		}
	}
	
	/**
	 * Returns an iterator over {@code col} which filters out any null
	 * elements. The returned iterator does not support remove.
	 * 
	 * @throws NullPointerException if col is null.
	 */
	public static <T> Iterator<T> iteratorNullsFiltered(Iterable<T> col) {
		return iteratorNullsFiltered(col.iterator());
	}
	
	/**
	 * Returns an iterator wrapping {@code itr} which filters out any null
	 * elements. The returned iterator does not support remove.
	 * 
	 * @throws NullPointerException if itr is null.
	 */
	public static <T> Iterator<T> iteratorNullsFiltered(Iterator<T> itr) {
		return Iterators.filter(itr, Predicates.notNull());
	}
	
	/**
	 * Wraps the specified Enumeration in an Iterable.
	 * 
	 * @throws NullPointerException if e is null.
	 */
	public static final <T> Iterable<T> toIterable(Enumeration<T> e) {
		return () -> toIterator(e);
	}
	
	/**
	 * Converts the specified Enumeration to an Iterator.
	 * 
	 * @throws NullPointerException if e is null.
	 */
	public static final <T> Iterator<T> toIterator(Enumeration<T> e) {
		Objects.requireNonNull(e);
		
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return e.hasMoreElements();
			}
			@Override
			public T next() {
				return e.nextElement();
			}
		};
	}
	
}
