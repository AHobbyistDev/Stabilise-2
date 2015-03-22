package com.stabilise.util.collect;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import com.stabilise.util.annotation.NotThreadSafe;

/**
 * An implementation of {@link LightLinkedList} which automatically clears
 * itself when it is iterated over, either through a for...each loop, or when
 * {@link #iterator()} is invoked (though note the former case implies the
 * latter).
 * 
 * <p>Furthermore, invoking {@link #toArray()} or {@link #toArray(Object[])}
 * also clears the list.
 */
@NotThreadSafe
public class ClearingLinkedList<E> extends LightLinkedList<E> {
	
	@Override
	protected AbstractItr getIterator() {
		return new ClearingItr();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method clears the list.
	 */
	@Override
	public Object[] toArray() {
		Object[] arr = new Object[size];
		int i = 0;
		for(E e : this) // wipes this list
			arr[i++] = e;
		return arr;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Invoking this method clears the list.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (a.length < size)
			a = (T[])Array.newInstance(a.getClass().getComponentType(), size);
		int i = 0;
		Object[] result = a;
		int oldSize = size;
		for(E e : this) // wipes this list
			result[i++] = e;
		if (a.length > oldSize)
			a[oldSize] = null;
		return a;
	}
	
	@Override
	public void clear() {
		// Checking first should offer a small performance improvement if this
		// is being used generically and this is invoked after, say, iterator()
		// or toArray().
		if(size != 0)
			super.clear();
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	private class ClearingItr extends AbstractItr {
		
		@Override
		protected void reset() {
			super.reset(); // lastReturned = new Node<E>(head);
			head = tail = null; // effectively wipes the list
			size = 0;
		}
		
		@Override
		public E next() {
			Node<E> prev = lastReturned;
			lastReturned = lastReturned.next;
			prev.wipe();
			if(lastReturned == null)
				throw new NoSuchElementException();
			return lastReturned.e;
		}
		
		@Override
		public void remove() {
			// done already
		}
		
	}
	
}
