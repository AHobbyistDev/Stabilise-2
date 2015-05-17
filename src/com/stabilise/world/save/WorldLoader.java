package com.stabilise.world.save;

import java.util.Objects;
import java.util.concurrent.Executor;

import com.badlogic.gdx.files.FileHandle;
import com.stabilise.util.Log;
import com.stabilise.util.annotation.UserThread;
import com.stabilise.world.HostWorld;
import com.stabilise.world.Region;
import com.stabilise.world.RegionCache.CachedRegion;
import com.stabilise.world.provider.WorldProvider;

/**
 * A {@code WorldLoader} instance manages the loading and saving of regions for
 * a world.
 * 
 * <p>Internally, a WorldLoader uses an ExecutorService to perform its I/O
 * tasks; each individual load or save request for a region is delegated to a
 * separate thread.
 * 
 * <p>TODO: Synchronisation policy on saved regions. Since it is incredibly
 * inefficient and wasteful to make a defensive copy of a region and its
 * contents when it is being saved, it can be expected that concurrency
 * problems will arise from the fact that said region and contents will be
 * modified while it is in the process of being saved. This can be rectified
 * either by:
 * 
 * <ul>
 * <li>never saving regions mid-game (though this lends itself to potential
 *     loss of data if, say, the JVM crashes and as such the game can't
 *     properly shut down), or
 * <li>defining a synchronisation policy wherein at minimum no exceptions or
 *     errors will be thrown, and cases of deadlock, livelock and starvation
 *     are impossible (though there may be inconsistent state data as the world
 *     changes while it is being saved - though at least that would be
 *     preferable to losing data)
 * </ul>
 */
public abstract class WorldLoader {
	
	/** The world provider for which the loader is loading. */
	protected final WorldProvider<?> provider;
	private final Executor executor;
	
	/** Whether or not loading operations should be cancelled due to the loader
	 * having been shut down. This is volatile. */
	private volatile boolean cancelLoadOperations = false;
	
	/** The world loader's log. */
	protected final Log log = Log.getAgent("WORLDLOADER");
	
	
	/**
	 * Creates a new WorldLoader.
	 * 
	 * @param provider The world provider.
	 * 
	 * @throws NullPointerException if {@code provider} is {@code null}.
	 */
	public WorldLoader(WorldProvider<?> provider) {
		this.provider = Objects.requireNonNull(provider);
		executor = provider.getExecutor();
	}
	
	/**
	 * Instructs this WorldLoader to asynchronously load a region. This method
	 * does nothing if {@link Region#getLoadPermit() region.getLoadPermit()}
	 * returns {@code false}.
	 * 
	 * @param world The region's parent world.
	 * @param region The region to load.
	 * 
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	@UserThread("Any")
	public final void loadRegion(HostWorld world, Region region, boolean generate) {
		if(region.getLoadPermit())
			executor.execute(new RegionLoader(world, region, generate));
		else if(generate)
			world.generator.generate(region);
	}
	
	/**
	 * Instructs this WorldLoader to load a region on the current thread. This
	 * instruction is ignored, however, if the region has already loaded or is
	 * currently loading. Note that this will set {@link Region#loaded loaded}
	 * to {@code true} once the region has completed loading.
	 * 
	 * <p>This method exists for the use of the WorldGenerator.
	 * 
	 * @param world The region's parent world.
	 * @param region The region to load.
	 */
	/*
	public final void loadRegionSynchronously(HostWorld world, Region region) {
		if(region.getLoadPermit())
			new RegionLoader(world, region, false).run();
	}
	*/
	
	/**
	 * Instructs this WorldLoader to asynchronously save a region.
	 * 
	 * <p>The request will be ignored if the region does not grant its {@link
	 * Region#getSavePermit() save permit}.
	 * 
	 * @param world The region's parent world.
	 * @param region The region to save.
	 * @param cacheHandle The handle to this region's cache entry. {@code null}
	 * is allowed.
	 */
	@UserThread("Any")
	public final void saveRegion(HostWorld world, Region region,
			CachedRegion cacheHandle) {
		//if(region.getSavePermit()) {
		executor.execute(new RegionSaver(world, region, cacheHandle));
		region.lastSaved = world.getAge();
		//}
	}
	
	/**
	 * Instructs the WorldLoader to save a region on the current thread.
	 * 
	 * <p>This method exists for the use of the WorldGenerator.
	 * 
	 * @param world The region's parent world.
	 * @param region The region to save.
	 */
	/*
	@UserThread("WorkerThread")
	public final void saveRegionSynchronously(HostWorld world, Region region) {
		if(region.getSavePermit()) {
			new RegionSaver(world, region).run();
			region.unsavedChanges = false;
			region.lastSaved = world.getAge();
		}
	}
	*/
	
	/**
	 * Loads a region.
	 * 
	 * @param r The region to load.
	 * @param file The region's file.
	 */
	@UserThread("WorkerThread")
	protected abstract void load(Region r, FileHandle file);
	
	/**
	 * Saves a region.
	 * 
	 * @param r The region to save.
	 * @param file The region's file.
	 */
	@UserThread("WorkerThread")
	protected abstract void save(Region r, FileHandle file);
	
	/**
	 * Shuts down the WorldLoader; region loading operations will be cancelled
	 * but region saves will be permitted to complete.
	 */
	@UserThread("MainThread")
	public void shutdown() {
		cancelLoadOperations = true;
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	/**
	 * A {@code Runnable} which is passed into {@link #executor} for the
	 * purpose of loading or saving a region.
	 * 
	 * @see RegionLoader
	 * @see RegionSaver
	 */
	private abstract class RegionIO implements Runnable {
		
		protected final HostWorld world;
		protected final Region r;
		
		/**
		 * @throws NPE if world is null
		 */
		protected RegionIO(HostWorld world, Region r) {
			this.world = Objects.requireNonNull(world);
			this.r = r;
		}
		
	}
	
	/**
	 * A RegionLoader is a Runnable which is passed into the executor for the
	 * purpose of loading a region.
	 */
	private class RegionLoader extends RegionIO {
		
		/** True if the region should be generated, if it is not already. */
		private final boolean generate;
		
		/**
		 * Creates a new RegionLoader.
		 * 
		 * @param world The region's parent world.
		 * @param region The region to load.
		 * @param generate Whether or not the region should be generated after
		 * being loaded, if it is not already.
		 * 
		 * @throws NullPointerException if {@code world} is {@code null}.
		 */
		public RegionLoader(HostWorld world, Region region, boolean generate) {
			super(world, region);
			this.generate = generate;
		}
		
		@Override
		public void run() {
			if(cancelLoadOperations)
				return;
			
			if(r.fileExists(world))
				load(r, r.getFile(world));
			
			if(generate)
				world.generator.generateSynchronously(r);
			
			//log.postFineDebug("Finished loading " + r);
		}
		
	}
	
	/**
	 * A RegionSaver is a Runnable which is passed into the executor for the
	 * purpose of saving a region.
	 */
	private class RegionSaver extends RegionIO {
		
		private final CachedRegion cacheHandle;
		
		/**
		 * Creates a new RegionSaver.
		 * 
		 * @param world The region's parent world.
		 * @param region The region to save.
		 * @param cache The handle to the region's cache entry. null is
		 * allowed.
		 * 
		 * @throws NullPointerException if {@code world} is {@code null}.
		 */
		public RegionSaver(HostWorld world, Region region, CachedRegion cache) {
			super(world, region);
			this.cacheHandle = cache;
		}
		
		@Override
		public void run() {
			if(r.getSavePermit()) {
				save(r, r.getFile(world));
				r.finishSaving();
			}
			// Extremely important final step in the lifecycle of a region: try
			// to uncache a region after it has been saved.
			world.regionCache.finaliseUncaching(cacheHandle);
		}
		
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Gets the loader to use for world loading.
	 * 
	 * @param provider The world provider.
	 * 
	 * @return The loader to use for world loading.
	 */
	public static WorldLoader getLoader(WorldProvider<?> provider) {
		return new PreAlphaWorldLoader(provider);
	}
	
}