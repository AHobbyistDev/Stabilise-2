/*
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.stabilise.util.concurrent;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import com.stabilise.util.BiIntFunction;
import com.stabilise.util.TheUnsafe;

/**
 * A variant of {@link java.util.concurrent.ConcurrentHashMap} which uses two
 * integers as keys (instead of some object).
 * 
 * <p>This class is more or less the exact same as ConcurrentHashMap, except
 * I've stripped away much of the superfluous stuff, and (hopefully) properly
 * adapted all of its functions.
 */
public class ConcurrentBiIntHashMap<V> implements Iterable<V> {
	
	/* ---------------- Constants -------------- */

	/**
	 * The largest possible table capacity.  This value must be
	 * exactly 1<<30 to stay within Java array allocation and indexing
	 * bounds for power of two table sizes, and is further required
	 * because the top two bits of 32bit hash fields are used for
	 * control purposes.
	 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * The default initial table capacity.  Must be a power of 2
	 * (i.e., at least 1) and at most MAXIMUM_CAPACITY.
	 */
	private static final int DEFAULT_CAPACITY = 16;

	/**
	 * The largest possible (non-power of two) array size.
	 * Needed by toArray and related methods.
	 */
	static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	/**
	 * The load factor for this table. Overrides of this value in
	 * constructors affect only the initial table capacity.  The
	 * actual floating point value isn't normally used -- it is
	 * simpler to use expressions such as {@code n - (n >>> 2)} for
	 * the associated resizing threshold.
	 */
	@SuppressWarnings("unused")
	private static final float LOAD_FACTOR = 0.75f;

	/**
	 * The bin count threshold for using a tree rather than list for a
	 * bin.  Bins are converted to trees when adding an element to a
	 * bin with at least this many nodes. The value must be greater
	 * than 2, and should be at least 8 to mesh with assumptions in
	 * tree removal about conversion back to plain bins upon
	 * shrinkage.
	 */
	static final int TREEIFY_THRESHOLD = 8;

	/**
	 * The bin count threshold for untreeifying a (split) bin during a
	 * resize operation. Should be less than TREEIFY_THRESHOLD, and at
	 * most 6 to mesh with shrinkage detection under removal.
	 */
	static final int UNTREEIFY_THRESHOLD = 6;

	/**
	 * The smallest table capacity for which bins may be treeified.
	 * (Otherwise the table is resized if too many nodes in a bin.)
	 * The value should be at least 4 * TREEIFY_THRESHOLD to avoid
	 * conflicts between resizing and treeification thresholds.
	 */
	static final int MIN_TREEIFY_CAPACITY = 64;

	/**
	 * Minimum number of rebinnings per transfer step. Ranges are
	 * subdivided to allow multiple resizer threads.  This value
	 * serves as a lower bound to avoid resizers encountering
	 * excessive memory contention.  The value should be at least
	 * DEFAULT_CAPACITY.
	 */
	private static final int MIN_TRANSFER_STRIDE = 16;

	/**
	 * The number of bits used for generation stamp in sizeCtl.
	 * Must be at least 6 for 32bit arrays.
	 */
	private static int RESIZE_STAMP_BITS = 16;

	/**
	 * The maximum number of threads that can help resize.
	 * Must fit in 32 - RESIZE_STAMP_BITS bits.
	 */
	private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;

	/**
	 * The bit shift for recording size stamp in sizeCtl.
	 */
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;

	/*
	 * Encodings for Node hash fields. See above for explanation.
	 */
	static final int MOVED	 = -1; // hash for forwarding nodes
	static final int TREEBIN   = -2; // hash for roots of trees
	static final int RESERVED  = -3; // hash for transient reservations
	static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash

	/** Number of CPUS, to place bounds on some sizings */
	static final int NCPU = Runtime.getRuntime().availableProcessors();

	/* ---------------- Nodes -------------- */
	
	public static interface Entry<V> {
		int getKeyX();
		int getKeyY();
		V getValue();
	}
	
	/**
	 * Key-value entry.  This class is never exported out as a
	 * user-mutable Entry (i.e., one supporting setValue; see
	 * MapEntry below), but can be used for read-only traversals used
	 * in bulk tasks.  Subclasses of Node with a negative hash field
	 * are special, and contain null keys and values (but are never
	 * exported).  Otherwise, keys and vals are never null.
	 */
	static class Node<V> implements Entry<V> {
		final int hash;
		final int keyX, keyY;
		volatile V val;
		volatile Node<V> next;

		Node(int hash, int keyX, int keyY, V val, Node<V> next) {
			this.hash = hash;
			this.keyX = keyX;
			this.keyY = keyY;
			this.val = val;
			this.next = next;
		}

		public final int getKeyX()	   { return keyX; }
		public final int getKeyY()	   { return keyY; }
		public final V getValue()	 { return val; }
		public final int hashCode()   { return hash ^ val.hashCode(); }
		public final String toString(){ return "(" + keyX + "," + keyY + ")=" + val; }

		public final boolean equals(Object o) {
			if(!(o instanceof Node)) return false;
			Node<?> n = (Node<?>)o;
			return keyX == n.keyX && keyY == n.keyY && val != null && val.equals(n.val);
		}

		/**
		 * Virtualized support for map.get(); overridden in subclasses.
		 * 
		 * @param h the hash
		 * @param kX keyX
		 * @param kY keyY
		 */
		Node<V> find(int h, int kX, int kY) {
			Node<V> e = this;
			do {
				if(e.hash == h && e.keyX == kX && e.keyY == kY)
					return e;
			} while ((e = e.next) != null);
			return null;
		}
	}

	/* ---------------- Static utilities -------------- */

	/**
	 * Spreads (XORs) higher bits of hash to lower and also forces top
	 * bit to 0. Because the table uses power-of-two masking, sets of
	 * hashes that vary only in bits above the current mask will
	 * always collide. (Among known examples are sets of Float keys
	 * holding consecutive whole numbers in small tables.)  So we
	 * apply a transform that spreads the impact of higher bits
	 * downward. There is a tradeoff between speed, utility, and
	 * quality of bit-spreading. Because many common sets of hashes
	 * are already reasonably distributed (so don't benefit from
	 * spreading), and because we use trees to handle large sets of
	 * collisions in bins, we just XOR some shifted bits in the
	 * cheapest possible way to reduce systematic lossage, as well as
	 * to incorporate impact of the highest bits that would otherwise
	 * never be used in index calculations because of table bounds.
	 */
	final int hash(int x, int y) {
		x = hasher.apply(x, y); // store result in x
		return (x ^ (x >>> 16)) & HASH_BITS;
	}

	/**
	 * Returns a power of two table size for the given desired capacity.
	 * See Hackers Delight, sec 3.2
	 */
	private static final int tableSizeFor(int c) {
		int n = c - 1;
		n |= n >>> 1;
		n |= n >>> 2;
		n |= n >>> 4;
		n |= n >>> 8;
		n |= n >>> 16;
		return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
	}

	/**
	 * Returns x's Class if it is of the form "class C implements
	 * Comparable<C>", else null.
	 */
	static Class<?> comparableClassFor(Object x) {
		if (x instanceof Comparable) {
			Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
			if ((c = x.getClass()) == String.class) // bypass checks
				return c;
			if ((ts = c.getGenericInterfaces()) != null) {
				for (int i = 0; i < ts.length; ++i) {
					if (((t = ts[i]) instanceof ParameterizedType) &&
						((p = (ParameterizedType)t).getRawType() ==
						 Comparable.class) &&
						(as = p.getActualTypeArguments()) != null &&
						as.length == 1 && as[0] == c) // type arg is c
						return c;
				}
			}
		}
		return null;
	}

	/**
	 * Formerly compared classes implementing Comparable; now compares key
	 * pairs.
	 */
	static int compareComparables(int kx1, int ky1, int kx2, int ky2) {
		if(kx1 > kx2) return 1;
		if(kx1 < kx2) return -1;
		if(ky1 > ky2) return 1;
		if(ky1 < ky2) return -1;
		return 0;
	}

	/* ---------------- Table element access -------------- */

	/*
	 * Volatile access methods are used for table elements as well as
	 * elements of in-progress next table while resizing.  All uses of
	 * the tab arguments must be null checked by callers.  All callers
	 * also paranoically precheck that tab's length is not zero (or an
	 * equivalent check), thus ensuring that any index argument taking
	 * the form of a hash value anded with (length - 1) is a valid
	 * index.  Note that, to be correct wrt arbitrary concurrency
	 * errors by users, these checks must operate on local variables,
	 * which accounts for some odd-looking inline assignments below.
	 * Note that calls to setTabAt always occur within locked regions,
	 * and so in principle require only release ordering, not
	 * full volatile semantics, but are currently coded as volatile
	 * writes to be conservative.
	 */

	@SuppressWarnings("unchecked")
	static final <V> Node<V> tabAt(Node<V>[] tab, int i) {
		return (Node<V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
	}

	static final <V> boolean casTabAt(Node<V>[] tab, int i,
										Node<V> c, Node<V> v) {
		return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
	}

	static final <V> void setTabAt(Node<V>[] tab, int i, Node<V> v) {
		U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
	}

	/* ---------------- Fields -------------- */

	/**
	 * The array of bins. Lazily initialized upon first insertion.
	 * Size is always a power of two. Accessed directly by iterators.
	 */
	transient volatile Node<V>[] table;

	/**
	 * The next table to use; non-null only while resizing.
	 */
	private transient volatile Node<V>[] nextTable;

	/**
	 * Base counter value, used mainly when there is no contention,
	 * but also as a fallback during table initialization
	 * races. Updated via CAS.
	 */
	private transient volatile long baseCount;

	/**
	 * Table initialization and resizing control.  When negative, the
	 * table is being initialized or resized: -1 for initialization,
	 * else -(1 + the number of active resizing threads).  Otherwise,
	 * when table is null, holds the initial table size to use upon
	 * creation, or 0 for default. After initialization, holds the
	 * next element count value upon which to resize the table.
	 */
	private transient volatile int sizeCtl;

	/**
	 * The next table index (plus one) to split while resizing.
	 */
	private transient volatile int transferIndex;

	/**
	 * Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	 */
	private transient volatile int cellsBusy;

	/**
	 * Table of counter cells. When non-null, size is a power of 2.
	 */
	private transient volatile CounterCell[] counterCells;
	
	/**
	 * The function to use to hash the incoming integer keys.
	 */
	private final BiIntFunction hasher;

	// views
	private transient ValuesView<V> values;
	private transient EntrySetView<V> entrySet;


	/* ---------------- Public operations -------------- */

	/**
	 * Creates a new, empty map with the default initial table size (16).
	 * 
	 * @param hasher The function to use to hash the integer keys.
	 * 
	 * @throws NullPointerException if {@code hasher} is {@code null}.
	 */
	public ConcurrentBiIntHashMap(BiIntFunction hasher) {
		this.hasher = Objects.requireNonNull(hasher);
	}

	/**
	 * Creates a new, empty map with an initial table size
	 * accommodating the specified number of elements without the need
	 * to dynamically resize.
	 *
	 * @param initialCapacity The implementation performs internal
	 * sizing to accommodate this many elements.
	 * @throws IllegalArgumentException if the initial capacity of
	 * elements is negative
	 */
	public ConcurrentBiIntHashMap(int initialCapacity, BiIntFunction hasher) {
		this(hasher);
		if (initialCapacity < 0)
			throw new IllegalArgumentException();
		int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
				   MAXIMUM_CAPACITY :
				   tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
		this.sizeCtl = cap;
	}

	/**
	 * Creates a new, empty map with an initial table size based on
	 * the given number of elements ({@code initialCapacity}) and
	 * initial table density ({@code loadFactor}).
	 *
	 * @param initialCapacity the initial capacity. The implementation
	 * performs internal sizing to accommodate this many elements,
	 * given the specified load factor.
	 * @param loadFactor the load factor (table density) for
	 * establishing the initial table size
	 * @throws IllegalArgumentException if the initial capacity of
	 * elements is negative or the load factor is nonpositive
	 *
	 * @since 1.6
	 */
	public ConcurrentBiIntHashMap(int initialCapacity, float loadFactor,
			BiIntFunction hasher) {
		this(initialCapacity, loadFactor, 1, hasher);
	}

	/**
	 * Creates a new, empty map with an initial table size based on
	 * the given number of elements ({@code initialCapacity}), table
	 * density ({@code loadFactor}), and number of concurrently
	 * updating threads ({@code concurrencyLevel}).
	 *
	 * @param initialCapacity the initial capacity. The implementation
	 * performs internal sizing to accommodate this many elements,
	 * given the specified load factor.
	 * @param loadFactor the load factor (table density) for
	 * establishing the initial table size
	 * @param concurrencyLevel the estimated number of concurrently
	 * updating threads. The implementation may use this value as
	 * a sizing hint.
	 * @throws IllegalArgumentException if the initial capacity is
	 * negative or the load factor or concurrencyLevel are
	 * nonpositive
	 */
	public ConcurrentBiIntHashMap(int initialCapacity, float loadFactor,
			int concurrencyLevel, BiIntFunction hasher) {
		this(hasher);
		if (!(loadFactor > 0.0f) || initialCapacity < 0 || concurrencyLevel <= 0)
			throw new IllegalArgumentException();
		if (initialCapacity < concurrencyLevel)   // Use at least as many bins
			initialCapacity = concurrencyLevel;   // as estimated threads
		long size = (long)(1.0 + (long)initialCapacity / loadFactor);
		int cap = (size >= (long)MAXIMUM_CAPACITY) ?
			MAXIMUM_CAPACITY : tableSizeFor((int)size);
		this.sizeCtl = cap;
	}

	// Original (since JDK1.2) Map methods

	/**
	 * Returns the number of mappings in this map. This method is not very
	 * useful as it is expected that size() will fluctuate rapidly.
	 */
	public int size() {
		long n = sumCount();
		return ((n < 0L) ? 0 :
				(n > (long)Integer.MAX_VALUE) ? Integer.MAX_VALUE :
				(int)n);
	}

	/**
	 * Returns {@code true} if this map is empty.
	 */
	public boolean isEmpty() {
		return sumCount() <= 0L; // ignore transient negative values
	}

	/**
	 * Returns the value to which the specified key is mapped,
	 * or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that {@code key.equals(k)},
	 * then this method returns {@code v}; otherwise it returns
	 * {@code null}.  (There can be at most one such mapping.)
	 */
	public V get(int keyX, int keyY) {
		Node<V>[] tab; Node<V> e, p; int n, eh;
		int h = hash(keyX, keyY);
		if ((tab = table) != null && (n = tab.length) > 0 &&
			(e = tabAt(tab, (n - 1) & h)) != null) {
			if ((eh = e.hash) == h) {
				if (e.keyX == keyX && e.keyY == keyY)
					return e.val;
			}
			else if (eh < 0)
				return (p = e.find(h, keyX, keyY)) != null ? p.val : null;
			while ((e = e.next) != null) {
				if (e.hash == h && e.keyX == keyX && e.keyY == keyY)
					return e.val;
			}
		}
		return null;
	}

	/**
	 * Tests if the specified object is a key in this table.
	 *
	 * @param  key possible key
	 * @return {@code true} if and only if the specified object
	 *		 is a key in this table, as determined by the
	 *		 {@code equals} method; {@code false} otherwise
	 * @throws NullPointerException if the specified key is null
	 */
	public boolean containsKey(int keyX, int keyY) {
		return get(keyX, keyY) != null;
	}

	/**
	 * Returns {@code true} if this map maps one or more keys to the
	 * specified value. Note: This method may require a full traversal
	 * of the map, and is much slower than method {@code containsKey}.
	 *
	 * @param value value whose presence in this map is to be tested
	 * @return {@code true} if this map maps one or more keys to the
	 *		 specified value
	 * @throws NullPointerException if the specified value is null
	 */
	public boolean containsValue(Object value) {
		if (value == null)
			throw new NullPointerException();
		Node<V>[] t;
		if ((t = table) != null) {
			Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length);
			for (Node<V> p; (p = it.advance()) != null; ) {
				V v;
				if ((v = p.val) == value || (v != null && value.equals(v)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Maps the specified key to the specified value in this table.
	 * Neither the key nor the value can be null.
	 *
	 * <p>The value can be retrieved by calling the {@code get} method
	 * with a key that is equal to the original key.
	 *
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the previous value associated with {@code key}, or
	 *		 {@code null} if there was no mapping for {@code key}
	 * @throws NullPointerException if the specified key or value is null
	 */
	public V put(int keyX, int keyY, V value) {
		return putVal(keyX, keyY, value, false);
	}

	/** Implementation for put and putIfAbsent */
	final V putVal(int keyX, int keyY, V value, boolean onlyIfAbsent) {
		if (value == null) throw new NullPointerException();
		int hash = hash(keyX, keyY);
		int binCount = 0;
		for (Node<V>[] tab = table;;) {
			Node<V> f; int n, i, fh;
			if (tab == null || (n = tab.length) == 0)
				tab = initTable();
			else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
				if (casTabAt(tab, i, null,
							 new Node<V>(hash, keyX, keyY, value, null)))
					break;				   // no lock when adding to empty bin
			}
			else if ((fh = f.hash) == MOVED)
				tab = helpTransfer(tab, f);
			else {
				V oldVal = null;
				synchronized (f) {
					if (tabAt(tab, i) == f) {
						if (fh >= 0) {
							binCount = 1;
							for (Node<V> e = f;; ++binCount) {
								if (e.hash == hash &&
									e.keyX == keyX && e.keyY == keyY) {
									oldVal = e.val;
									if (!onlyIfAbsent)
										e.val = value;
									break;
								}
								Node<V> pred = e;
								if ((e = e.next) == null) {
									pred.next = new Node<V>(hash, keyX, keyY,
															  value, null);
									break;
								}
							}
						}
						else if (f instanceof TreeBin) {
							Node<V> p;
							binCount = 2;
							if ((p = ((TreeBin<V>)f).putTreeVal(hash, keyX, keyY,
														   value)) != null) {
								oldVal = p.val;
								if (!onlyIfAbsent)
									p.val = value;
							}
						}
					}
				}
				if (binCount != 0) {
					if (binCount >= TREEIFY_THRESHOLD)
						treeifyBin(tab, i);
					if (oldVal != null)
						return oldVal;
					break;
				}
			}
		}
		addCount(1L, binCount);
		return null;
	}

	/**
	 * Removes the key (and its corresponding value) from this map.
	 * This method does nothing if the key is not in the map.
	 *
	 * @param  key the key that needs to be removed
	 * @return the previous value associated with {@code key}, or
	 *		 {@code null} if there was no mapping for {@code key}
	 * @throws NullPointerException if the specified key is null
	 */
	public V remove(int keyX, int keyY) {
		return replaceNode(keyX, keyY, null, null);
	}

	/**
	 * Implementation for the four public remove/replace methods:
	 * Replaces node value with v, conditional upon match of cv if
	 * non-null.  If resulting value is null, delete.
	 */
	final V replaceNode(int keyX, int keyY, V value, Object cv) {
		int hash = hash(keyX, keyY);
		for (Node<V>[] tab = table;;) {
			Node<V> f; int n, i, fh;
			if (tab == null || (n = tab.length) == 0 ||
				(f = tabAt(tab, i = (n - 1) & hash)) == null)
				break;
			else if ((fh = f.hash) == MOVED)
				tab = helpTransfer(tab, f);
			else {
				V oldVal = null;
				boolean validated = false;
				synchronized (f) {
					if (tabAt(tab, i) == f) {
						if (fh >= 0) {
							validated = true;
							for (Node<V> e = f, pred = null;;) {
								if (e.hash == hash && e.keyX == keyX && e.keyY == keyY) {
									V ev = e.val;
									if (cv == null || cv == ev ||
										(ev != null && cv.equals(ev))) {
										oldVal = ev;
										if (value != null)
											e.val = value;
										else if (pred != null)
											pred.next = e.next;
										else
											setTabAt(tab, i, e.next);
									}
									break;
								}
								pred = e;
								if ((e = e.next) == null)
									break;
							}
						}
						else if (f instanceof TreeBin) {
							validated = true;
							TreeBin<V> t = (TreeBin<V>)f;
							TreeNode<V> r, p;
							if ((r = t.root) != null &&
								(p = r.findTreeNode(hash, keyX, keyY)) != null) {
								V pv = p.val;
								if (cv == null || cv == pv ||
									(pv != null && cv.equals(pv))) {
									oldVal = pv;
									if (value != null)
										p.val = value;
									else if (t.removeTreeNode(p))
										setTabAt(tab, i, untreeify(t.first));
								}
							}
						}
					}
				}
				if (validated) {
					if (oldVal != null) {
						if (value == null)
							addCount(-1L, -1);
						return oldVal;
					}
					break;
				}
			}
		}
		return null;
	}

	/**
	 * Removes all of the mappings from this map.
	 */
	public void clear() {
		long delta = 0L; // negative number of deletions
		int i = 0;
		Node<V>[] tab = table;
		while (tab != null && i < tab.length) {
			int fh;
			Node<V> f = tabAt(tab, i);
			if (f == null)
				++i;
			else if ((fh = f.hash) == MOVED) {
				tab = helpTransfer(tab, f);
				i = 0; // restart
			}
			else {
				synchronized (f) {
					if (tabAt(tab, i) == f) {
						Node<V> p = (fh >= 0 ? f :
									   (f instanceof TreeBin) ?
									   ((TreeBin<V>)f).first : null);
						while (p != null) {
							--delta;
							p = p.next;
						}
						setTabAt(tab, i++, null);
					}
				}
			}
		}
		if (delta != 0L)
			addCount(delta, -1);
	}
	
	@Override
	public Iterator<V> iterator() {
		return values().iterator();
	}

	/**
	 * Returns a {@link Collection} view of the values contained in this map.
	 * The collection is backed by the map, so changes to the map are
	 * reflected in the collection, and vice-versa.  The collection
	 * supports element removal, which removes the corresponding
	 * mapping from this map, via the {@code Iterator.remove},
	 * {@code Collection.remove}, {@code removeAll},
	 * {@code retainAll}, and {@code clear} operations.  It does not
	 * support the {@code add} or {@code addAll} operations.
	 *
	 * <p>The view's iterators and spliterators are
	 * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
	 *
	 * <p>The view's {@code spliterator} reports {@link Spliterator#CONCURRENT}
	 * and {@link Spliterator#NONNULL}.
	 *
	 * @return the collection view
	 */
	public Collection<V> values() {
		ValuesView<V> vs;
		return (vs = values) != null ? vs : (values = new ValuesView<V>(this));
	}

	/**
	 * Returns a {@link Set} view of the mappings contained in this map.
	 * The set is backed by the map, so changes to the map are
	 * reflected in the set, and vice-versa.  The set supports element
	 * removal, which removes the corresponding mapping from the map,
	 * via the {@code Iterator.remove}, {@code Set.remove},
	 * {@code removeAll}, {@code retainAll}, and {@code clear}
	 * operations.
	 *
	 * <p>The view's iterators and spliterators are
	 * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
	 *
	 * <p>The view's {@code spliterator} reports {@link Spliterator#CONCURRENT},
	 * {@link Spliterator#DISTINCT}, and {@link Spliterator#NONNULL}.
	 *
	 * @return the set view
	 */
	public Set<Entry<V>> entrySet() {
		EntrySetView<V> es;
		return (es = entrySet) != null ? es : (entrySet = new EntrySetView<V>(this));
	}

	/**
	 * Returns the hash code value for this {@link Map}, i.e.,
	 * the sum of, for each key-value pair in the map,
	 * {@code key.hashCode() ^ value.hashCode()}.
	 *
	 * @return the hash code value for this map
	 */
	public int hashCode() {
		int h = 0;
		Node<V>[] t;
		if ((t = table) != null) {
			Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length);
			for (Node<V> p; (p = it.advance()) != null; )
				h += p.hash ^ p.val.hashCode();
		}
		return h;
	}

	/**
	 * Returns a string representation of this map.  The string
	 * representation consists of a list of key-value mappings (in no
	 * particular order) enclosed in braces ("{@code {}}").  Adjacent
	 * mappings are separated by the characters {@code ", "} (comma
	 * and space).  Each key-value mapping is rendered as the key
	 * followed by an equals sign ("{@code =}") followed by the
	 * associated value.
	 *
	 * @return a string representation of this map
	 */
	public String toString() {
		Node<V>[] t;
		int f = (t = table) == null ? 0 : t.length;
		Traverser<V> it = new Traverser<V>(t, f, 0, f);
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		Node<V> p;
		if ((p = it.advance()) != null) {
			for (;;) {
				V v = p.val;
				sb.append('(');
				sb.append(p.keyX);
				sb.append(',');
				sb.append(p.keyY);
				sb.append(')');
				sb.append('=');
				sb.append(v == this ? "(this Map)" : v);
				if ((p = it.advance()) == null)
					break;
				sb.append(',').append(' ');
			}
		}
		return sb.append('}').toString();
	}

	/**
	 * Stripped-down version of helper class used in previous version,
	 * declared for the sake of serialization compatibility
	 */
	static class Segment<V> extends ReentrantLock implements Serializable {
		private static final long serialVersionUID = 2249069246763182397L;
		final float loadFactor;
		Segment(float lf) { this.loadFactor = lf; }
	}
	
	// ConcurrentMap methods

	/**
	 * {@inheritDoc}
	 *
	 * @return the previous value associated with the specified key,
	 *		 or {@code null} if there was no mapping for the key
	 * @throws NullPointerException if the specified key or value is null
	 */
	public V putIfAbsent(int keyX, int keyY, V value) {
		return putVal(keyX, keyY, value, true);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException if the specified key is null
	 */
	public boolean remove(int keyX, int keyY, Object value) {
		return value != null && replaceNode(keyX, keyY, null, value) != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws NullPointerException if any of the arguments are null
	 */
	public boolean replace(int keyX, int keyY, V oldValue, V newValue) {
		if (oldValue == null || newValue == null)
			throw new NullPointerException();
		return replaceNode(keyX, keyY, newValue, oldValue) != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the previous value associated with the specified key,
	 *		 or {@code null} if there was no mapping for the key
	 * @throws NullPointerException if the specified key or value is null
	 */
	public V replace(int keyX, int keyY, V value) {
		return replaceNode(keyX, keyY, Objects.requireNonNull(value), null);
	}

	// Overrides of JDK8+ Map extension method defaults

	/**
	 * Returns the value to which the specified key is mapped, or the
	 * given default value if this map contains no mapping for the
	 * key.
	 *
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the value to return if this map contains
	 * no mapping for the given key
	 * @return the mapping for the key, if present; else the default value
	 * @throws NullPointerException if the specified key is null
	 */
	public V getOrDefault(int keyX, int keyY, V defaultValue) {
		V v;
		return (v = get(keyX, keyY)) == null ? defaultValue : v;
	}

	// ConcurrentBiIntHashMap-only methods

	/**
	 * Returns the number of mappings. This method should be used
	 * instead of {@link #size} because a ConcurrentBiIntHashMap may
	 * contain more mappings than can be represented as an int. The
	 * value returned is an estimate; the actual count may differ if
	 * there are concurrent insertions or removals.
	 *
	 * @return the number of mappings
	 * @since 1.8
	 */
	public long mappingCount() {
		long n = sumCount();
		return (n < 0L) ? 0L : n; // ignore transient negative values
	}

	/* ---------------- Special Nodes -------------- */

	/**
	 * A node inserted at head of bins during transfer operations.
	 */
	static final class ForwardingNode<V> extends Node<V> {
		final Node<V>[] nextTable;
		ForwardingNode(Node<V>[] tab) {
			super(MOVED, 0, 0, null, null);
			this.nextTable = tab;
		}
		
		@Override
		Node<V> find(int h, int kx, int ky) {
			// loop to avoid arbitrarily deep recursion on forwarding nodes
			outer: for (Node<V>[] tab = nextTable;;) {
				Node<V> e; int n;
				if (tab == null || (n = tab.length) == 0 ||
					(e = tabAt(tab, (n - 1) & h)) == null)
					return null;
				for (;;) {
					int eh;
					if ((eh = e.hash) == h && e.keyX == kx && e.keyY == ky)
						return e;
					if (eh < 0) {
						if (e instanceof ForwardingNode) {
							tab = ((ForwardingNode<V>)e).nextTable;
							continue outer;
						}
						else
							return e.find(h, kx, ky);
					}
					if ((e = e.next) == null)
						return null;
				}
			}
		}
	}

	/**
	 * A place-holder node used in computeIfAbsent and compute
	 */
	static final class ReservationNode<V> extends Node<V> {
		ReservationNode() {
			super(RESERVED, 0, 0, null, null);
		}

		Node<V> find(int h, Object k) {
			return null;
		}
	}

	/* ---------------- Table Initialization and Resizing -------------- */

	/**
	 * Returns the stamp bits for resizing a table of size n.
	 * Must be negative when shifted left by RESIZE_STAMP_SHIFT.
	 */
	static final int resizeStamp(int n) {
		return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1));
	}

	/**
	 * Initializes table, using the size recorded in sizeCtl.
	 */
	private final Node<V>[] initTable() {
		Node<V>[] tab; int sc;
		while ((tab = table) == null || tab.length == 0) {
			if ((sc = sizeCtl) < 0)
				Thread.yield(); // lost initialization race; just spin
			else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
				try {
					if ((tab = table) == null || tab.length == 0) {
						int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
						@SuppressWarnings("unchecked")
						Node<V>[] nt = (Node<V>[])new Node<?>[n];
						table = tab = nt;
						sc = n - (n >>> 2);
					}
				} finally {
					sizeCtl = sc;
				}
				break;
			}
		}
		return tab;
	}

	/**
	 * Adds to count, and if table is too small and not already
	 * resizing, initiates transfer. If already resizing, helps
	 * perform transfer if work is available.  Rechecks occupancy
	 * after a transfer to see if another resize is already needed
	 * because resizings are lagging additions.
	 *
	 * @param x the count to add
	 * @param check if <0, don't check resize, if <= 1 only check if uncontended
	 */
	private final void addCount(long x, int check) {
		///*
		CounterCell[] as; long b, s;
		if ((as = counterCells) != null ||
			!U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
			CounterCell a; long v; int m;
			boolean uncontended = true;
			if (as == null || (m = as.length - 1) < 0 ||
				(a = as[getThreadLocalRandomProbe() & m]) == null ||
				!(uncontended =
				  U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
				fullAddCount(x, uncontended);
				return;
			}
			if (check <= 1)
				return;
			s = sumCount();
		}
		if (check >= 0) {
			Node<V>[] tab, nt; int n, sc;
			while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
				   (n = tab.length) < MAXIMUM_CAPACITY) {
				int rs = resizeStamp(n);
				if (sc < 0) {
					if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
						sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
						transferIndex <= 0)
						break;
					if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
						transfer(tab, nt);
				}
				else if (U.compareAndSwapInt(this, SIZECTL, sc,
											 (rs << RESIZE_STAMP_SHIFT) + 2))
					transfer(tab, null);
				s = sumCount();
			}
		}
		//*/
	}

	/**
	 * Helps transfer if a resize is in progress.
	 */
	final Node<V>[] helpTransfer(Node<V>[] tab, Node<V> f) {
		Node<V>[] nextTab; int sc;
		if (tab != null && (f instanceof ForwardingNode) &&
			(nextTab = ((ForwardingNode<V>)f).nextTable) != null) {
			int rs = resizeStamp(tab.length);
			while (nextTab == nextTable && table == tab &&
				   (sc = sizeCtl) < 0) {
				if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
					sc == rs + MAX_RESIZERS || transferIndex <= 0)
					break;
				if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
					transfer(tab, nextTab);
					break;
				}
			}
			return nextTab;
		}
		return table;
	}

	/**
	 * Tries to presize table to accommodate the given number of elements.
	 *
	 * @param size number of elements (doesn't need to be perfectly accurate)
	 */
	private final void tryPresize(int size) {
		int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
			tableSizeFor(size + (size >>> 1) + 1);
		int sc;
		while ((sc = sizeCtl) >= 0) {
			Node<V>[] tab = table; int n;
			if (tab == null || (n = tab.length) == 0) {
				n = (sc > c) ? sc : c;
				if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
					try {
						if (table == tab) {
							@SuppressWarnings("unchecked")
							Node<V>[] nt = (Node<V>[])new Node<?>[n];
							table = nt;
							sc = n - (n >>> 2);
						}
					} finally {
						sizeCtl = sc;
					}
				}
			}
			else if (c <= sc || n >= MAXIMUM_CAPACITY)
				break;
			else if (tab == table) {
				int rs = resizeStamp(n);
				if (sc < 0) {
					Node<V>[] nt;
					if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
						sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
						transferIndex <= 0)
						break;
					if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
						transfer(tab, nt);
				}
				else if (U.compareAndSwapInt(this, SIZECTL, sc,
											 (rs << RESIZE_STAMP_SHIFT) + 2))
					transfer(tab, null);
			}
		}
	}

	/**
	 * Moves and/or copies the nodes in each bin to new table. See
	 * above for explanation.
	 */
	private final void transfer(Node<V>[] tab, Node<V>[] nextTab) {
		int n = tab.length, stride;
		if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
			stride = MIN_TRANSFER_STRIDE; // subdivide range
		if (nextTab == null) {			// initiating
			try {
				@SuppressWarnings("unchecked")
				Node<V>[] nt = (Node<V>[])new Node<?>[n << 1];
				nextTab = nt;
			} catch (Throwable ex) {	  // try to cope with OOME
				sizeCtl = Integer.MAX_VALUE;
				return;
			}
			nextTable = nextTab;
			transferIndex = n;
		}
		int nextn = nextTab.length;
		ForwardingNode<V> fwd = new ForwardingNode<V>(nextTab);
		boolean advance = true;
		boolean finishing = false; // to ensure sweep before committing nextTab
		for (int i = 0, bound = 0;;) {
			Node<V> f; int fh;
			while (advance) {
				int nextIndex, nextBound;
				if (--i >= bound || finishing)
					advance = false;
				else if ((nextIndex = transferIndex) <= 0) {
					i = -1;
					advance = false;
				}
				else if (U.compareAndSwapInt
						 (this, TRANSFERINDEX, nextIndex,
						  nextBound = (nextIndex > stride ?
									   nextIndex - stride : 0))) {
					bound = nextBound;
					i = nextIndex - 1;
					advance = false;
				}
			}
			if (i < 0 || i >= n || i + n >= nextn) {
				int sc;
				if (finishing) {
					nextTable = null;
					table = nextTab;
					sizeCtl = (n << 1) - (n >>> 1);
					return;
				}
				if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
					if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
						return;
					finishing = advance = true;
					i = n; // recheck before commit
				}
			}
			else if ((f = tabAt(tab, i)) == null)
				advance = casTabAt(tab, i, null, fwd);
			else if ((fh = f.hash) == MOVED)
				advance = true; // already processed
			else {
				synchronized (f) {
					if (tabAt(tab, i) == f) {
						Node<V> ln, hn;
						if (fh >= 0) {
							int runBit = fh & n;
							Node<V> lastRun = f;
							for (Node<V> p = f.next; p != null; p = p.next) {
								int b = p.hash & n;
								if (b != runBit) {
									runBit = b;
									lastRun = p;
								}
							}
							if (runBit == 0) {
								ln = lastRun;
								hn = null;
							}
							else {
								hn = lastRun;
								ln = null;
							}
							for (Node<V> p = f; p != lastRun; p = p.next) {
								int ph = p.hash, pkx = p.keyX, pky = p.keyY; V pv = p.val;
								if ((ph & n) == 0)
									ln = new Node<V>(ph, pkx, pky, pv, ln);
								else
									hn = new Node<V>(ph, pkx, pky, pv, hn);
							}
							setTabAt(nextTab, i, ln);
							setTabAt(nextTab, i + n, hn);
							setTabAt(tab, i, fwd);
							advance = true;
						}
						else if (f instanceof TreeBin) {
							TreeBin<V> t = (TreeBin<V>)f;
							TreeNode<V> lo = null, loTail = null;
							TreeNode<V> hi = null, hiTail = null;
							int lc = 0, hc = 0;
							for (Node<V> e = t.first; e != null; e = e.next) {
								int h = e.hash;
								TreeNode<V> p = new TreeNode<V>
									(h, e.keyX, e.keyY, e.val, null, null);
								if ((h & n) == 0) {
									if ((p.prev = loTail) == null)
										lo = p;
									else
										loTail.next = p;
									loTail = p;
									++lc;
								}
								else {
									if ((p.prev = hiTail) == null)
										hi = p;
									else
										hiTail.next = p;
									hiTail = p;
									++hc;
								}
							}
							ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
								(hc != 0) ? new TreeBin<V>(lo) : t;
							hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
								(lc != 0) ? new TreeBin<V>(hi) : t;
							setTabAt(nextTab, i, ln);
							setTabAt(nextTab, i + n, hn);
							setTabAt(tab, i, fwd);
							advance = true;
						}
					}
				}
			}
		}
	}

	/* ---------------- Counter support -------------- */

	/**
	 * A padded cell for distributing counts.  Adapted from LongAdder
	 * and Striped64.  See their internal docs for explanation.
	 */
	@sun.misc.Contended static final class CounterCell {
		volatile long value;
		CounterCell(long x) { value = x; }
	}

	final long sumCount() {
		CounterCell[] as = counterCells; CounterCell a;
		long sum = baseCount;
		if (as != null) {
			for (int i = 0; i < as.length; ++i) {
				if ((a = as[i]) != null)
					sum += a.value;
			}
		}
		return sum;
	}

	// See LongAdder version for explanation
	private final void fullAddCount(long x, boolean wasUncontended) {
		///*
		int h;
		if ((h = getThreadLocalRandomProbe()) == 0) {
			//ThreadLocalRandom.localInit();	  // force initialization
			// Since localInit() is package-private, we must resort to
			// reflection to boot this thing up. Luckily this only appears to 
			// need to be done once-per-thread, so this shouldn't introduce too
			// much of a performance deficit.
			try {
				Method m = ThreadLocalRandom.class.getMethod("localInit");
				m.setAccessible(true);
				m.invoke(null);
			} catch(Exception e) {
				throw new Error(e);
			}
			
			h = getThreadLocalRandomProbe();
			wasUncontended = true;
		}
		boolean collide = false;				// True if last slot nonempty
		for (;;) {
			CounterCell[] as; CounterCell a; int n; long v;
			if ((as = counterCells) != null && (n = as.length) > 0) {
				if ((a = as[(n - 1) & h]) == null) {
					if (cellsBusy == 0) {			// Try to attach new Cell
						CounterCell r = new CounterCell(x); // Optimistic create
						if (cellsBusy == 0 &&
							U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
							boolean created = false;
							try {			   // Recheck under lock
								CounterCell[] rs; int m, j;
								if ((rs = counterCells) != null &&
									(m = rs.length) > 0 &&
									rs[j = (m - 1) & h] == null) {
									rs[j] = r;
									created = true;
								}
							} finally {
								cellsBusy = 0;
							}
							if (created)
								break;
							continue;		   // Slot is now non-empty
						}
					}
					collide = false;
				}
				else if (!wasUncontended)	   // CAS already known to fail
					wasUncontended = true;	  // Continue after rehash
				else if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
					break;
				else if (counterCells != as || n >= NCPU)
					collide = false;			// At max size or stale
				else if (!collide)
					collide = true;
				else if (cellsBusy == 0 &&
						 U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
					try {
						if (counterCells == as) {// Expand table unless stale
							CounterCell[] rs = new CounterCell[n << 1];
							for (int i = 0; i < n; ++i)
								rs[i] = as[i];
							counterCells = rs;
						}
					} finally {
						cellsBusy = 0;
					}
					collide = false;
					continue;				   // Retry with expanded table
				}
				h = threadLocalRandomAdvanceProbe(h);
			}
			else if (cellsBusy == 0 && counterCells == as &&
					 U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
				boolean init = false;
				try {						   // Initialize table
					if (counterCells == as) {
						CounterCell[] rs = new CounterCell[2];
						rs[h & 1] = new CounterCell(x);
						counterCells = rs;
						init = true;
					}
				} finally {
					cellsBusy = 0;
				}
				if (init)
					break;
			}
			else if (U.compareAndSwapLong(this, BASECOUNT, v = baseCount, v + x))
				break;						  // Fall back on using base
		}
		//*/
	}

	/* ---------------- Conversion from/to TreeBins -------------- */

	/**
	 * Replaces all linked nodes in bin at given index unless table is
	 * too small, in which case resizes instead.
	 */
	private final void treeifyBin(Node<V>[] tab, int index) {
		Node<V> b; int n;
		if (tab != null) {
			if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
				tryPresize(n << 1);
			else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
				synchronized (b) {
					if (tabAt(tab, index) == b) {
						TreeNode<V> hd = null, tl = null;
						for (Node<V> e = b; e != null; e = e.next) {
							TreeNode<V> p =
								new TreeNode<V>(e.hash, e.keyX, e.keyY, e.val,
												  null, null);
							if ((p.prev = tl) == null)
								hd = p;
							else
								tl.next = p;
							tl = p;
						}
						setTabAt(tab, index, new TreeBin<V>(hd));
					}
				}
			}
		}
	}

	/**
	 * Returns a list on non-TreeNodes replacing those in given list.
	 */
	static <V> Node<V> untreeify(Node<V> b) {
		Node<V> hd = null, tl = null;
		for (Node<V> q = b; q != null; q = q.next) {
			Node<V> p = new Node<V>(q.hash, q.keyX, q.keyY, q.val, null);
			if (tl == null)
				hd = p;
			else
				tl.next = p;
			tl = p;
		}
		return hd;
	}

	/* ---------------- TreeNodes -------------- */

	/**
	 * Nodes for use in TreeBins
	 */
	static final class TreeNode<V> extends Node<V> {
		TreeNode<V> parent;  // red-black tree links
		TreeNode<V> left;
		TreeNode<V> right;
		TreeNode<V> prev;	// needed to unlink next upon deletion
		boolean red;

		TreeNode(int hash, int keyX, int keyY, V val, Node<V> next,
				 TreeNode<V> parent) {
			super(hash, keyX, keyY, val, next);
			this.parent = parent;
		}
		
		@Override
		Node<V> find(int h, int keyX, int keyY) {
			return findTreeNode(h, keyX, keyY);
		}

		/**
		 * Returns the TreeNode (or null if not found) for the given key
		 * starting at given root.
		 */
		final TreeNode<V> findTreeNode(int h, int kx, int ky) {
			
				TreeNode<V> p = this;
				do  {
					int ph, dir; TreeNode<V> q;
					TreeNode<V> pl = p.left, pr = p.right;
					if ((ph = p.hash) > h)
						p = pl;
					else if (ph < h)
						p = pr;
					else if (p.keyX == kx && p.keyY == ky)
						return p;
					else if (pl == null)
						p = pr;
					else if (pr == null)
						p = pl;
					else if ((dir = compareComparables(kx, ky, p.keyX, p.keyY)) != 0)
						p = (dir < 0) ? pl : pr;
					else if ((q = pr.findTreeNode(h, kx, ky)) != null)
						return q;
					else
						p = pl;
				} while (p != null);
			return null;
		}
	}

	/* ---------------- TreeBins -------------- */

	/**
	 * TreeNodes used at the heads of bins. TreeBins do not hold user
	 * keys or values, but instead point to list of TreeNodes and
	 * their root. They also maintain a parasitic read-write lock
	 * forcing writers (who hold bin lock) to wait for readers (who do
	 * not) to complete before tree restructuring operations.
	 */
	static final class TreeBin<V> extends Node<V> {
		TreeNode<V> root;
		volatile TreeNode<V> first;
		volatile Thread waiter;
		volatile int lockState;
		// values for lockState
		static final int WRITER = 1; // set while holding write lock
		static final int WAITER = 2; // set when waiting for write lock
		static final int READER = 4; // increment value for setting read lock

		/**
		 * Tie-breaking utility for ordering insertions when equal
		 * hashCodes and non-comparable. We don't require a total
		 * order, just a consistent insertion rule to maintain
		 * equivalence across rebalancings. Tie-breaking further than
		 * necessary simplifies testing a bit.
		 */
		static int tieBreakOrder(int kx1, int ky1, int kx2, int ky2) {
			/*
			int d;
			if (a == null || b == null ||
				(d = a.getClass().getName().
				 compareTo(b.getClass().getName())) == 0)
				d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
					 -1 : 1);
			return d;
			*/
			// TODO: this should theoretically not return 0, but if it does, RIP us
			return compareComparables(kx1, ky1, kx2, ky2);
		}

		/**
		 * Creates bin with initial set of nodes headed by b.
		 */
		TreeBin(TreeNode<V> b) {
			super(TREEBIN, 0, 0, null, null);
			this.first = b;
			TreeNode<V> r = null;
			for (TreeNode<V> x = b, next; x != null; x = next) {
				next = (TreeNode<V>)x.next;
				x.left = x.right = null;
				if (r == null) {
					x.parent = null;
					x.red = false;
					r = x;
				}
				else {
					int h = x.hash;
					for (TreeNode<V> p = r;;) {
						int dir, ph;
						if ((ph = p.hash) > h)
							dir = -1;
						else if (ph < h)
							dir = 1;
						else if((dir = compareComparables(x.keyX, x.keyY, p.keyX, p.keyY)) == 0)
							// we formerly used tieBreakOrder
							throw new RuntimeException("The keys are equal!!");
						TreeNode<V> xp = p;
						if ((p = (dir <= 0) ? p.left : p.right) == null) {
							x.parent = xp;
							if (dir <= 0)
								xp.left = x;
							else
								xp.right = x;
							r = balanceInsertion(r, x);
							break;
						}
					}
				}
			}
			this.root = r;
			assert checkInvariants(root);
		}

		/**
		 * Acquires write lock for tree restructuring.
		 */
		private final void lockRoot() {
			if (!U.compareAndSwapInt(this, LOCKSTATE, 0, WRITER))
				contendedLock(); // offload to separate method
		}

		/**
		 * Releases write lock for tree restructuring.
		 */
		private final void unlockRoot() {
			lockState = 0;
		}

		/**
		 * Possibly blocks awaiting root lock.
		 */
		private final void contendedLock() {
			boolean waiting = false;
			for (int s;;) {
				if (((s = lockState) & ~WAITER) == 0) {
					if (U.compareAndSwapInt(this, LOCKSTATE, s, WRITER)) {
						if (waiting)
							waiter = null;
						return;
					}
				}
				else if ((s & WAITER) == 0) {
					if (U.compareAndSwapInt(this, LOCKSTATE, s, s | WAITER)) {
						waiting = true;
						waiter = Thread.currentThread();
					}
				}
				else if (waiting)
					LockSupport.park(this);
			}
		}

		/**
		 * Returns matching node or null if none. Tries to search
		 * using tree comparisons from root, but continues linear
		 * search when lock not available.
		 */
		@Override
		final Node<V> find(int h, int kx, int ky) {
			for (Node<V> e = first; e != null; ) {
				int s;
				if (((s = lockState) & (WAITER|WRITER)) != 0) {
					if (e.hash == h && e.keyX == kx && e.keyY == ky)
						return e;
					e = e.next;
				}
				else if (U.compareAndSwapInt(this, LOCKSTATE, s,
											 s + READER)) {
					TreeNode<V> r, p;
					try {
						p = ((r = root) == null ? null :
							 r.findTreeNode(h, kx, ky));
					} finally {
						Thread w;
						if (U.getAndAddInt(this, LOCKSTATE, -READER) ==
							(READER|WAITER) && (w = waiter) != null)
							LockSupport.unpark(w);
					}
					return p;
				}
			}
			return null;
		}

		/**
		 * Finds or adds a node.
		 * @return null if added
		 */
		final TreeNode<V> putTreeVal(int h, int kX, int kY, V v) {
			boolean searched = false;
			for (TreeNode<V> p = root;;) {
				int dir, ph;
				if (p == null) {
					first = root = new TreeNode<V>(h, kX, kY, v, null, null);
					break;
				}
				else if ((ph = p.hash) > h)
					dir = -1;
				else if (ph < h)
					dir = 1;
				else if (p.keyX == kX && p.keyY == keyY)
					return p;
				else if ((dir = compareComparables(kX, kY, p.keyX, p.keyY)) == 0) {
					if (!searched) {
						TreeNode<V> q, ch;
						searched = true;
						if (((ch = p.left) != null &&
							 (q = ch.findTreeNode(h, kX, kY)) != null) ||
							((ch = p.right) != null &&
							 (q = ch.findTreeNode(h, kX, kY)) != null))
							return q;
					}
					throw new RuntimeException("Compared keys in treenode are equal!!!");
					//dir = tieBreakOrder(k, pk);
				}

				TreeNode<V> xp = p;
				if ((p = (dir <= 0) ? p.left : p.right) == null) {
					TreeNode<V> x, f = first;
					first = x = new TreeNode<V>(h, kX, kY, v, f, xp);
					if (f != null)
						f.prev = x;
					if (dir <= 0)
						xp.left = x;
					else
						xp.right = x;
					if (!xp.red)
						x.red = true;
					else {
						lockRoot();
						try {
							root = balanceInsertion(root, x);
						} finally {
							unlockRoot();
						}
					}
					break;
				}
			}
			assert checkInvariants(root);
			return null;
		}

		/**
		 * Removes the given node, that must be present before this
		 * call.  This is messier than typical red-black deletion code
		 * because we cannot swap the contents of an interior node
		 * with a leaf successor that is pinned by "next" pointers
		 * that are accessible independently of lock. So instead we
		 * swap the tree linkages.
		 *
		 * @return true if now too small, so should be untreeified
		 */
		final boolean removeTreeNode(TreeNode<V> p) {
			TreeNode<V> next = (TreeNode<V>)p.next;
			TreeNode<V> pred = p.prev;  // unlink traversal pointers
			TreeNode<V> r, rl;
			if (pred == null)
				first = next;
			else
				pred.next = next;
			if (next != null)
				next.prev = pred;
			if (first == null) {
				root = null;
				return true;
			}
			if ((r = root) == null || r.right == null || // too small
				(rl = r.left) == null || rl.left == null)
				return true;
			lockRoot();
			try {
				TreeNode<V> replacement;
				TreeNode<V> pl = p.left;
				TreeNode<V> pr = p.right;
				if (pl != null && pr != null) {
					TreeNode<V> s = pr, sl;
					while ((sl = s.left) != null) // find successor
						s = sl;
					boolean c = s.red; s.red = p.red; p.red = c; // swap colors
					TreeNode<V> sr = s.right;
					TreeNode<V> pp = p.parent;
					if (s == pr) { // p was s's direct parent
						p.parent = s;
						s.right = p;
					}
					else {
						TreeNode<V> sp = s.parent;
						if ((p.parent = sp) != null) {
							if (s == sp.left)
								sp.left = p;
							else
								sp.right = p;
						}
						if ((s.right = pr) != null)
							pr.parent = s;
					}
					p.left = null;
					if ((p.right = sr) != null)
						sr.parent = p;
					if ((s.left = pl) != null)
						pl.parent = s;
					if ((s.parent = pp) == null)
						r = s;
					else if (p == pp.left)
						pp.left = s;
					else
						pp.right = s;
					if (sr != null)
						replacement = sr;
					else
						replacement = p;
				}
				else if (pl != null)
					replacement = pl;
				else if (pr != null)
					replacement = pr;
				else
					replacement = p;
				if (replacement != p) {
					TreeNode<V> pp = replacement.parent = p.parent;
					if (pp == null)
						r = replacement;
					else if (p == pp.left)
						pp.left = replacement;
					else
						pp.right = replacement;
					p.left = p.right = p.parent = null;
				}

				root = (p.red) ? r : balanceDeletion(r, replacement);

				if (p == replacement) {  // detach pointers
					TreeNode<V> pp;
					if ((pp = p.parent) != null) {
						if (p == pp.left)
							pp.left = null;
						else if (p == pp.right)
							pp.right = null;
						p.parent = null;
					}
				}
			} finally {
				unlockRoot();
			}
			assert checkInvariants(root);
			return false;
		}

		/* ------------------------------------------------------------ */
		// Red-black tree methods, all adapted from CLR

		static <V> TreeNode<V> rotateLeft(TreeNode<V> root,
											  TreeNode<V> p) {
			TreeNode<V> r, pp, rl;
			if (p != null && (r = p.right) != null) {
				if ((rl = p.right = r.left) != null)
					rl.parent = p;
				if ((pp = r.parent = p.parent) == null)
					(root = r).red = false;
				else if (pp.left == p)
					pp.left = r;
				else
					pp.right = r;
				r.left = p;
				p.parent = r;
			}
			return root;
		}

		static <V> TreeNode<V> rotateRight(TreeNode<V> root,
											   TreeNode<V> p) {
			TreeNode<V> l, pp, lr;
			if (p != null && (l = p.left) != null) {
				if ((lr = p.left = l.right) != null)
					lr.parent = p;
				if ((pp = l.parent = p.parent) == null)
					(root = l).red = false;
				else if (pp.right == p)
					pp.right = l;
				else
					pp.left = l;
				l.right = p;
				p.parent = l;
			}
			return root;
		}

		static <V> TreeNode<V> balanceInsertion(TreeNode<V> root,
													TreeNode<V> x) {
			x.red = true;
			for (TreeNode<V> xp, xpp, xppl, xppr;;) {
				if ((xp = x.parent) == null) {
					x.red = false;
					return x;
				}
				else if (!xp.red || (xpp = xp.parent) == null)
					return root;
				if (xp == (xppl = xpp.left)) {
					if ((xppr = xpp.right) != null && xppr.red) {
						xppr.red = false;
						xp.red = false;
						xpp.red = true;
						x = xpp;
					}
					else {
						if (x == xp.right) {
							root = rotateLeft(root, x = xp);
							xpp = (xp = x.parent) == null ? null : xp.parent;
						}
						if (xp != null) {
							xp.red = false;
							if (xpp != null) {
								xpp.red = true;
								root = rotateRight(root, xpp);
							}
						}
					}
				}
				else {
					if (xppl != null && xppl.red) {
						xppl.red = false;
						xp.red = false;
						xpp.red = true;
						x = xpp;
					}
					else {
						if (x == xp.left) {
							root = rotateRight(root, x = xp);
							xpp = (xp = x.parent) == null ? null : xp.parent;
						}
						if (xp != null) {
							xp.red = false;
							if (xpp != null) {
								xpp.red = true;
								root = rotateLeft(root, xpp);
							}
						}
					}
				}
			}
		}

		static <V> TreeNode<V> balanceDeletion(TreeNode<V> root,
												   TreeNode<V> x) {
			for (TreeNode<V> xp, xpl, xpr;;)  {
				if (x == null || x == root)
					return root;
				else if ((xp = x.parent) == null) {
					x.red = false;
					return x;
				}
				else if (x.red) {
					x.red = false;
					return root;
				}
				else if ((xpl = xp.left) == x) {
					if ((xpr = xp.right) != null && xpr.red) {
						xpr.red = false;
						xp.red = true;
						root = rotateLeft(root, xp);
						xpr = (xp = x.parent) == null ? null : xp.right;
					}
					if (xpr == null)
						x = xp;
					else {
						TreeNode<V> sl = xpr.left, sr = xpr.right;
						if ((sr == null || !sr.red) &&
							(sl == null || !sl.red)) {
							xpr.red = true;
							x = xp;
						}
						else {
							if (sr == null || !sr.red) {
								if (sl != null)
									sl.red = false;
								xpr.red = true;
								root = rotateRight(root, xpr);
								xpr = (xp = x.parent) == null ?
									null : xp.right;
							}
							if (xpr != null) {
								xpr.red = (xp == null) ? false : xp.red;
								if ((sr = xpr.right) != null)
									sr.red = false;
							}
							if (xp != null) {
								xp.red = false;
								root = rotateLeft(root, xp);
							}
							x = root;
						}
					}
				}
				else { // symmetric
					if (xpl != null && xpl.red) {
						xpl.red = false;
						xp.red = true;
						root = rotateRight(root, xp);
						xpl = (xp = x.parent) == null ? null : xp.left;
					}
					if (xpl == null)
						x = xp;
					else {
						TreeNode<V> sl = xpl.left, sr = xpl.right;
						if ((sl == null || !sl.red) &&
							(sr == null || !sr.red)) {
							xpl.red = true;
							x = xp;
						}
						else {
							if (sl == null || !sl.red) {
								if (sr != null)
									sr.red = false;
								xpl.red = true;
								root = rotateLeft(root, xpl);
								xpl = (xp = x.parent) == null ?
									null : xp.left;
							}
							if (xpl != null) {
								xpl.red = (xp == null) ? false : xp.red;
								if ((sl = xpl.left) != null)
									sl.red = false;
							}
							if (xp != null) {
								xp.red = false;
								root = rotateRight(root, xp);
							}
							x = root;
						}
					}
				}
			}
		}

		/**
		 * Recursive invariant check
		 */
		static <V> boolean checkInvariants(TreeNode<V> t) {
			TreeNode<V> tp = t.parent, tl = t.left, tr = t.right,
				tb = t.prev, tn = (TreeNode<V>)t.next;
			if (tb != null && tb.next != t)
				return false;
			if (tn != null && tn.prev != t)
				return false;
			if (tp != null && t != tp.left && t != tp.right)
				return false;
			if (tl != null && (tl.parent != t || tl.hash > t.hash))
				return false;
			if (tr != null && (tr.parent != t || tr.hash < t.hash))
				return false;
			if (t.red && tl != null && tl.red && tr != null && tr.red)
				return false;
			if (tl != null && !checkInvariants(tl))
				return false;
			if (tr != null && !checkInvariants(tr))
				return false;
			return true;
		}

		private static final sun.misc.Unsafe U;
		private static final long LOCKSTATE;
		static {
			try {
				U = TheUnsafe.unsafe;
				Class<?> k = TreeBin.class;
				LOCKSTATE = U.objectFieldOffset
					(k.getDeclaredField("lockState"));
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

	/* ----------------Table Traversal -------------- */

	/**
	 * Records the table, its length, and current traversal index for a
	 * traverser that must process a region of a forwarded table before
	 * proceeding with current table.
	 */
	static final class TableStack<V> {
		int length;
		int index;
		Node<V>[] tab;
		TableStack<V> next;
	}

	/**
	 * Encapsulates traversal for methods such as containsValue; also
	 * serves as a base class for other iterators and spliterators.
	 *
	 * Method advance visits once each still-valid node that was
	 * reachable upon iterator construction. It might miss some that
	 * were added to a bin after the bin was visited, which is OK wrt
	 * consistency guarantees. Maintaining this property in the face
	 * of possible ongoing resizes requires a fair amount of
	 * bookkeeping state that is difficult to optimize away amidst
	 * volatile accesses.  Even so, traversal maintains reasonable
	 * throughput.
	 *
	 * Normally, iteration proceeds bin-by-bin traversing lists.
	 * However, if the table has been resized, then all future steps
	 * must traverse both the bin at the current index as well as at
	 * (index + baseSize); and so on for further resizings. To
	 * paranoically cope with potential sharing by users of iterators
	 * across threads, iteration terminates if a bounds checks fails
	 * for a table read.
	 */
	static class Traverser<V> {
		Node<V>[] tab;		// current table; updated if resized
		Node<V> next;		 // the next entry to use
		TableStack<V> stack, spare; // to save/restore on ForwardingNodes
		int index;			  // index of bin to use next
		int baseIndex;		  // current index of initial table
		int baseLimit;		  // index bound for initial table
		final int baseSize;	 // initial table size

		Traverser(Node<V>[] tab, int size, int index, int limit) {
			this.tab = tab;
			this.baseSize = size;
			this.baseIndex = this.index = index;
			this.baseLimit = limit;
			this.next = null;
		}

		/**
		 * Advances if possible, returning next valid node, or null if none.
		 */
		final Node<V> advance() {
			Node<V> e;
			if ((e = next) != null)
				e = e.next;
			for (;;) {
				Node<V>[] t; int i, n;  // must use locals in checks
				if (e != null)
					return next = e;
				if (baseIndex >= baseLimit || (t = tab) == null ||
					(n = t.length) <= (i = index) || i < 0)
					return next = null;
				if ((e = tabAt(t, i)) != null && e.hash < 0) {
					if (e instanceof ForwardingNode) {
						tab = ((ForwardingNode<V>)e).nextTable;
						e = null;
						pushState(t, i, n);
						continue;
					}
					else if (e instanceof TreeBin)
						e = ((TreeBin<V>)e).first;
					else
						e = null;
				}
				if (stack != null)
					recoverState(n);
				else if ((index = i + baseSize) >= n)
					index = ++baseIndex; // visit upper slots if present
			}
		}

		/**
		 * Saves traversal state upon encountering a forwarding node.
		 */
		private void pushState(Node<V>[] t, int i, int n) {
			TableStack<V> s = spare;  // reuse if possible
			if (s != null)
				spare = s.next;
			else
				s = new TableStack<V>();
			s.tab = t;
			s.length = n;
			s.index = i;
			s.next = stack;
			stack = s;
		}

		/**
		 * Possibly pops traversal state.
		 *
		 * @param n length of current table
		 */
		private void recoverState(int n) {
			TableStack<V> s; int len;
			while ((s = stack) != null && (index += (len = s.length)) >= n) {
				n = len;
				index = s.index;
				tab = s.tab;
				s.tab = null;
				TableStack<V> next = s.next;
				s.next = spare; // save for reuse
				stack = next;
				spare = s;
			}
			if (s == null && (index += baseSize) >= n)
				index = ++baseIndex;
		}
	}

	/**
	 * Base of key, value, and entry Iterators. Adds fields to
	 * Traverser to support iterator.remove.
	 */
	static class BaseIterator<V> extends Traverser<V> {
		final ConcurrentBiIntHashMap<V> map;
		Node<V> lastReturned;
		BaseIterator(Node<V>[] tab, int size, int index, int limit,
					ConcurrentBiIntHashMap<V> map) {
			super(tab, size, index, limit);
			this.map = map;
			advance();
		}

		public final boolean hasNext() { return next != null; }
		public final boolean hasMoreElements() { return next != null; }

		public final void remove() {
			Node<V> p;
			if ((p = lastReturned) == null)
				throw new IllegalStateException();
			lastReturned = null;
			map.replaceNode(p.keyX, p.keyY, null, null);
		}
	}

	static final class ValueIterator<V> extends BaseIterator<V>
		implements Iterator<V>, Enumeration<V> {
		ValueIterator(Node<V>[] tab, int index, int size, int limit,
					  ConcurrentBiIntHashMap<V> map) {
			super(tab, index, size, limit, map);
		}

		public final V next() {
			Node<V> p;
			if ((p = next) == null)
				throw new NoSuchElementException();
			V v = p.val;
			lastReturned = p;
			advance();
			return v;
		}

		public final V nextElement() { return next(); }
	}

	static final class EntryIterator<V> extends BaseIterator<V>
		implements Iterator<Entry<V>> {
		EntryIterator(Node<V>[] tab, int index, int size, int limit,
					  ConcurrentBiIntHashMap<V> map) {
			super(tab, index, size, limit, map);
		}

		public final Entry<V> next() {
			Node<V> p;
			if ((p = next) == null)
				throw new NoSuchElementException();
			lastReturned = p;
			advance();
			return new MapEntry<V>(p.keyX, p.keyY, p.val, map);
		}
	}

	/**
	 * Exported Entry for EntryIterator
	 */
	static final class MapEntry<V> implements Entry<V> {
		final int keyX, keyY;
		V val;	   // non-null
		final ConcurrentBiIntHashMap<V> map;
		MapEntry(int keyX, int keyY, V val, ConcurrentBiIntHashMap<V> map) {
			this.keyX = keyX;
			this.keyY = keyY;
			this.val = val;
			this.map = map;
		}
		public int getKeyX()		{ return keyX; }
		public int getKeyY()		{ return keyY; }
		public V getValue()	  { return val; }
		public int hashCode()	{ return keyX ^ keyY ^ val.hashCode(); }
		public String toString() { return "(" + keyX + ',' + keyY + ")=" + val; }

		public boolean equals(Object o) {
			Object v; Entry<?> e;
			return ((o instanceof Entry) &&
					(v = (e = (Entry<?>)o).getValue()) != null &&
					(e.getKeyX() == keyX && e.getKeyY() == keyY) &&
					(v == val || v.equals(val)));
		}

		/**
		 * Sets our entry's value and writes through to the map. The
		 * value to return is somewhat arbitrary here. Since we do not
		 * necessarily track asynchronous changes, the most recent
		 * "previous" value could be different from what we return (or
		 * could even have been removed, in which case the put will
		 * re-establish). We do not and cannot guarantee more.
		 */
		public V setValue(V value) {
			if (value == null) throw new NullPointerException();
			V v = val;
			val = value;
			map.put(keyX, keyY, value);
			return v;
		}
	}

	static final class ValueSpliterator<V> extends Traverser<V>
		implements Spliterator<V> {
		long est;			   // size estimate
		ValueSpliterator(Node<V>[] tab, int size, int index, int limit,
						 long est) {
			super(tab, size, index, limit);
			this.est = est;
		}

		public Spliterator<V> trySplit() {
			int i, f, h;
			return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null :
				new ValueSpliterator<V>(tab, baseSize, baseLimit = h,
										  f, est >>>= 1);
		}

		public void forEachRemaining(Consumer<? super V> action) {
			if (action == null) throw new NullPointerException();
			for (Node<V> p; (p = advance()) != null;)
				action.accept(p.val);
		}

		public boolean tryAdvance(Consumer<? super V> action) {
			if (action == null) throw new NullPointerException();
			Node<V> p;
			if ((p = advance()) == null)
				return false;
			action.accept(p.val);
			return true;
		}

		public long estimateSize() { return est; }

		public int characteristics() {
			return Spliterator.CONCURRENT | Spliterator.NONNULL;
		}
	}

	static final class EntrySpliterator<V> extends Traverser<V>
		implements Spliterator<Entry<V>> {
		final ConcurrentBiIntHashMap<V> map; // To export MapEntry
		long est;			   // size estimate
		EntrySpliterator(Node<V>[] tab, int size, int index, int limit,
						 long est, ConcurrentBiIntHashMap<V> map) {
			super(tab, size, index, limit);
			this.map = map;
			this.est = est;
		}

		public Spliterator<Entry<V>> trySplit() {
			int i, f, h;
			return (h = ((i = baseIndex) + (f = baseLimit)) >>> 1) <= i ? null :
				new EntrySpliterator<V>(tab, baseSize, baseLimit = h,
										  f, est >>>= 1, map);
		}

		public void forEachRemaining(Consumer<? super Entry<V>> action) {
			if (action == null) throw new NullPointerException();
			for (Node<V> p; (p = advance()) != null; )
				action.accept(new MapEntry<V>(p.getKeyX(), p.getKeyY(), p.val, map));
		}

		public boolean tryAdvance(Consumer<? super Entry<V>> action) {
			if (action == null) throw new NullPointerException();
			Node<V> p;
			if ((p = advance()) == null)
				return false;
			action.accept(new MapEntry<V>(p.getKeyX(), p.getKeyY(), p.val, map));
			return true;
		}

		public long estimateSize() { return est; }

		public int characteristics() {
			return Spliterator.DISTINCT | Spliterator.CONCURRENT |
				Spliterator.NONNULL;
		}
	}
	
	/* ----------------Views -------------- */

	/**
	 * Base class for views.
	 */
	private abstract static class CollectionView<V,E>
		implements Collection<E>, java.io.Serializable {
		private static final long serialVersionUID = 7249069246763182397L;
		final ConcurrentBiIntHashMap<V> map;
		CollectionView(ConcurrentBiIntHashMap<V> map)  { this.map = map; }

		/**
		 * Removes all of the elements from this view, by removing all
		 * the mappings from the map backing this view.
		 */
		public final void clear()	  { map.clear(); }
		public final int size()		{ return map.size(); }
		public final boolean isEmpty() { return map.isEmpty(); }

		// implementations below rely on concrete classes supplying these
		// abstract methods
		/**
		 * Returns an iterator over the elements in this collection.
		 *
		 * <p>The returned iterator is
		 * <a href="package-summary.html#Weakly"><i>weakly consistent</i></a>.
		 *
		 * @return an iterator over the elements in this collection
		 */
		public abstract Iterator<E> iterator();
		public abstract boolean contains(Object o);
		public abstract boolean remove(Object o);

		private static final String oomeMsg = "Required array size too large";

		public final Object[] toArray() {
			long sz = map.mappingCount();
			if (sz > MAX_ARRAY_SIZE)
				throw new OutOfMemoryError(oomeMsg);
			int n = (int)sz;
			Object[] r = new Object[n];
			int i = 0;
			for (E e : this) {
				if (i == n) {
					if (n >= MAX_ARRAY_SIZE)
						throw new OutOfMemoryError(oomeMsg);
					if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
						n = MAX_ARRAY_SIZE;
					else
						n += (n >>> 1) + 1;
					r = Arrays.copyOf(r, n);
				}
				r[i++] = e;
			}
			return (i == n) ? r : Arrays.copyOf(r, i);
		}

		@SuppressWarnings("unchecked")
		public final <T> T[] toArray(T[] a) {
			long sz = map.mappingCount();
			if (sz > MAX_ARRAY_SIZE)
				throw new OutOfMemoryError(oomeMsg);
			int m = (int)sz;
			T[] r = (a.length >= m) ? a :
				(T[])java.lang.reflect.Array
				.newInstance(a.getClass().getComponentType(), m);
			int n = r.length;
			int i = 0;
			for (E e : this) {
				if (i == n) {
					if (n >= MAX_ARRAY_SIZE)
						throw new OutOfMemoryError(oomeMsg);
					if (n >= MAX_ARRAY_SIZE - (MAX_ARRAY_SIZE >>> 1) - 1)
						n = MAX_ARRAY_SIZE;
					else
						n += (n >>> 1) + 1;
					r = Arrays.copyOf(r, n);
				}
				r[i++] = (T)e;
			}
			if (a == r && i < n) {
				r[i] = null; // null-terminate
				return r;
			}
			return (i == n) ? r : Arrays.copyOf(r, i);
		}

		/**
		 * Returns a string representation of this collection.
		 * The string representation consists of the string representations
		 * of the collection's elements in the order they are returned by
		 * its iterator, enclosed in square brackets ({@code "[]"}).
		 * Adjacent elements are separated by the characters {@code ", "}
		 * (comma and space).  Elements are converted to strings as by
		 * {@link String#valueOf(Object)}.
		 *
		 * @return a string representation of this collection
		 */
		public final String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			Iterator<E> it = iterator();
			if (it.hasNext()) {
				for (;;) {
					Object e = it.next();
					sb.append(e == this ? "(this Collection)" : e);
					if (!it.hasNext())
						break;
					sb.append(',').append(' ');
				}
			}
			return sb.append(']').toString();
		}

		public final boolean containsAll(Collection<?> c) {
			if (c != this) {
				for (Object e : c) {
					if (e == null || !contains(e))
						return false;
				}
			}
			return true;
		}

		public final boolean removeAll(Collection<?> c) {
			if (c == null) throw new NullPointerException();
			boolean modified = false;
			for (Iterator<E> it = iterator(); it.hasNext();) {
				if (c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

		public final boolean retainAll(Collection<?> c) {
			if (c == null) throw new NullPointerException();
			boolean modified = false;
			for (Iterator<E> it = iterator(); it.hasNext();) {
				if (!c.contains(it.next())) {
					it.remove();
					modified = true;
				}
			}
			return modified;
		}

	}
	
	/**
	 * A view of a ConcurrentBiIntHashMap as a {@link Collection} of
	 * values, in which additions are disabled. This class cannot be
	 * directly instantiated. See {@link #values()}.
	 */
	private static final class ValuesView<V> extends CollectionView<V,V>
		implements Collection<V>, java.io.Serializable {
		private static final long serialVersionUID = 2249069246763182397L;
		ValuesView(ConcurrentBiIntHashMap<V> map) { super(map); }
		public final boolean contains(Object o) {
			return map.containsValue(o);
		}

		public final boolean remove(Object o) {
			if (o != null) {
				for (Iterator<V> it = iterator(); it.hasNext();) {
					if (o.equals(it.next())) {
						it.remove();
						return true;
					}
				}
			}
			return false;
		}

		public final Iterator<V> iterator() {
			ConcurrentBiIntHashMap<V> m = map;
			Node<V>[] t;
			int f = (t = m.table) == null ? 0 : t.length;
			return new ValueIterator<V>(t, f, 0, f, m);
		}

		public final boolean add(V e) {
			throw new UnsupportedOperationException();
		}
		public final boolean addAll(Collection<? extends V> c) {
			throw new UnsupportedOperationException();
		}

		public Spliterator<V> spliterator() {
			Node<V>[] t;
			ConcurrentBiIntHashMap<V> m = map;
			long n = m.sumCount();
			int f = (t = m.table) == null ? 0 : t.length;
			return new ValueSpliterator<V>(t, f, 0, f, n < 0L ? 0L : n);
		}

		public void forEach(Consumer<? super V> action) {
			if (action == null) throw new NullPointerException();
			Node<V>[] t;
			if ((t = map.table) != null) {
				Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length);
				for (Node<V> p; (p = it.advance()) != null; )
					action.accept(p.val);
			}
		}
	}

	/**
	 * A view of a ConcurrentBiIntHashMap as a {@link Set} of (key, value)
	 * entries.  This class cannot be directly instantiated. See
	 * {@link #entrySet()}.
	 */
	private static final class EntrySetView<V> extends CollectionView<V,Entry<V>>
		implements Set<Entry<V>>, java.io.Serializable {
		private static final long serialVersionUID = 2249069246763182397L;
		EntrySetView(ConcurrentBiIntHashMap<V> map) { super(map); }

		public boolean contains(Object o) {
			Object v, r; Entry<?> e;
			return ((o instanceof Entry) &&
					(r = map.get((e = (Entry<?>)o).getKeyX(), e.getKeyY())) != null &&
					(v = e.getValue()) != null &&
					(v == r || v.equals(r)));
		}

		public boolean remove(Object o) {
			Object v; Entry<?> e;
			return ((o instanceof Entry) &&
					(v = (e = (Entry<?>)o).getValue()) != null &&
					map.remove(e.getKeyX(), e.getKeyY(), v));
		}

		/**
		 * @return an iterator over the entries of the backing map
		 */
		public Iterator<Entry<V>> iterator() {
			ConcurrentBiIntHashMap<V> m = map;
			Node<V>[] t;
			int f = (t = m.table) == null ? 0 : t.length;
			return new EntryIterator<V>(t, f, 0, f, m);
		}

		public boolean add(Entry<V> e) {
			return map.putVal(e.getKeyX(), e.getKeyY(), e.getValue(), false) == null;
		}

		public boolean addAll(Collection<? extends Entry<V>> c) {
			boolean added = false;
			for (Entry<V> e : c) {
				if (add(e))
					added = true;
			}
			return added;
		}

		public final int hashCode() {
			int h = 0;
			Node<V>[] t;
			if ((t = map.table) != null) {
				Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length);
				for (Node<V> p; (p = it.advance()) != null; ) {
					h += p.hashCode();
				}
			}
			return h;
		}

		public final boolean equals(Object o) {
			Set<?> c;
			return ((o instanceof Set) &&
					((c = (Set<?>)o) == this ||
					 (containsAll(c) && c.containsAll(this))));
		}

		public Spliterator<Entry<V>> spliterator() {
			Node<V>[] t;
			ConcurrentBiIntHashMap<V> m = map;
			long n = m.sumCount();
			int f = (t = m.table) == null ? 0 : t.length;
			return new EntrySpliterator<V>(t, f, 0, f, n < 0L ? 0L : n, m);
		}

		public void forEach(Consumer<? super Entry<V>> action) {
			if (action == null) throw new NullPointerException();
			Node<V>[] t;
			if ((t = map.table) != null) {
				Traverser<V> it = new Traverser<V>(t, t.length, 0, t.length);
				for (Node<V> p; (p = it.advance()) != null; )
					action.accept(new MapEntry<V>(p.keyX, p.keyY, p.val, map));
			}
		}

	}

	// -------------------------------------------------------

	// Unsafe mechanics
	private static final sun.misc.Unsafe U;
	private static final long SIZECTL;
	private static final long TRANSFERINDEX;
	private static final long BASECOUNT;
	private static final long CELLSBUSY;
	private static final long CELLVALUE;
	private static final long ABASE;
	private static final int ASHIFT;
	
	// Thrad#threadLocalRandomProbe
	private static final long PROBE;
	
	/** visible alternative to ThreadLocalRandom#getProbe() */
	private static int getThreadLocalRandomProbe() {
		return U.getInt(Thread.currentThread(), PROBE);
	}
	
	/** visible alternative to ThreadLocalRandom#advanceProbe(int) */
	private static int threadLocalRandomAdvanceProbe(int probe) {
        probe ^= probe << 13;   // xorshift
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        U.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
	}

	static {
		try {
			U = TheUnsafe.unsafe;
			Class<?> k = ConcurrentBiIntHashMap.class;
			SIZECTL = U.objectFieldOffset
				(k.getDeclaredField("sizeCtl"));
			TRANSFERINDEX = U.objectFieldOffset
				(k.getDeclaredField("transferIndex"));
			BASECOUNT = U.objectFieldOffset
				(k.getDeclaredField("baseCount"));
			CELLSBUSY = U.objectFieldOffset
				(k.getDeclaredField("cellsBusy"));
			Class<?> ck = CounterCell.class;
			CELLVALUE = U.objectFieldOffset
				(ck.getDeclaredField("value"));
			Class<?> ak = Node[].class;
			ABASE = U.arrayBaseOffset(ak);
			int scale = U.arrayIndexScale(ak);
			if ((scale & (scale - 1)) != 0)
				throw new Error("data type scale not a power of two");
			ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
			

	        PROBE = U.objectFieldOffset
	                (Thread.class.getDeclaredField("threadLocalRandomProbe"));
		} catch (Exception e) {
			throw new Error(e);
		}
	}
}
