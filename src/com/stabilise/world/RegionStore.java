package com.stabilise.world;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.GuardedBy;
import com.stabilise.util.annotation.NotThreadSafe;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.IteratorUtils;
import com.stabilise.util.collect.LightLinkedList;
import com.stabilise.util.maths.Maths;
import com.stabilise.util.maths.Point;
import com.stabilise.world.loader.WorldLoader.DimensionLoader;

/**
 * Stores and manages regions. Every {@link HostWorld} owns a {@code
 * RegionStore}.
 * 
 * <p>A {@code RegionStore} provides two layers of region storage for a world -
 * primary and cache storage.
 * 
 * <h3>Primary Storage</h3>
 * 
 * <p>Primary storage holds all regions currently being used by the world.
 * Regions are loaded into primary storage using {@link
 * #loadRegion(int, int, boolean) loadRegion()}, and can be retrieved through
 * {@link #getRegionAt(int, int) getRegionAt()}. Every Region in primary
 * storage is {@link Region#update(HostWorld, RegionStore) updated} by {@link
 * #updateRegions()}, and is only removed from primary storage if {@code
 * update} returns {@code true}.
 * 
 * <p>Primary storage is not thread-safe and should only be interacted with on
 * the main thread.
 * 
 * <h3>Cache Storage</h3>
 * 
 * <p>Cache storage can overlap with primary storage, and it holds any Regions
 * which are loaded for anything other than gameplay.
 * 
 * <p>A Region may be obtained from the cache through {@link #cache(int, int)},
 * which is the cache analogue of {@code loadRegion()}. Since this class only
 * cares about the number of threads using a particular region, a thread may
 * invoke {@code cache()} as many times as it pleases. There is no method to
 * relinquish a particular region; a thread <i>must</i> invoke {@link
 * #uncacheAll()} to release each region it has cached.
 * 
 * <p>Cache storage is thread-safe, and interacts with primary storage in a
 * thread-safe way to ensure complete consistency.
 * 
 * <h3>Other Details</h3>
 * 
 * <p>This class's constructor requires a {@link Consumer} to act as an unload
 * handler. It is invoked in {@code updateRegions()} when a region is about to
 * be unloaded - immediately before it is {@link #saveRegion(Region) saved} and
 * removed from primary storage.
 */
public class RegionStore {
	
	/** Wait time in seconds for {@link #waitUntilDone()}. */
	private static final int WAIT_TIME = 5;
	
	/** The number of locks to stripe {@Link #cacheLocks} and {@link
	 * #storeLocks} into. */
	private static final int STRIPE_FACTOR = 8;
	private static final IntBinaryOperator STRIPE_HASHER =
			Maths.generateHashingFunction(STRIPE_FACTOR, false);
	
	private final HostWorld world;
	private DimensionLoader loader = null;
	
	/** Consumer which is invoked right before a region is unloaded, to perform
	 * any unload logic as required by the world. */
	private final Consumer<Region> unloadHandler;
	
	// VERY IMPORTANT IMPLEMENTATION DETAILS:
	// Empirical testing has revealed that the table size for region maps in
	// HostWorld is usually 16, but can grow to 32. This means the number of
	// valid hash bits is either 4 or 5 (all higher bits are masked out), so we
	// need to compress our data into those bits while minimising collisions.
	/** The map of all loaded regions. Maps region.loc -> region. */
	private final ConcurrentMap<Point, Region> regions =
			new ConcurrentHashMap<>();
	/** Tracks the number of loaded regions. */
	private int numRegions = 0;
	
	/** The map of cached regions. Maps region.loc -> region. */
	private final ConcurrentMap<Point, CachedRegion> cache =
			new ConcurrentHashMap<>();
	
	/** Locks to use for managing cache-local data. These locks double as
	 * mutable points for convenience, which should only be used while
	 * synchronised on themselves. */
	private final Striper<Point> cacheLocks =
			new Striper<>(i -> Region.createMutableLoc());
	/** Locks to synchronise on when adding/removing from storage. These locks
	 * are designed to be used to ensure mutual exclusion between the two
	 * following operations:
	 * 
	 * <ul>
	 * <li>In <i>loadRegion()</i>:
	 *     <ul>
	 *     <li>Try to get a region from the cache.
	 *     <li>If it is not in the cache, create it and add it to main storage.
	 *     </ul>
	 * <li>In <i>cache()</i>:
	 *     <ul>
	 *     <li>Try to get a region from main storage.
	 *     <li>If it is not in main storage, create it an add it to the cache.
	 *     </ul>
	 * </ul> */
	private final Striper<Object> storeLocks = new Striper<>(i -> new Object());
	
	/** Dummy key for {@link #regions} whose mutability may be abused for on
	 * the main thread. */
	private final Point unguardedDummyLoc = Region.createMutableLoc();
	
	/** Regions which have been cached by the current worker thread. */
	// Note: Sometime in the future it might be a good idea to switch from a
	// List to a Map, especially if some implementation decides to rapid-fire
	// on cache() to fetch tons of different regions.
	private final ThreadLocal<List<Region>> localCachedRegions =
			ThreadLocal.withInitial(() -> new LightLinkedList<>());
	
	// A Lock and its associated Condition to wait on in waitUntilDone().
	private final ReentrantLock doneLock = new ReentrantLock();
	private final Condition emptyCondition = doneLock.newCondition();
	
	private final Log log;
	
	
	/**
	 * Creates a new region cache.
	 * 
	 * @param world The world to cache regions for.
	 * @param unloadHandler A task to run when a region is about to be
	 * unloaded.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	RegionStore(HostWorld world, Consumer<Region> unloadHandler) {
		this.world = Objects.requireNonNull(world);
		this.unloadHandler = Objects.requireNonNull(unloadHandler);
		
		log = Log.getAgent(world.getDimensionName() + "RegionCache");
	}
	
	/**
	 * Prepares this WorldGenerator by providing it with a reference to the
	 * world's loader.
	 * 
	 * @throws IllegalStateException if this cache has already been prepared.
	 * @throws NullPointerException if loader is null.
	 */
	public void prepare(DimensionLoader loader) {
		if(this.loader != null)
			throw new IllegalStateException("Loader already set");
		this.loader = Objects.requireNonNull(loader);
	}
	
	/**
	 * Updates all regions.
	 */
	@UserThread("MainThread")
	public void updateRegions() {
		IteratorUtils.forEach(regions.values(), r ->
				r.update(world, this) && unloadRegion(r));
	}
	
	/**
	 * Runs the given action for every region in the store.
	 * 
	 * @throws NullPointerException if {@code action} is {@code null}.
	 */
	/*
	@UserThread("MainThread")
	public void forEach(Consumer<Region> action) {
		regions.values().forEach(action);
	}
	*/
	
	/**
	 * Gets a region at the given coordinates, in region-lengths.
	 * 
	 * @return The region, or {@code null} if no such region exists.
	 */
	@UserThread("MainThread")
	@NotThreadSafe
	public Region getRegionAt(int x, int y) {
		return regions.get(unguardedDummyLoc.set(x, y));
	}
	
	/**
	 * Gets the region at the specified location.
	 * 
	 * @param point The region's location, whose coordinates are in
	 * region-lengths.
	 * 
	 * @return The region, or {@code null} if no such region exists.
	 */
	@ThreadSafe
	private Region getRegionAt(Point point) {
		return regions.get(point);
	}
	
	/**
	 * Loads a region into memory. If the region has already been loaded, it
	 * is returned.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region.
	 */
	@UserThread("MainThread")
	@NotThreadSafe
	@Deprecated
	public Region loadRegion(int x, int y) {
		// Get the region if it is already loaded
		Region r = regions.get(unguardedDummyLoc.set(x, y));
		if(r != null)
			return r;
		
		// If it is not loaded directly, try getting it from the cache.
		// Synchronised to make this atomic. See cacheRegion()
		synchronized(storeLocks.get(x, y)) {
			r = getFromCache(unguardedDummyLoc); // unsafeDummyLoc is already (x,y)
			if(r == null) // if it's not cached, create it
				r = new Region(x, y, world.getAge());
			regions.put(r.loc, r);
		}
		
		numRegions++;
		
		loader.loadRegion(r, true);
		
		return r;
	}
	
	@UserThread("MainThread")
	@NotThreadSafe
	public Region loadRegion(int x, int y, boolean makeActive) {
		// Get the region if it is already loaded
		Region r = regions.get(unguardedDummyLoc.set(x, y));
		if(r != null) {
			if(makeActive)
				setRegionActive(r);
			return r;
		}
		
		// If it is not loaded directly, try getting it from the cache.
		// Synchronised to make this atomic. See cache()
		synchronized(storeLocks.get(x, y)) {
			CachedRegion c = cache.get(unguardedDummyLoc); // dummy is (x,y)
			r = c != null ? c.region : new Region(x, y, world.getAge());
			regions.put(r.loc, r);
		}
		
		numRegions++;
		
		loader.loadRegion(r, true);
		
		if(makeActive)
			setRegionActive(r);
		
		return r;
	}
	
	/**
	 * Manages unloading a region. This method invokes {@link #unloadHandler}
	 * and ports the region to the cache in order to save it.
	 * 
	 * @return true always.
	 */
	@UserThread("MainThread")
	private boolean unloadRegion(Region r) {
		// Perform any operations needed for proper unloading first.
		unloadHandler.accept(r);
		
		// We save the region through the cache before we remove it from
		// primary storage.
		saveRegion(r);
		
		numRegions--;
		
		return true;
	}
	
	@UserThread("MainThread")
	private void setRegionActive(Region r) {
		if(r.active)
			return;
		r.active = true;
		
		// Load all 8 regions adjacent to r.
		for(int x = r.x() - 1; x <= r.x() + 1; x++) {
			for(int y = r.y() - 1; y <= r.y() + 1; y++) {
				if(x != r.x() && y != r.y())
					loadRegion(x, y, false).activeNeighbours++;
			}
		}
	}
	
	@UserThread("MainThread")
	private void setRegionInactive(Region r) {
		if(!r.active)
			return;
		r.active = false;
		
		// Unload all 8 regions adjacent to r.
		for(int x = r.x() - 1; x <= r.x() + 1; x++) {
			for(int y = r.y() - 1; y <= r.y() + 1; y++) {
				if(x != r.x() && y != r.y()) {
					try {
						getRegionAt(x,y).activeNeighbours--;
					} catch(NullPointerException e) {
						// Theoretically the region should never ever be null,
						// but better safe than sorry.
						log.postWarning("Region at (" + x + "," + y + ") is "
								+ "null? src: setRegionInactive");
					}
				}
			}
		}
	}
	
	/**
	 * Caches a region for usage by the current thread. No guarantees are made
	 * as to the state of the returned region, and so it should be assumed that
	 * any interactions with it are not thread-safe.
	 * 
	 * <p>{@link #uncacheAll()} should be invoked once you are done working
	 * with any cached regions to dispose of them.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region.
	 */
	@UserThread("Any")
	public Region cache(int x, int y) {
		List<Region> localRegions = localCachedRegions.get();
		
		// If the region is locally cached by this thread, use it.
		for(Region r : localRegions) {
			if(r.isAt(x, y))
				return r;
		}
		
		// Otherwise, if the region is cached, we locally cache it, and then
		// return it.
		
		CachedRegion regionHandle;
		Point p = cacheLocks.get(x, y);
		
		// Synchronised to make the put-if-absent atomic
		synchronized(p) {
			regionHandle = cache.get(p.set(x, y));
			
			// If the region isn't in the cache, we check to see if the world
			// has it.
			if(regionHandle == null) {
				// Synchronised to make this atomic. See loadRegion().
				synchronized(storeLocks.get(x, y)) {
					// If the region is in primary storage, we take it and put
					// it in the cache. If it isn't, we create the region in
					// the cache.
					Region region = getRegionAt(p); // p is (x,y)
					if(region == null)
						region = new Region(x, y, world.getAge());
					regionHandle = new CachedRegion(region);
					cache.put(region.loc, regionHandle);
				}
			}
			
			regionHandle.mark();
		}
		
		localRegions.add(regionHandle.region);
		
		// We need the region to be loaded such that when we save it we don't
		// lose any old data.
		loader.loadRegion(regionHandle.region, false);
		
		return regionHandle.region;
	}
	
	/**
	 * Uncaches any regions which have been cached by this thread. It is okay
	 * to defensively invoke this method, as this does nothing if no regions
	 * have been cached.
	 */
	@UserThread("Any")
	public void uncacheAll() {
		List<Region> localRegions = localCachedRegions.get();
		for(Region cRegion : localRegions)
			uncache(cRegion);
		localRegions.clear();
	}
	
	/**
	 * Tries to uncache the specified region. If the region is still being used
	 * by another thread, it will remain cached; if not, the region will
	 * be saved and then unloaded.
	 * 
	 * <p>An invocation of this method should have an associated prior call to
	 * {@link #cache(int, int)}.
	 * 
	 * @param r The region to uncache, as returned by {@link #cache(int, int)}.
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("Any")
	private void uncache(Region r) {
		// If the region is no longer being used as a part of the cache, we
		// first save it and then remove it.
		// 
		// To prevent overlapping save and load operations we keep the region
		// in the cache until saving is complete, and then we remove it. To do
		// this, we simply instruct the WorldLoader to invoke dispose() on the
		// handle once it has finished saving.
		synchronized(cacheLocks.get(r.x(), r.y())) {
			CachedRegion cacheHandle = cache.get(r.loc); // never null
			if(cacheHandle.removeMarking())
				loader.saveRegion(r, cacheHandle);
		}
	}
	
	/**
	 * Finalises the uncaching of a cached region. This is invoked by {@link
	 * CachedRegion#dispose()}, which is invoked by the WorldLoader once it
	 * finishes saving a region.
	 * 
	 * @param r The handle to the cached region.
	 */
	@UserThread("WorldLoaderThread")
	private void finaliseUncaching(CachedRegion r) {
		// Implementation note: To avoid a race-condition with loadRegion
		// we'd also want to synchronise on a lock returned by getLock() -
		// otherwise the loadRegion could get a region from getFromCache(),
		// only to have us suddenly remove it. However, we don't care if this
		// happens since it doesn't have any effect.
		boolean removed = false;
		synchronized(cacheLocks.get(r.region.x(), r.region.y())) {
			// Remove the region unless it has been re-cached.
			if(r.tryFinalise()) {
				cache.remove(r.region.loc);
				removed = true;
			}
		}
		
		// We notify any thread which may be waiting in waitUntilDone().
		if(removed && cache.isEmpty()) {
			doneLock.lock();
			try {
				emptyCondition.signalAll();
			} finally {
				doneLock.unlock();
			}
		}
	}
	
	/**
	 * Gets a cached region. This should be accessed within a synchronised
	 * block which holds the monitor on the lock returned by {@link
	 * #getLock(Point) getLock(loc)}.
	 * 
	 * @param loc The coordinates of the region, whose components are in
	 * region-lengths.
	 * 
	 * @return The region, or {@code null} if the region is not cached.
	 * @throws NullPointerException if {@code loc} is {@code null}.
	 */
	@UserThread("MainThread")
	@GuardedBy("getLock()")
	private Region getFromCache(Point loc) {
		CachedRegion cachedRegion = cache.get(loc);
		return cachedRegion == null ? null : cachedRegion.region;
	}
	
	/**
	 * Safely saves a region. This is done through the following steps:
	 * 
	 * <ul>
	 * <li>Store the region in the cache.
	 * <li>If the region is not cached for other purposes, get the WorldLoader
	 *     to save the region.
	 * <li>Remove the region from the cache if it is not being used for other
	 *     purposes.
	 * </ul>
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("MainThread")
	private void saveRegion(Region region) {
		CachedRegion handle;
		boolean save;
		
		// We pretty much dupe the functionality of cache() and uncache() here.
		
		// Synchronised to make the put-if-absent atomic
		synchronized(cacheLocks.get(region.x(), region.y())) {
			handle = cache.get(region.loc);
			// If the region isn't already in the cache, stick it in.
			if(handle == null) {
				handle = new CachedRegion(region);
				cache.put(region.loc, handle);
			}
			
			// Stake our claim, and then swiftly remove it.
			handle.mark();
			save = handle.removeMarking();
		}
		
		if(save)
			loader.saveRegion(region, handle);
	}
	
	/**
	 * Saves all regions in primary storage.
	 */
	public void saveAll() {
		// We could alternatively use this.saveRegion(), but I don't think it's
		// necessary as of this writing.
		regions.values().forEach(r -> loader.saveRegion(r, null));
	}
	
	/**
	 * Returns true iff all regions in primary storage are {@link
	 * Region#isPrepared() prepared}.
	 */
	public boolean isLoaded() {
		for(Region r : regions.values()) {
			if(!r.isPrepared()) {
				//log.postInfo("Not all regions loaded (" + r + ": "
				//		+ r.isLoaded() + ", " + r.isGenerated() + ")");
				return false;
			}
		}
		//log.postInfo("All regions loaded!");
		return true;
	}
	
	/**
	 * Returns the number of regions in primary storage.
	 */
	@UserThread("MainThread")
	@NotThreadSafe
	public int numRegions() {
		return numRegions;
	}
	
	/**
	 * Blocks the current thread until all regions in the cache have finished
	 * saving.
	 */
	public void waitUntilDone() {
		try {
			doneLock.lock();
			
			if(cache.isEmpty())
				return;
			
			// We only wait for a certain number of seconds, since we shouldn't
			// risk deadlock if some bug causes some regions to not be managed
			// properly. Instead we'll just complain about it to the log.
			// TODO: Instead, we could perhaps take charge of unloading them
			// ourselves?
			if(!emptyCondition.await(WAIT_TIME, TimeUnit.SECONDS)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Regions too long to finish saving! "
						+ "Here are our offenders:");
				for(CachedRegion c : cache.values()) {
					sb.append("\n    > ");
					sb.append(c.region.toStringDebug());
				}
				log.postWarning(sb.toString());
			}
		} catch(InterruptedException e) {
			log.postWarning("Interrupted while waiting to finish.");
		} finally {
			doneLock.unlock();
		}
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * Container class for cached regions.
	 * 
	 * <p>All methods of this class should only be invoked while the monitor
	 * lock on one of the {@link RegionStore#cacheLocks} is held.
	 */
	public class CachedRegion {
		
		/** The region. */
		private final Region region;
		/** The number of times the region has been cached. */
		private int timesCached = 0;
		
		
		/**
		 * Creates a new CachedRegion.
		 * 
		 * @param region The region being wrapped.
		 */
		private CachedRegion(Region region) {
			this.region = region;
		}
		
		/**
		 * Marks the region as cached by the current thread.
		 */
		private void mark() {
			timesCached++;
		}
		
		/**
		 * Removes the current thread's marking, and returns {@code true} if
		 * the region is no longer considered marked and should be saved.
		 */
		private boolean removeMarking() {
			// If this is the last mark, we don't remove it; rather, we let the
			// WorldLoader save the region (see uncache()), and then have the
			// final mark safely removed in tryFinalise().
			if(timesCached == 1)
				return true;
			timesCached--;
			return false;
		}
		
		/**
		 * Checks to see if this cached region has been finalised (that is, is
		 * not marked by any thread) and should be removed.
		 * 
		 * @return {@code true} if the region is no longer being cached for any
		 * thread and may be removed; {@code false} if the region is still in
		 * use.
		 */
		private boolean tryFinalise() {
			return --timesCached == 0;
		}
		
		/**
		 * Disposes this cached region. This is invoked by the WorldLoader once
		 * it has finished saving a region.
		 */
		@UserThread("WorldLoaderThread")
		public void dispose() {
			finaliseUncaching(this);
		}
		
	}
	
	/**
	 * Convenience class which extends {@link
	 * com.stabilise.util.concurrent.Striper} to provide {@code get(x, y)}.
	 */
	private class Striper<T> extends com.stabilise.util.concurrent.Striper<T> {
		
		public Striper(IntFunction<T> supplier) {
			super(STRIPE_FACTOR, supplier);
		}
		
		/**
		 * Gets the object corresponding to the specified x and y coordinates.
		 */
		public T get(int x, int y) {
			return get(STRIPE_HASHER.applyAsInt(x, y));
		}
		
	}
	
}
