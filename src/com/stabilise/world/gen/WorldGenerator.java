package com.stabilise.world.gen;

import static com.stabilise.world.Region.REGION_SIZE;
import static com.stabilise.world.Region.REGION_SIZE_IN_TILES;
import static com.stabilise.world.Slice.SLICE_SIZE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.stabilise.util.Log;
import com.stabilise.util.TaskTimer;
import com.stabilise.util.annotation.ThreadSafe;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.util.collect.ClearOnIterateLinkedList;
import com.stabilise.util.maths.HashPoint;
import com.stabilise.util.maths.Maths;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.Schematic;
import com.stabilise.world.Slice;
import com.stabilise.world.provider.WorldProvider;
import com.stabilise.world.tile.Tiles;

/**
 * The {@code WorldGenerator} class provides the mechanism for generating the
 * terrain of a world.
 * 
 * <h3>Standard Usage Guidelines</h3>
 * 
 * <p>To obtain the {@code WorldGenerator} instance to use for a world, simply
 * invoke {@link #getGenerator(WorldData)}.
 * 
 * <p>To generate a region, invoke {@link #generate(Region)} on the {@code
 * WorldGenerator} instance. When the specified region's {@link
 * Region#generated generated} flag is set to {@code true}, generation of the
 * region has completed and it is safe to interact with the region.
 * 
 * <p>{@link #generateSynchronously(Region)} is provided as a convenience
 * alternative to {@link #generate(Region)} if it is known that the thread
 * invoking it will not suffer from the prolonged blocking that generation
 * entails (as a rule of thumb, it is acceptable invoke {@code
 * generateSynchronously} on any thread but the main thread).
 * 
 * <p>To shut down the {@code WorldGenerator}, invoke {@link #shutdown()} and
 * then {@code shutdown()} on {@link WorldData#executor data.executor}, where
 * {@code data} is the {@code WorldData} object passed to {@code getGenerator}.
 * 
 * <h3>Implementation Details</h3>
 * 
 * <p>Each invocation of {@code generate} either creates a new thread or reuses
 * an existing thread created for generation, loading, or saving, depending on
 * the {@code ExecutorService} contained in the given {@code WorldData} object.
 * This ensures maximum generation throughput in that every region should be
 * able to generate concurrently.
 * 
 * <p>The generation of a region may entail that any number of other regions
 * are cached by the generator to enable inter-region generation, specifically
 * in the case of certain 'structures', or {@link Schematic schematics}, which
 * may be generated across region boundaries. These regions are obtained from
 * the world via invocation of {@link HostWorld#getRegionAt(int, int)
 * getRegionAt}, and have a slice marked as per {@link Region#anchorSlice()} to
 * indicate that they are being used. {@link Region#deAnchorSlice()} is invoked
 * when the region is no longer being used. If {@code getRegionAt} returns
 * {@code null}, the WorldGenerator will instantiate the region itself. Cached
 * regions may be accessed through {@link #getCachedRegion(int, int)}, and it
 * is advisable for the world to invoke this before instantiating regions to
 * prevent duplicate and inconsistent regions from being created.
 * 
 * <p>Cached regions may have schematics implanted via {@link
 * Region#queueSchematic(String, int, int, int, int, int, int) queueSchematic},
 * and these schematics will be added to the cached region immediately if it
 * has already been generated, or as the region is being normally generated if
 * it has not yet been generated.
 * 
 * <h3>Guidelines for Subclasses of WorldGenerator</h3>
 * 
 * <p>Generators are advised to keep the landform within the range
 * {@code -256 < y < 256} at and around {@code x = 0} for spawning purposes.
 */
@ThreadSafe
public abstract class WorldGenerator {
	
	/** The world provider. */
	private final WorldProvider<?> prov;
	/** The world for which the generator is generating. */
	private final HostWorld world;
	/** The seed to use for world generation. */
	protected final long seed;
	
	/** The executor which delegates threads. */
	private final Executor executor;
	/** Whether or not the generator has been shut down. This is volatile. */
	private volatile boolean isShutdown = false;
	
	/** A map of region points to each region's lock. A region's {@link
	 * Region#loc loc} member should be used as its key. */
	private final ConcurrentHashMap<HashPoint, RegionLock> regionLocks =
			new ConcurrentHashMap<>();
	
	/** A map of regions which have been cached by the world generator. A
	 * region's {@link Region#loc loc} member should be used as its key.*/
	private final ConcurrentHashMap<HashPoint, CachedRegion> cachedRegions =
			new ConcurrentHashMap<>();
	
	/** Locks used for lock striping when managing cached regions. */
	private final Object[] locks;
	private static final int LOCKS = 4; // Do not modify this without first checking getLock()
	
	/** Regions which have been cached by the current worker thread. The list
	 * member is a {@link ClearOnIterateLinkedList}. */
	private final ThreadLocal<List<Region>> localCachedRegions = new ThreadLocal<List<Region>>() {
		@Override
		protected List<Region> initialValue() {
			return new ClearOnIterateLinkedList<Region>();
		}
	};
	
	/** The cache of schematics in use. TODO: this system is outdated */
	private final Map<String, Schematic> schematics = new ConcurrentHashMap<>(5);
	
	/** The generator's log. */
	protected final Log log = Log.getAgent("GENERATOR");
	
	
	/**
	 * Creates a new WorldGenerator.
	 * 
	 * @param worldProv The world provider. This should be a {@code
	 * HostProvider} for all but a {@code PrivateDimensionGenerator}.
	 * @param world The world.
	 */
	protected WorldGenerator(WorldProvider<?> worldProv, HostWorld world) {
		this.prov = worldProv;
		this.world = world;
		this.executor = prov.getExecutor();
		
		seed = prov.getSeed();
		
		locks = new Object[LOCKS];
		for(int i = 0; i < LOCKS; i++)
			locks[i] = new Object();
	}
	
	/**
	 * Instructs the WorldGenerator to generate the given region.
	 * 
	 * <p>Regions which are ineligible for generation will ignored. That is,
	 * the given region will not be generated if:
	 * 
	 * <ul>
	 * <li>it has not been loaded (i.e. {@link Region#loaded region.loaded}
	 *     is {@code false}).
	 * <li>it has already been generated (i.e. {@link
	 *     Region#isGenerated() region.isGenerated()} is {@code true}).
	 * <li>it is being generated concurrently.
	 * </ul>
	 * 
	 * @param region The region to generate.
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("MainThread")
	public final void generate(final Region region) {
		// This method should return from here in most cases
		if(region.isGenerated() || !region.loaded)
			return;
		
		// Abort if the region is being generated:
		// Obtaining the region here prevents a large flux of calls (e.g.
		// when the world is initially loaded) from each creating a new
		// request and cluttering the executor
		if(isGenerating(region) || !tryObtainRegion(region))
			return;
		
		executor.execute(new Runnable() {
			@Override
			public void run() {
				genRegion(region, false);
			}
		});
	}
	
	/**
	 * Instructs the WorldGenerator to generate the given region on the current
	 * thread. Unlike {@link #generate(Region)}, this method does not ignore
	 * the instruction if the region is currently generating (though it will if
	 * <tt>{@link Region#isGenerated() region.isGenerated()} == true</tt>);
	 * this is because it is expected for this method to be invoked only by the
	 * WorldLoader once it finishes loading a region, and hence it is
	 * guaranteed that {@code region} is not currently being generated.
	 * 
	 * @param region The region to generate.
	 * 
	 * @throws NullPointerException if {@code region} is {@code null}.
	 */
	@UserThread("WorkerThread")
	public final void generateSynchronously(Region region) {
		if(!region.isGenerated() && obtainRegion(region))
			genRegion(region, false);
	}
	
	/**
	 * Generates a region. The lock for the region must be obtained before this
	 * method is invoked.
	 * 
	 * @param r The region to generate.
	 * @param cached Whether or not the region should be treated as a cached
	 * region.
	 * 
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	@UserThread("WorkerThread")
	private void genRegion(Region r, boolean cached) {
		RegionLock l = regionLocks.get(r.loc); // shouldn't be null
		
		if(isShutdown) {
			releaseRegion(r);
			if(cached)
				uncacheRegion(r);
			return;
		}
		
		//log.logMessage("Generating " + r);
		
		TaskTimer timer = new TaskTimer("Generating " + r);
		timer.start();
		
		boolean changes = false; // whether or not anything was changed during gen
		
		try {
			// Skip over this part if the region has already been generated
			if(!r.generated) {
				// Set up the region's slices
				for(int y = 0; y < REGION_SIZE; y++) {
					for(int x = 0; x < REGION_SIZE; x++) {
						r.slices[y][x] = new Slice(
								x + r.loc.x * REGION_SIZE,
								y + r.loc.y * REGION_SIZE,
								new int[SLICE_SIZE][SLICE_SIZE] // all values are 0 == Tiles.AIR
						);
					}
				}
				
				// Generate the region, as implemented in subclasses
				generateRegion(r);
				
				changes = true;
			}
			
			// After normal generation processes have been completed, add any
			// queued schematics.
			
			// Notifies other gen threads that this region is now beyond the
			// point where schematics can be queued and still generated.
			l.schematicsPlaced = true;
			
			Region.QueuedSchematic[] rSchematics = r.portSchematics();
			changes |= rSchematics.length > 0;
			
			// Now add the schematics
			for(Region.QueuedSchematic s : rSchematics)
				addSchematicAt(
						r, s.schematicName,
						s.sliceX, s.sliceY,
						s.tileX, s.tileY,
						SchematicParams.scheduledGenParams(s.offsetX, s.offsetY)
				);
			
			timer.stop();
			log.postDebug(timer.getResult(TimeUnit.MILLISECONDS));
			
			r.generated = true;
		} catch(Throwable t) {
			log.postSevere("Worldgen of " + r + " failed!", t);
		} finally {
			// Enclosed in a finally block in case generateRegion() disobeys
			// the order to not throw an exception or error.
			releaseRegion(r);
		}
		
		// Save the region once it's done generating, but don't burden the
		// world loader if nothing was changed
		if(changes || cached)
			prov.loader.saveRegion(world, r);
		if(cached)
			uncacheRegion(r);
		
		// Now that generation is done, we will perform the following with each
		// of the cached regions:
		//   > If the region has already been generated (i.e. generated==true),
		//     then pass that region through to this method, which simply adds
		//     any queued schematics to it (since now is a convenient time to
		//     implant schematics), and then uncache the region from the world.
		// Finally, we clear the threadlocal instance.
		for(Region cRegion : localCachedRegions.get()) {
			RegionLock cLock = regionLocks.get(cRegion.loc); // may be null
			
			// If the region is generated for the most part, implant the
			// schematics
			
			// Whether or not we can treat the cached region as generated. If
			// we can treat it as generated, then we can implant schematics.
			// TODO: Verify that in all cases this check works as if it is
			// atomic.
			boolean generated = cRegion.generated || (cLock != null && cLock.schematicsPlaced);
			
			if(generated && cRegion.hasQueuedSchematics()) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						if(obtainRegion(cRegion))
							genRegion(cRegion, true);
						else // game being shut down; save what we can
							prov.loader.saveRegion(world, r);
					}
				});
			} else {
				// Save the region here since the current implementation of
				// HostWorld prevents it from saving non-generated regions
				prov.loader.saveRegion(world, cRegion);
				uncacheRegion(cRegion);
			}
		}
		localCachedRegions.remove();
	}
	
	/**
	 * Generates a region.
	 * 
	 * <p>The general contract of this method is that it may modify the
	 * contents of any slice in the given region - namely, it may set tiles and
	 * tile entities, and add schematics. Leaving this method blank is
	 * equivalent to setting every tile in the region to {@link Tiles#AIR}.
	 * 
	 * <p>Subclasses of WorldGenerator should note that this method may be
	 * invoked by various worker threads concurrently, and it is hence the
	 * responsibility of implementors to ensure correct thread safety
	 * techniques are observed.
	 * 
	 * @param r The region to generate.
	 */
	@UserThread("WorkerThread")
	protected abstract void generateRegion(Region r);
	
	/**
	 * Adds a schematic to the world.
	 * 
	 * @param r The region to add the schematic to.
	 * @param name The name of the schematic to add.
	 * @param sliceX The x-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param sliceY The y-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param tileX The x-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param tileY The y-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param params The parameters to use for adding the schematic.
	 */
	@UserThread("WorkerThread")
	protected final void addSchematicAt(Region r, String name, int sliceX, int sliceY, int tileX, int tileY, SchematicParams params) {
		addSchematicAt(r, getSchematic(name), sliceX, sliceY, tileX, tileY, params);
	}
	
	/**
	 * Adds a schematic to the world.
	 * 
	 * @param r The region to add the schematic to.
	 * @param sc The name of the schematic to add.
	 * @param sliceX The x-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param sliceY The y-coordinate of the slice in which to place the
	 * schematic, relative to the region, in slice-lengths.
	 * @param tileX The x-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param tileY The y-coordinate of the tile at which to place the
	 * schematic, relative to the slice, in tile-lengths.
	 * @param params The parameters to use for adding the schematic.
	 * 
	 * @throws IllegalArgumentException Thrown if the schematic's origin has
	 * negative coordinates.
	 */
	@UserThread("WorkerThread")
	protected final void addSchematicAt(Region r, Schematic sc, int sliceX, int sliceY, int tileX, int tileY, SchematicParams params) {
		if(sc == null)
			return;
		
		//log.logMessage("Adding " + sc + " to " + r);
		
		// Don't allow negative pivots; they'll mess up our algorithm
		if(sc.x < 0 || sc.y < 0)
			throw new IllegalArgumentException(sc + " has a pivot in an invalid location! (" + sc.x + "," + sc.y + ")");
		
		//int offsetX = ((tileX + sliceX * SLICE_SIZE - sc.x) % (REGION_SIZE_IN_TILES));
		
		// Inform neighbouring regions of the schematic if the schematic overlaps into them
		if(params.informRegions) {
			for(int regionY = r.loc.y + params.offsetY + ((tileY + sliceY * SLICE_SIZE - sc.y) / (REGION_SIZE_IN_TILES));
					regionY <= r.loc.y + params.offsetY + ((tileY + sliceY * SLICE_SIZE - sc.y + sc.height) / (REGION_SIZE_IN_TILES));
					regionY++) {
				for(int regionX = r.loc.x + params.offsetX + ((tileX + sliceX * SLICE_SIZE - sc.x) / (REGION_SIZE_IN_TILES));
						regionX <= r.loc.x + params.offsetX + ((tileX + sliceX * SLICE_SIZE - sc.x + sc.width) / (REGION_SIZE_IN_TILES));
						regionX++) {
					if(regionY != r.loc.y || regionX != r.loc.x) {
						Region cachedRegion = cacheRegion(regionX, regionY);
						cachedRegion.queueSchematic(sc.name, sliceX, sliceY, tileX, tileY, r.loc.x - regionX, r.loc.y - regionY);
						cachedRegion.unsavedChanges = true; // TODO: thread safety on this
					}
				}
			}
		}
		
		// Get the bottom-left corner tile of the region from which the schematic will originate
		int initialX = sliceX * SLICE_SIZE + tileX - sc.x + params.offsetX * REGION_SIZE_IN_TILES;
		int initialY = sliceY * SLICE_SIZE + tileY - sc.y + params.offsetY * REGION_SIZE_IN_TILES;
		
		int x, y;
		
		// If the bottom-left corner tile is within the region, start from there
		// If not, we'll start generating from the bottom and/or left boundary of the region, and
		// skip to the first part of the schematic that is within the region
		if(initialX >= 0) {
			tileX = (initialX % REGION_SIZE_IN_TILES) % SLICE_SIZE;
			sliceX = Maths.floor(((initialX / SLICE_SIZE)) % REGION_SIZE);
			x = 0;
		} else {
			tileX = 0;
			sliceX = 0;
			x = -initialX;
		}
		
		if(initialY >= 0) {
			tileY = (initialY % REGION_SIZE_IN_TILES) % SLICE_SIZE;
			sliceY = Maths.floor(((initialY / SLICE_SIZE)) % REGION_SIZE);
			y = 0;
		} else {
			tileY = 0;
			sliceY = 0;
			y = -initialY;
		}
		
		// tileX, sliceX and x need resetting every time it loops to the next row
		int defaultTileX = tileX;
		int defaultSliceX = sliceX;
		initialX = x;
		
		Slice s = r.getSliceAt(sliceX, sliceY);
		
		for(; y < sc.height; y++) {
			for(; x < sc.width; x++) {
				
				int tile = sc.getTileAt(x, y);
				if(tile != -1 && (params.copyAir || tile != 0))
					s.setTileAt(tileX, tileY, tile);
				
				tileX++;
				
				if(tileX == SLICE_SIZE) {
					sliceX++;
					tileX = 0;
					if(sliceX == REGION_SIZE)
						break;
					s = r.getSliceAt(sliceX, sliceY);
				}
			}
			
			tileX = defaultTileX;
			sliceX = defaultSliceX;
			x = initialX;
			
			tileY++;
			
			if(tileY == SLICE_SIZE) {
				sliceY++;
				tileY = 0;
				if(sliceY == REGION_SIZE)
					break;
			}
			
			// Reset the slice
			s = r.getSliceAt(sliceX, sliceY);
		}
	}
	
	/**
	 * Gets a schematic.
	 * 
	 * @param schematic The schematic's name.
	 * 
	 * @return The schematic, or {@code null} if a schematic with the given
	 * name could not be found.
	 */
	@UserThread("WorkerThread")
	private Schematic getSchematic(String schematic) {
		// Weakly synchronised through use of a ConcurrentHashMap - this is
		// sufficient for now (it doesn't really matter if a map entry is
		// overwritten by an equivalent schematic)
		if(schematics.containsKey(schematic)) {
			return schematics.get(schematic);
		} else {
			Schematic s = new Schematic(schematic);
			try {
				s.load();
			} catch(IOException e) {
				log.postWarning("Could not load schematic \"" + schematic + "\"!", e);
				return null;
			}
			schematics.put(schematic, s);
			return s;
		}
	}
	
	/**
	 * Caches a region for referencing during world generation. Note that the
	 * returned region may currently be in the process of generating.
	 * 
	 * <p>It is imperative that the region is uncached as per an invocation of
	 * {@link #uncacheRegion(Region)} once the returned region is no longer
	 * needed.
	 * 
	 * @param x The x-coordinate of the region, in region-lengths.
	 * @param y The y-coordinate of the region, in region-lengths.
	 * 
	 * @return The region.
	 */
	@UserThread("WorkerThread")
	private Region cacheRegion(int x, int y) {
		List<Region> cRegions = localCachedRegions.get();
		
		// If the region is already cached by this thread, use it
		for(Region r : cRegions) {
			if(r.loc.equals(x, y));
				return r;
		}
		
		CachedRegion cachedRegion;
		boolean wasUncached = false;
		
		// Otherwise, if the region is already cached by the world generator,
		// use it.
		
		HashPoint loc = Region.getKey(x, y);
		
		// Synchronised to make the put-if-absent atomic
		synchronized(cachedRegions) {
			cachedRegion = cachedRegions.get(loc);
			// If the region is not cached by the world generator, get it from
			// the world
			if(cachedRegion == null) {
				wasUncached = true;
				// Synchronise on a public lock to make this atomic. See
				// HostWorld.loadRegion()
				synchronized(getLock(loc)) {
					Region region = world.getRegionAt(loc);
					if(region == null)
						region = new Region(loc, world.getAge());
					cachedRegion = new CachedRegion(region);
					cachedRegions.put(loc, cachedRegion);
				}
			}
			
			cachedRegion.increment();
		}
		
		cRegions.add(cachedRegion.region);
		
		// No guarantees are made as to whether or not the region is loaded, so
		// perform a loading operation here.
		if(wasUncached) {
			prov.loader.loadRegionSynchronously(world, cachedRegion.region);
			// In case it is being loaded due to another thread's actions..
			cachedRegion.region.waitUntilLoaded();
		}
		
		return cachedRegion.region;
	}
	
	/**
	 * Tries to uncache the specified region. If the region is still being used
	 * by another worker thread, it will remain cached; if not, the region will
	 * be saved as per {@link
	 * com.stabilise.world.save.WorldLoader#saveRegionSynchronously(Region)
	 * WorldLoader.saveRegionSynchronously(Region)}.
	 * 
	 * <p>An invocation of this method should have an associated prior call to
	 * {@link #cacheRegion(int, int)}.
	 * 
	 * @param region The region to uncache, as returned by
	 * {@link #cacheRegion(int, int)}.
	 */
	@UserThread("WorkerThread")
	private void uncacheRegion(Region region) {
		// Synchronised to make this atomic
		synchronized(cachedRegions) {
			if(cachedRegions.get(region.loc).decrementAndCheck())
				cachedRegions.remove(region.loc);
			else
				return;	
		}
		prov.loader.saveRegionSynchronously(world, region);
	}
	
	/**
	 * Gets a region cached by the world generator. This should be accessed
	 * within a synchronised block which holds the monitor on the lock returned
	 * by {@link #getLock(HashPoint) getLock(loc)}.
	 * 
	 * @param loc The region's location, whose coordinates are in region-
	 * lengths.
	 * 
	 * @return The region, or {@code null} if the region is not cached.
	 */
	@UserThread("MainThread")
	public Region getCachedRegion(HashPoint loc) {
		CachedRegion cachedRegion = cachedRegions.get(loc);
		return cachedRegion == null ? null : cachedRegion.region;
	}
	
	/**
	 * Gets the lock upon which to synchronise when atomically accessing a
	 * region cached by the world generator.
	 * 
	 * @param loc The region's location, whose coordinates are in region-
	 * lengths.
	 * 
	 * @return The object lock.
	 * @throws NullPointerException if {@code loc} is {@code null}.
	 */
	public Object getLock(HashPoint loc) {
		// We use the lowest two bits holding whether or not x and y are odd
		// or even numbers. This creates a repeating 2x2 grid such that
		// adjacent regions do not use the same lock. This can be made
		// explicit:
		// locks[0] (00) if x is even and y is even
		// locks[1] (01) if x is even and y is odd
		// locks[2] (10) if x is odd and y is even
		// locks[3] (11) if x is odd and y is odd
		return locks[((loc.x & 1) << 1) | (loc.y & 1)];
	}
	
	/**
	 * Checks for whether or not a region is currently being generated.
	 * 
	 * @return {@code true} if the region is being generated; {@code false}
	 * otherwise.
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	private boolean isGenerating(Region r) {
		RegionLock l = regionLocks.get(r.loc);
		return l != null && l.isLocked();
	}
	
	/**
	 * Prepares the RegionLock instance associated with a region. This should
	 * only be invoked once per gen attempt of a region - that is, only if it
	 * is guaranteed that either {@code lock()} or {@code tryLock()} will be
	 * invoked on the returned RegionLock.
	 * 
	 * @return The region's lock.
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	private RegionLock prepareLock(Region r) {
		RegionLock l;
		// Synchronised to make this atomic. See releaseRegion().
		// Is there a way to avoid overt synchronisation?
		synchronized(r) {
			l = regionLocks.get(r.loc);
			if(l == null) {
				l = new RegionLock();
				regionLocks.put(r.loc, l);
			}
			// Pre-lock while synced so isUnused() returns false in releaseRegion
			l.preLock();
		}
		return l;
	}
	
	/**
	 * Acquires the permit to generate the given region. If this method returns
	 * {@code true}, it is safe to generate the region.
	 * 
	 * <p>To release the generation permit once generation is complete, use
	 * {@link #releaseRegion(Region) releaseRegion(r)}. Every invocation of
	 * this method should have an associated call to {@code releaseRegion} for
	 * <i>all</i> code paths.
	 * 
	 * @param r The region.
	 * 
	 * @return {@code true} if the permit was acquired; {@code false} if the
	 * current thread was interrupted while waiting to acquire the permit.
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	private boolean obtainRegion(Region r) {
		// If this fails due to interruption, don't bother removing the lock
		// through releaseRegion(r) since this thread being interrupted means
		// the world generator is being shutdown, in which case the lock map
		// will be GC'd anyway.
		return prepareLock(r).lock();
	}
	
	/**
	 * Attempts to acquire the permit to generate the given region. If this
	 * method returns {@code true}, it is safe to generate the region.
	 * 
	 * <p>To release the generation permit once generation is complete, use
	 * {@link #releaseRegion(Region) releaseRegion(r)}. Every invocation of
	 * this method which returns {@code true} should have an associated call to
	 * {@code releaseRegion} for <i>all</i> code paths.
	 * 
	 * @return {@code true} if the permit was acquired; {@code false}
	 * otherwise.
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	private boolean tryObtainRegion(Region r) {
		return prepareLock(r).tryLock();
	}
	
	/**
	 * Releases the permit to generate the given region.
	 * 
	 * @param r The region.
	 * 
	 * @throws NullPointerException if {@code r} is {@code null}.
	 */
	private void releaseRegion(Region r) {
		// No need to check to see if this is null; no thread should remove the
		// mapping while the lock is obtained
		RegionLock l = regionLocks.get(r.loc);
		l.unlock();
		// Synchronised to make this atomic. See prepareLock().
		synchronized(r) {
			if(l.isUnused())
				regionLocks.remove(r.loc);
		}
	}
	
	/**
	 * Instructs the WorldGenerator to shut down.
	 */
	@UserThread("MainThread")
	public final void shutdown() {
		isShutdown = true;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A class containing parameters for placing schematics in the world.
	 */
	protected static class SchematicParams {
		
		/** Whether or not to copy air tiles from the schematic. */
		protected final boolean copyAir;
		/** Whether or not to inform neighbouring regions of the schematic
		 * needing to be generated on their sides of a region boundary. */
		protected final boolean informRegions;
		/** The x-offset of the schematic's origin, in region-lengths. */
		protected final int offsetX;
		/** The y-offset of the schematic's origin, in region-lengths. */
		protected final int offsetY;
		
		
		/**
		 * Creates a new object containing parameters for schematic placement.
		 * 
		 * @param copyAir Whether or not to copy air tiles.
		 * @param informRegions Whether or not to carry generation over to
		 * neighbouring regions.
		 */
		protected SchematicParams(boolean copyAir, boolean informRegions, int offsetX, int offsetY) {
			this.copyAir = copyAir;
			this.informRegions = informRegions;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
		// Static factories
		
		/**
		 * Gets a SchematicParams object suitable for default cases.
		 */
		protected static SchematicParams defaultParams() {
			return new SchematicParams(true, true, 0, 0);
		}
		
		/**
		 * Gets a SchematicParams object suitable for scheduled generation.
		 * 
		 * @param offsetX The x-offset of the schematic's origin, in
		 * region-lengths.
		 * @param offsetY The y-offset of the schematic's origin, in
		 * region-lengths.
		 */
		protected static SchematicParams scheduledGenParams(int offsetX, int offsetY) {
			return new SchematicParams(true, false, offsetX, offsetY);
		}
		
	}
	
	/**
	 * A class which holds a cached region and a counter as to the number of
	 * times it has been cached.
	 */
	private static class CachedRegion {
		
		/** The region. */
		private final Region region;
		/** The number of times the region has been cached. */
		private final AtomicInteger timesCached = new AtomicInteger(0);
		
		
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
			timesCached.getAndIncrement();
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
			return timesCached.decrementAndGet() == 0;
		}
		
	}
	
	/**
	 * A lock used to prevent a region from being generated multiple times
	 * simultaneously.
	 */
	private static class RegionLock {
		
		/** The RegionLock's backing semaphore. This is used instead of a
		 * ReentrantLock so that the permit may be acquired and released on
		 * different threads. Do <i>not</i> interact with this directly. */
		private final Semaphore semaphore = new Semaphore(1, false);
		/** Tracks the number of threads which are using this RegionLock, to
		 * determine whether or not it should be allowed to be garbage
		 * collected. Do <i>not</i> interact with this directly. */
		private final AtomicInteger threadsUsing = new AtomicInteger(0);
		/** Whether or not the schematics have been implanted in the region.
		 * This is reset to {@code false} every time the lock is freshly
		 * obtained. This is volatile. */
		private volatile boolean schematicsPlaced = false;
		
		
		/**
		 * Creates a new RegionLock.
		 */
		private RegionLock() {}
		
		/**
		 * Prepares to acquire the lock. This should prepend any invocations of
		 * {@link #lock} and {@link #tryLock}.
		 */
		private void preLock() {
			threadsUsing.getAndIncrement();
		}
		
		/**
		 * Acquires the lock. An invocation of this should be prepended by an
		 * invocation of {@link #preLock()}.
		 * 
		 * @return {@code true} if the lock was acquired; {@code false} if the
		 * current thread was interrupted while waiting to acquire the lock.
		 * @see Semaphore#acquire()
		 */
		private boolean lock() {
			try {
				semaphore.acquire();
				onLockSuccess();
				return true;
			} catch(InterruptedException e) {
				onLockFailure();
				return false;
			}
		}
		
		/**
		 * Attempts to acquire the lock. An invocation of this should be
		 * prepended by an invocation of {@link #preLock()}.
		 * 
		 * @return {@code true} if the lock was acquired; {@code false}
		 * otherwise.
		 */
		private boolean tryLock() {
			if(semaphore.tryAcquire()) {
				onLockSuccess();
				return true;
			} else {
				onLockFailure();
				return false;
			}
		}
		
		private void onLockSuccess() {
			// Reset at the start of every gen
			schematicsPlaced = false;
		}
		
		private void onLockFailure() {
			// Undo what preLock() does
			threadsUsing.getAndDecrement();
		}
		
		/**
		 * Releases the lock.
		 * 
		 * @see Semaphore#release()
		 */
		private void unlock() {
			semaphore.release();
			threadsUsing.getAndDecrement();
		}
		
		/**
		 * Queries if the lock is being held by another thread - that is, if
		 * the region is currently being generated (since this should be the
		 * only time the lock is held).
		 * 
		 * @return {@code true} if any thread holds the lock; {@code false}
		 * otherwise.
		 */
		private boolean isLocked() {
			return semaphore.availablePermits() == 0;
		}
		
		/**
		 * Checks for whether or not the lock is being used.
		 * 
		 * @return {@code true} if the lock is in use; {@code false} otherwise.
		 */
		private boolean isUnused() {
			return threadsUsing.get() == 0;
		}
	}
	
}
