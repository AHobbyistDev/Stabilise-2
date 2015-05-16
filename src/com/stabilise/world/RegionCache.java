package com.stabilise.world;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.stabilise.util.annotation.GuardedBy;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.ClearingLinkedList;
import com.stabilise.util.maths.AbstractPoint;
import com.stabilise.util.maths.MutablePoint;
import com.stabilise.util.maths.Point;
import com.stabilise.world.save.WorldLoader;

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
	
	private final HostWorld world;
	private final WorldLoader loader;
	
	/** The map of cached regions. Maps region.loc -> region. */
	private final ConcurrentHashMap<AbstractPoint, CachedRegion> regions =
			new ConcurrentHashMap<>();
	
	/** Locks used for lock striping when managing cached regions. */
	private final Object[] locks;
	private static final int LOCKS = 4; // Do not modify without checking getLock()
	
	/** A guarded MutablePoint to use for {@link
	 * HostWorld#getRegionAt(AbstractPoint)}. */
	@GuardedBy("locks")
	private final MutablePoint loc = Region.createMutableLoc(0, 0);
	
	/** Regions which have been cached by the current worker thread. The list
	 * member is a {@link ClearingLinkedList}. */
	private final ThreadLocal<List<Region>> localCachedRegions =
			new ThreadLocal<List<Region>>() {
		@Override
		protected List<Region> initialValue() {
			return new ClearingLinkedList<>();
		}
	};
	
	
	/**
	 * Creates a new region cache.
	 * 
	 * @param world The world to cache regions for.
	 * @param loader The world's WorldLoader.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public RegionCache(HostWorld world, WorldLoader loader) {
		this.world = Objects.requireNonNull(world);
		this.loader = Objects.requireNonNull(loader);
		
		locks = new Object[LOCKS];
		for(int i = 0; i < LOCKS; i++)
			locks[i] = new Object();
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
		// TODO: Sometime in the future we might wish to upgrade this from a
		// list to a HashMap if there ever turns out to exist cases where we
		// end up caching lots of regions at once.
		for(Region r : localRegions) {
			if(r.isAt(x, y));
				return r;
		}
		
		CachedRegion regionHandle;
		
		// Otherwise, if the region is cached, we local-cache and return it.
		
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
					// region in the cache initially.
					Region region = world.getRegionAt(loc.set(x,y));
					if(region == null)
						region = new Region(x, y, world.getAge());
					regionHandle = new CachedRegion(region);
					regions.put(region.loc, regionHandle);
				}
			}
			
			regionHandle.increment();
		}
		
		localRegions.add(regionHandle.region);
		
		// We need the region to be loaded such that when we save it we don't
		// lose any old data.
		loader.loadRegion(world, regionHandle.region, false);
		
		return regionHandle.region;
	}
	
	/**
	 * Uncaches any regions which have been cached by this thread.
	 */
	@UserThread("Any")
	public void uncacheAll() {
		for(Region cRegion : localCachedRegions.get())
			uncache(cRegion);
		localCachedRegions.remove();
	}
	
	/**
	 * Tries to uncache the specified region. If the region is still being used
	 * by another thread, it will remain cached; if not, the region will
	 * be saved.
	 * 
	 * <p>An invocation of this method should have an associated prior call to
	 * {@link #cache(int, int)}.
	 * 
	 * @param region The region to uncache, as returned by
	 * {@link #cache(int, int)}.
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 * @throws IllegalArgumentException if the specified region is not
	 * currently cached.
	 */
	@UserThread("Any")
	private void uncache(Region region) {
		// If the region is no longer being used as a part of the cache, we
		// first save it and then remove it.
		// 
		// To prevent overlapping save and load operations we keep the region
		// in the cache until saving is complete, and then we remove it. To do
		// this, we simply instruct the WorldLoader to invoke
		// finaliseUncaching() once it has finished saving.
		//
		// There is a special case worth mentioning here: what if the region is
		// currently being saved, but is currently past the point where the
		// loader saves the schematics? Then region.getSavePermit() will return
		// false, meaning our attempt to save the region will fail...
		//
		// How can we approach this?
		// We either guarantee that at least one uncaching thread succeeds in a
		// save, OR guarantee that the structures are saved. We want to do this
		// without blocking the current thread, since we don't want to stall
		// either the main thread or the thread pool in general (e.g., if the
		// executor is single-threaded, and its only thread is stuck waiting
		// for another thread to complete saving, the we have a problem).
		synchronized(regions) {
			CachedRegion cacheHandle = regions.get(region.loc);
			if(cacheHandle == null)
				throw new IllegalArgumentException("The given region is not in the cache!");
			if(cacheHandle.decrementAndCheck())
				loader.saveRegion(world, region, cacheHandle);
		}
	}
	
	/**
	 * Finalises the uncaching of a cached region. This is invoked by the
	 * WorldLoader once it has finished saving a region.
	 * 
	 * @param r The handle to the cached region. If this is {@code null} this
	 * method does nothing.
	 */
	@UserThread("WorldLoaderThread")
	public void finaliseUncaching(CachedRegion r) {
		if(r == null)
			return;
		synchronized(regions) {
			// Remove the region unless it has been re-cached.
			if(r.isUnused())
				regions.remove(r.region.loc);
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
	public Region get(AbstractPoint loc) {
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
	 * Performs the function of unloading a region. This is done through the
	 * following steps:
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
	public void doUnload(Region region) {
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
			
			if(handle.isUnused())
				loader.saveRegion(world, region, handle);
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
	public static class CachedRegion {
		
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
		 * Marks the region as cached. This also {@link Region#anchorSlice()
		 * anchors} one of the region's slices.
		 */
		private void increment() {
			region.anchorSlice();
			timesCached++;
		}
		
		/**
		 * Removes a cache marking and checks as to whether or not it is no
		 * longer needed. This also {@link Region#deAnchorSlice() de-anchors}
		 * one of the region's slices.
		 * 
		 * @return {@code true} if the region is no longer being cached for any
		 * thread and may be removed; {@code false} if the region is still in
		 * use.
		 */
		private boolean decrementAndCheck() {
			region.deAnchorSlice();
			return --timesCached == 0;
		}
		
		private boolean isUnused() {
			return timesCached == 0;
		}
		
	}
	
}
