package com.stabilise.world;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.stabilise.util.Log;
import com.stabilise.util.annotation.GuardedBy;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.LightLinkedList;
import com.stabilise.util.maths.AbstractPoint;
import com.stabilise.util.maths.MutablePoint;
import com.stabilise.util.maths.Point;
import com.stabilise.world.loader.WorldLoader.DimensionLoader;

/**
 * Provides a cache implementation for regions. Every {@link HostWorld} owns a
 * {@code RegionCache}.
 * 
 * <p>A {@code RegionCache} provides secondary storage for regions in a world
 * which can overlap with primary storage. Primary and cache storage together
 * constitute the entirety of all regions in memory. A region should be
 * obtained through the cache rather than the world itself if it is required
 * for anything other than gameplay.
 * 
 * <p>A Region may be cached using {@link #cache(int, int)}. Since this class
 * only cares about the number of threads using a particular region, a thread
 * may invoke {@code cache()} as many times as it pleases. There is no method
 * available to relinquish a particular region; a thread <i>must</i> invoke
 * {@link #uncacheAll()} to release each region it has cached.
 * 
 * <p>The thread-safety of a {@code RegionCache} with respect to its owner
 * {@code HostWorld} is heavily dependent on the {@code HostWorld} class
 * properly using the synchronisation procedures defined in each method's
 * documentation. Furthermore, some methods are designed only to be used by
 * certain classes while following certain protocols. Any deviation from these
 * protocols can cause significant issues, so care <i>must</i> be taken.
 */
public class RegionCache {
	
	/** Wait time in seconds for {@link #waitUntilDone()}. */
	private static final int WAIT_TIME = 5;
	
	private final HostWorld world;
	private DimensionLoader loader = null;
	
	/** The map of cached regions. Maps region.loc -> region. */
	private final ConcurrentHashMap<AbstractPoint, CachedRegion> regions =
			new ConcurrentHashMap<>();
	
	/** Locks used for lock striping when managing cached regions. */
	private final Object[] locks;
	private static final int LOCKS = 4; // Do not modify without checking getLock()
	
	/** A guarded MutablePoint to use for {@link
	 * HostWorld#getRegionAt(AbstractPoint)}. */
	@GuardedBy("locks")
	private final MutablePoint loc = Region.createMutableLoc();
	
	/** Regions which have been cached by the current worker thread. */
	// Note: Sometime in the future it might be a good idea to switch from a
	// List to a Map, especially if some implementation decides to rapid-fire
	// on cache() to fetch tons of different regions.
	private final ThreadLocal<List<Region>> localCachedRegions =
			ThreadLocal.withInitial(() -> new LightLinkedList<>());
	
	// Lock and its associated Condition to wait on in waitUntilDone().
	private final Lock doneLock = new ReentrantLock();
	private final Condition emptyCondition = doneLock.newCondition();
	
	private final Log log;
	
	
	/**
	 * Creates a new region cache.
	 * 
	 * @param world The world to cache regions for.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public RegionCache(HostWorld world) {
		this.world = Objects.requireNonNull(world);
		
		locks = new Object[LOCKS];
		for(int i = 0; i < LOCKS; i++)
			locks[i] = new Object();
		
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
			if(r.isAt(x, y));
				return r;
		}
		
		// Otherwise, if the region is cached, we local-cache and return it.
		
		CachedRegion regionHandle;
		
		// Synchronised to make the put-if-absent atomic
		synchronized(regions) {
			regionHandle = regions.get(Region.createLoc(x, y));
			
			// If the region isn't in the cache, we check to see if the world
			// has it.
			if(regionHandle == null) {
				// Synchronise on a public lock to make this atomic. See
				// HostWorld.loadRegion()
				synchronized(getLock(x, y)) {
					// If the world has the region, we take it and put it in
					// the cache. If the world doesn't have it, we create the
					// region in the cache.
					Region region = world.getRegionAt(loc.set(x,y));
					if(region == null)
						region = new Region(x, y, world.getAge());
					regionHandle = new CachedRegion(region);
					regions.put(region.loc, regionHandle);
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
		// this, we simply instruct the WorldLoader to invoke
		// finaliseUncaching() once it has finished saving.
		synchronized(regions) {
			CachedRegion cacheHandle = regions.get(r.loc);
			if(cacheHandle == null)
				throw new AssertionError(r + " is not in the cache?");
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
		// Implementation note: To avoid a race-condition with
		// HostWorld.loadRegion we'd also want to synchronise on a lock
		// returned by getLock() - otherwise the HostWorld could get a region
		// from get(), only to have us suddenly remove it. We don't care if
		// this happens since it doesn't have any effect.
		boolean removed = false;
		synchronized(regions) {
			// Remove the region unless it has been re-cached.
			if(r.tryFinalise()) {
				regions.remove(r.region.loc);
				removed = true;
			}
		}
		
		// We notify any thread which may be waiting in waitUntilDone().
		if(removed && regions.isEmpty()) {
			try {
				doneLock.lock();
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
	public Region get(AbstractPoint loc) {
		// Note: do not under any circumstances try to synchronise on regions
		// here since it can lead to deadlock (see HostWorld.loadRegion() and
		// cache()).
		CachedRegion cachedRegion = regions.get(loc);
		return cachedRegion == null ? null : cachedRegion.region;
	}
	
	/**
	 * Gets the lock upon which to synchronise when accessing a cached region.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The object lock.
	 */
	public Object getLock(int x, int y) {
		// We use the lowest bit in both x and y to return one of 4 locks.
		return locks[((x & 1) << 1) | (y & 1)];
	}
	
	/**
	 * Gets the lock upon which to synchronise when accessing a cached region.
	 * 
	 * <p>Invoking this is equivalent to invoking {@link #getLock(int, int)
	 * getLock(r.loc.x, r.loc.y)}.
	 * 
	 * @return The object lock.
	 */
	public Object getLock(Region r) {
		return getLock(r.loc.x, r.loc.y);
	}
	
	/**
	 * Safely saves a region. This is done through the following steps:
	 * 
	 * <ul>
	 * <li>Store the region in this cache.
	 * <li>If the region is not cached for other purposes, get the WorldLoader
	 *     to save the region.
	 * <li>Remove the region from the cache if it is not being used for other
	 *     purposes.
	 * </ul>
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("MainThread")
	public void saveRegion(Region region) {
		// We pretty much dupe the functionality of cache() and uncache() here.
		
		// Synchronised to make the put-if-absent atomic
		synchronized(regions) {
			CachedRegion handle = regions.get(region.loc);
			// If the region is not cached by the world generator, get it from
			// the world.
			if(handle == null) {
				handle = new CachedRegion(region);
				regions.put(region.loc, handle);
			}
			
			// Stake our claim, and then swiftly remove it.
			handle.mark();
			if(handle.removeMarking())
				loader.saveRegion(region, handle);
		}
	}
	
	/**
	 * Blocks the current thread until all regions in the cache have finished
	 * saving.
	 */
	public void waitUntilDone() {
		try {
			doneLock.lock();
			
			if(regions.isEmpty())
				return;
			
			// We only wait for a certain number of seconds, since we shouldn't
			// risk deadlock if some bug causes some regions to not be managed
			// properly. Instead we'l just complain about it to the log.
			// TODO: Instead, we could perhaps take charge of unloading them
			// ourselves?
			if(!emptyCondition.await(WAIT_TIME, TimeUnit.SECONDS)) {
				StringBuilder sb = new StringBuilder();
				sb.append("Regions too long to finish saving! "
						+ "Here are our offenders:");
				for(CachedRegion c : regions.values()) {
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
	 * lock on {@link RegionCache#regions} is held.
	 */
	public class CachedRegion {
		
		/** The region. */
		private final Region region;
		/** The number of times the region has been cached. */
		@GuardedBy("RegionCache.regions") private int timesCached = 0;
		
		
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
		@GuardedBy("RegionCache.regions")
		private void mark() {
			timesCached++;
		}
		
		/**
		 * Removes the current thread's marking, and returns {@code true} if
		 * the region is no longer considered marked and should be saved.
		 */
		@GuardedBy("RegionCache.regions")
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
		@GuardedBy("RegionCache.regions")
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
	
}
