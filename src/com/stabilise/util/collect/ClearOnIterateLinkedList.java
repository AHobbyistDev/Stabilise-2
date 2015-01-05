package com.stabilise.util.collect;

/**
 * An implementation of {@link LightLinkedList} which automatically clears
 * itself when it is iterated over, either through a for...each loop, or when
 * {@link #iterator()} is invoked (though note the former case implies the
 * latter).
 */
public class ClearOnIterateLinkedList<E> extends LightLinkedList<E> {
	
	@Override
	protected AbstractItr getIterator() {
		return new ClearingItr();
	}
	
	private class ClearingItr extends AbstractItr {
		
		@Override
		protected void reset() {
			super.reset();
			// clear head and tail on reset
			head = tail = null;
		}
		
		@Override
		public E next() {
			// Wipe links when it is returned
			prev = lastReturned;
			lastReturned = prev.next;
			prev.wipe();
			return lastReturned.e;
		}
		
		@Override
		public void remove() {
			// done already in next()
		}
		
	}
	
}
